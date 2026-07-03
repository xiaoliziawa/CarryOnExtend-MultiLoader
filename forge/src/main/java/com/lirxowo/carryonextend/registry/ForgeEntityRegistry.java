package com.lirxowo.carryonextend.registry;

import com.lirxowo.carryonextend.Constants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);

    public static final RegistryObject<EntityType<CustomFallingBlockEntity>> CUSTOM_FALLING_BLOCK = ENTITIES.register(
            "custom_falling_block",
            () -> EntityType.Builder.<CustomFallingBlockEntity>of(CustomFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(Constants.id("custom_falling_block").toString())
    );
}
