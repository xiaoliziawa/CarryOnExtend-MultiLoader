package com.lirxowo.carryonextend.trigger;

import com.google.gson.JsonObject;
import com.lirxowo.carryonextend.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TntThrowTrigger extends SimpleCriterionTrigger<TntThrowTrigger.TriggerInstance> {

    private static final ResourceLocation ID = Constants.id("tnt_throw");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player);
    }

    public void trigger(ServerPlayer player) {
        trigger(player, triggerInstance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ContextAwarePredicate player) {
            super(ID, player);
        }

        public static TriggerInstance tntThrow() {
            return new TriggerInstance(ContextAwarePredicate.ANY);
        }
    }
}
