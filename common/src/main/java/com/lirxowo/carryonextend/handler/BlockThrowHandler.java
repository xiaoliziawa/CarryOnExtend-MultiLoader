package com.lirxowo.carryonextend.handler;

import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.lirxowo.carryonextend.util.FallingBlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;

public class BlockThrowHandler {

    private static final float BASE_THROW_POWER = 0.8f;
    private static final float BASE_THROW_UPWARD = 0.3f;
    private static final float MAX_POWER_MULTIPLIER = 2.5f;

    public static void throwCarriedBlock(ServerPlayer player) {
        throwCarriedBlockWithPower(player, 1.0f);
    }

    public static void throwCarriedBlockWithPower(ServerPlayer player, float powerFactor) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if (!carry.isCarrying(CarryType.BLOCK)) {
            return;
        }

        Level level = player.level();
        BlockState blockState = carry.getBlock();
        Vec3 playerPos = player.getEyePosition().add(player.getLookAngle().scale(0.5));
        BlockEntity blockEntity = carry.getBlockEntity(BlockPos.containing(playerPos));

        runPlaceScript(player, carry);

        float powerMultiplier = 1.0f + powerFactor * (MAX_POWER_MULTIPLIER - 1.0f);
        Vec3 lookDir = player.getLookAngle();
        Vec3 motion = new Vec3(
                lookDir.x * BASE_THROW_POWER * powerMultiplier,
                BASE_THROW_UPWARD * powerMultiplier,
                lookDir.z * BASE_THROW_POWER * powerMultiplier
        );

        CompoundTag blockData = blockEntity != null ? blockEntity.saveWithFullMetadata() : new CompoundTag();
        CustomFallingBlockEntity.throwBlock(level, playerPos.x, playerPos.y, playerPos.z, blockState, blockData, motion);
        FallingBlockUtil.playThrowSound(level, player, powerFactor);

        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
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
}
