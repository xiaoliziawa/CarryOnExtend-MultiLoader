package com.lirxowo.carryonextend.client;

import com.lirxowo.carryonextend.Constants;
import com.lirxowo.carryonextend.client.renderer.CustomFallingBlockRenderer;
import com.lirxowo.carryonextend.registry.ForgeEntityRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ForgeEntityRegistry.CUSTOM_FALLING_BLOCK.get(), CustomFallingBlockRenderer::new);
    }
}
