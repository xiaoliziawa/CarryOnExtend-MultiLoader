package com.lirxowo.carryonextend.mixin;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CriteriaTriggers.class)
public interface CriteriaTriggersAccessor {

    @Invoker("register")
    static <T extends CriterionTrigger<?>> T carryonextend$register(T trigger) {
        throw new AssertionError();
    }
}
