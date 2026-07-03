package com.lirxowo.carryonextend;

import com.lirxowo.carryonextend.network.FabricNetworkHandler;
import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.lirxowo.carryonextend.trigger.TriggerRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.PrimedTnt;

public class CarryOnExtend implements ModInitializer {

    public static final EntityType<CustomFallingBlockEntity> CUSTOM_FALLING_BLOCK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Constants.id("custom_falling_block"),
            EntityType.Builder.<CustomFallingBlockEntity>of(CustomFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(Constants.id("custom_falling_block").toString())
    );

    @Override
    public void onInitialize() {
        CommonClass.init();
        FabricNetworkHandler.registerServerReceivers();
        ServerLivingEntityEvents.AFTER_DEATH.register(this::afterDeath);
    }

    private void afterDeath(net.minecraft.world.entity.LivingEntity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity instanceof PrimedTnt tnt && tnt.getTags().contains("thrownBy:" + player.getUUID())) {
            TriggerRegistry.SELF_DESTRUCTION.trigger(player);
        }
    }
}
