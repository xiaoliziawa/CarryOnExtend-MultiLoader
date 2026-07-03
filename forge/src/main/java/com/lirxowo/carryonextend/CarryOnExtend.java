package com.lirxowo.carryonextend;

import com.lirxowo.carryonextend.network.ForgeNetworkHandler;
import com.lirxowo.carryonextend.registry.ForgeEntityRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class CarryOnExtend {

    public CarryOnExtend() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeEntityRegistry.ENTITIES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CommonClass.init();
            ForgeNetworkHandler.register();
        });
    }
}
