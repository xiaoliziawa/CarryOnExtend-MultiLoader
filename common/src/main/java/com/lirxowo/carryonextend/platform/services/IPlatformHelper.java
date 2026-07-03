package com.lirxowo.carryonextend.platform.services;

import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    EntityType<CustomFallingBlockEntity> getCustomFallingBlockType();

    void sendPlayerThrowPacket(ServerPlayer player, double x, double y, double z);

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
