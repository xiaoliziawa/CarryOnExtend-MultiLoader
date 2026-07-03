package com.lirxowo.carryonextend;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

    public static final String MOD_ID = "carryonextend";
    public static final String MOD_NAME = "CarryOnExtend";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
