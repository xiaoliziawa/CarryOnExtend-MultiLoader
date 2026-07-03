package com.lirxowo.carryonextend.handler;

import com.lirxowo.carryonextend.platform.Services;
import com.lirxowo.carryonextend.trigger.TriggerRegistry;
import com.lirxowo.carryonextend.util.FallingBlockUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingPacket;

import java.util.UUID;

public class EntityThrowHandler {

    private static final float BASE_THROW_POWER = 0.8f;
    private static final float BASE_THROW_UPWARD = 0.2f;
    private static final float MAX_POWER_MULTIPLIER = 2.5f;
    private static final float PLAYER_THROW_POWER_BONUS = 1.2f;
    private static final float PLAYER_THROW_UPWARD_BONUS = 0.6f;

    public static void throwCarriedEntity(ServerPlayer player) {
        throwCarriedEntityWithPower(player, 1.0f);
    }

    public static void throwCarriedEntityWithPower(ServerPlayer player, float powerFactor) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if (!carry.isCarrying(CarryType.ENTITY) && !carry.isCarrying(CarryType.PLAYER)) {
            return;
        }

        Level level = player.level();
        float powerMultiplier = 1.0f + powerFactor * (MAX_POWER_MULTIPLIER - 1.0f);
        float throwPower = BASE_THROW_POWER * powerMultiplier;
        float throwUpward = BASE_THROW_UPWARD * powerMultiplier;

        if (carry.isCarrying(CarryType.PLAYER) && throwCarriedPlayer(player, carry, level, throwPower, throwUpward, powerFactor)) {
            return;
        }

        CompoundTag entityNbt = carry.getContentNbt();
        if (entityNbt == null) {
            clearCarry(player, carry);
            return;
        }

        if (FallingBlockUtil.handleFallingBlockThrow(player, carry, entityNbt, throwPower, throwUpward, powerFactor)) {
            return;
        }

        Entity entity = getOrCreateEntity(player, carry, entityNbt, level);
        if (entity == null) {
            clearCarry(player, carry);
            return;
        }

        Vec3 playerPos = player.position().add(0, 1.0, 0);
        entity.setPos(playerPos);
        runPlaceScript(player, carry);
        mergeEntityData(entity, entityNbt);
        level.addFreshEntity(entity);

        Vec3 lookDir = player.getLookAngle();
        entity.setDeltaMovement(lookDir.x * throwPower, throwUpward, lookDir.z * throwPower);

        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }

        if (entity instanceof PrimedTnt primedTnt) {
            primedTnt.addTag("thrownBy:" + player.getUUID());
            TriggerRegistry.TNT_THROW.trigger(player);
        }

        FallingBlockUtil.playThrowSound(level, player, powerFactor);
        clearCarry(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private static boolean throwCarriedPlayer(ServerPlayer player, CarryOnData carry, Level level, float throwPower, float throwUpward, float powerFactor) {
        Entity passenger = player.getFirstPassenger();
        if (passenger == null) {
            return false;
        }

        Vec3 lookDir = player.getLookAngle();
        Vec3 playerPos = player.position().add(lookDir.multiply(2.0, 0, 2.0)).add(0, 1.5, 0);
        Vec3 velocity = new Vec3(
                lookDir.x * (throwPower + PLAYER_THROW_POWER_BONUS),
                throwUpward + PLAYER_THROW_UPWARD_BONUS,
                lookDir.z * (throwPower + PLAYER_THROW_POWER_BONUS)
        );

        syncStopRiding(player, level, passenger);
        passenger.stopRiding();
        player.ejectPassengers();
        passenger.setPos(playerPos);

        if (passenger instanceof ServerPlayer thrownPlayer) {
            thrownPlayer.connection.teleport(playerPos.x, playerPos.y, playerPos.z, thrownPlayer.getYRot(), thrownPlayer.getXRot());
            Services.PLATFORM.sendPlayerThrowPacket(thrownPlayer, velocity.x, velocity.y, velocity.z);
        } else {
            passenger.setDeltaMovement(velocity);
        }

        playPlayerThrowSound(level, player, powerFactor);
        clearCarry(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }

    private static void syncStopRiding(ServerPlayer player, Level level, Entity passenger) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
            tschipp.carryon.platform.Services.PLATFORM.sendPacketToPlayer(
                    tschipp.carryon.Constants.PACKET_ID_START_RIDING,
                    new ClientboundStartRidingPacket(passenger.getId(), false),
                    serverPlayer
            );
        }
    }

    private static Entity getOrCreateEntity(ServerPlayer player, CarryOnData carry, CompoundTag entityNbt, Level level) {
        Entity entity = carry.getEntity(level);
        if (entity != null) {
            return entity;
        }

        if (!entityNbt.contains("id") || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        EntityType<?> entityType = EntityType.byString(entityNbt.getString("id")).orElse(null);
        if (entityType == null) {
            return null;
        }

        Entity createdEntity = entityType.create(serverLevel);
        if (createdEntity == null) {
            return null;
        }

        createdEntity.load(entityNbt);
        if (entityNbt.hasUUID("UUID")) {
            UUID entityUuid = entityNbt.getUUID("UUID");
            createdEntity.setUUID(entityUuid);
        }

        return createdEntity;
    }

    private static void runPlaceScript(ServerPlayer player, CarryOnData carry) {
        if (carry.getActiveScript().isEmpty() || player.getServer() == null) {
            return;
        }

        ScriptEffects effects = carry.getActiveScript().get().scriptEffects();
        String command = effects.commandPlace();
        if (!command.isEmpty()) {
            player.getServer().getCommands().performPrefixedCommand(
                    player.getServer().createCommandSourceStack(),
                    "/execute as " + player.getGameProfile().getName() + " run " + command
            );
        }
    }

    private static void mergeEntityData(Entity entity, CompoundTag entityNbt) {
        if (entityNbt.isEmpty()) {
            return;
        }

        entityNbt.remove("Pos");
        CompoundTag savedData = new CompoundTag();
        entity.saveWithoutId(savedData);
        entityNbt.merge(savedData);
        entity.load(entityNbt);
    }

    private static void clearCarry(ServerPlayer player, CarryOnData carry) {
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
    }

    private static void playPlayerThrowSound(Level level, ServerPlayer player, float powerFactor) {
        float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);
    }
}
