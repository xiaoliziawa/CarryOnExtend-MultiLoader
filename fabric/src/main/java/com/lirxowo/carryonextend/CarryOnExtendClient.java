package com.lirxowo.carryonextend;

import com.lirxowo.carryonextend.client.ClientThrowHandler;
import com.lirxowo.carryonextend.client.PowerThrowHandler;
import com.lirxowo.carryonextend.client.renderer.CustomFallingBlockRenderer;
import com.lirxowo.carryonextend.network.FabricNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarryOnExtendClient implements ClientModInitializer {

    private boolean dropPressed;

    @Override
    public void onInitializeClient() {
        FabricNetworkHandler.registerClientReceivers();
        EntityRendererRegistry.register(CarryOnExtend.CUSTOM_FALLING_BLOCK, CustomFallingBlockRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
    }

    private void onEndClientTick(Minecraft minecraft) {
        handleDropKey(minecraft);
        ClientThrowHandler.tick();
    }

    private void handleDropKey(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            dropPressed = false;
            return;
        }

        boolean currentlyPressed = minecraft.options.keyDrop.isDown();
        if (!currentlyPressed) {
            dropPressed = false;
            return;
        }

        if (dropPressed) {
            return;
        }

        dropPressed = true;
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        boolean carryingEntity = carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryOnData.CarryType.PLAYER);
        if (carryingEntity || carry.isCarrying(CarryOnData.CarryType.BLOCK)) {
            FabricNetworkHandler.sendThrowPowerToServer(PowerThrowHandler.getPowerFactor(), carryingEntity);
        }
    }
}
