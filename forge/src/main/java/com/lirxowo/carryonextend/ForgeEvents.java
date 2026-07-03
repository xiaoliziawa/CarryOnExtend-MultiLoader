package com.lirxowo.carryonextend;

import com.lirxowo.carryonextend.client.ClientThrowHandler;
import com.lirxowo.carryonextend.client.PowerThrowHandler;
import com.lirxowo.carryonextend.network.ForgeNetworkHandler;
import com.lirxowo.carryonextend.trigger.TriggerRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class ForgeEvents {

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID)
    public static class Server {

        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            DamageSource damageSource = event.getSource();
            Entity directEntity = damageSource.getDirectEntity();
            if (directEntity instanceof PrimedTnt tnt && tnt.getTags().contains("thrownBy:" + player.getUUID())) {
                TriggerRegistry.SELF_DESTRUCTION.trigger(player);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static class Client {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null || event.getAction() != InputConstants.PRESS || event.getKey() != minecraft.options.keyDrop.getKey().getValue()) {
                return;
            }

            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            boolean carryingEntity = carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryOnData.CarryType.PLAYER);
            if (!carryingEntity && !carry.isCarrying(CarryOnData.CarryType.BLOCK)) {
                return;
            }

            ForgeNetworkHandler.sendThrowPowerToServer(PowerThrowHandler.getPowerFactor(), carryingEntity);
        }

        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (PowerThrowHandler.handleScroll(event.getScrollDelta())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                ClientThrowHandler.tick();
            }
        }
    }
}
