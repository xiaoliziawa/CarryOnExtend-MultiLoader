package com.lirxowo.carryonextend.trigger;

import com.lirxowo.carryonextend.mixin.CriteriaTriggersAccessor;

public class TriggerRegistry {

    public static final TntThrowTrigger TNT_THROW = new TntThrowTrigger();
    public static final SelfDestructionTrigger SELF_DESTRUCTION = new SelfDestructionTrigger();

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }

        CriteriaTriggersAccessor.carryonextend$register(TNT_THROW);
        CriteriaTriggersAccessor.carryonextend$register(SELF_DESTRUCTION);
        registered = true;
    }
}
