package com.lirxowo.carryonextend.platform;

import com.lirxowo.carryonextend.network.ForgeNetworkHandler;
import com.lirxowo.carryonextend.platform.services.IPlatformHelper;
import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.lirxowo.carryonextend.registry.ForgeEntityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public EntityType<CustomFallingBlockEntity> getCustomFallingBlockType() {
        return ForgeEntityRegistry.CUSTOM_FALLING_BLOCK.get();
    }

    @Override
    public void sendPlayerThrowPacket(ServerPlayer player, double x, double y, double z) {
        ForgeNetworkHandler.sendPlayerThrow(player, x, y, z);
    }
}
