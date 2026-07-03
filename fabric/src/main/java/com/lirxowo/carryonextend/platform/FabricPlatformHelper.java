package com.lirxowo.carryonextend.platform;

import com.lirxowo.carryonextend.CarryOnExtend;
import com.lirxowo.carryonextend.network.FabricNetworkHandler;
import com.lirxowo.carryonextend.platform.services.IPlatformHelper;
import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public EntityType<CustomFallingBlockEntity> getCustomFallingBlockType() {
        return CarryOnExtend.CUSTOM_FALLING_BLOCK;
    }

    @Override
    public void sendPlayerThrowPacket(ServerPlayer player, double x, double y, double z) {
        FabricNetworkHandler.sendPlayerThrow(player, x, y, z);
    }
}
