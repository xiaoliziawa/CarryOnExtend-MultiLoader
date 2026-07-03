package com.lirxowo.carryonextend;

import com.lirxowo.carryonextend.trigger.TriggerRegistry;

public class CommonClass {

    public static void init() {
        TriggerRegistry.register();
        Constants.LOG.info("{} initialized", Constants.MOD_NAME);
    }
}
