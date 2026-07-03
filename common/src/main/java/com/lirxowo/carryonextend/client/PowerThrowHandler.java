package com.lirxowo.carryonextend.client;

import com.lirxowo.carryonextend.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class PowerThrowHandler {

    private static final int MAX_POWER_STEPS = 10;
    private static final float MIN_POWER = 0.1f;

    private static int powerLevel = MAX_POWER_STEPS;

    public static float getPowerFactor() {
        if (powerLevel == 1) {
            return MIN_POWER;
        }

        return MIN_POWER + (float) (powerLevel - 1) / (MAX_POWER_STEPS - 1) * (1.0f - MIN_POWER);
    }

    public static boolean handleScroll(double scrollDelta) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || !player.isShiftKeyDown() || !isCarrying(player)) {
            return false;
        }

        if (scrollDelta > 0) {
            increasePower();
        } else if (scrollDelta < 0) {
            decreasePower();
        }

        return scrollDelta != 0;
    }

    public static boolean isCarrying(Player player) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        return carry.isCarrying(CarryOnData.CarryType.ENTITY)
                || carry.isCarrying(CarryOnData.CarryType.PLAYER)
                || carry.isCarrying(CarryOnData.CarryType.BLOCK);
    }

    private static void increasePower() {
        if (powerLevel < MAX_POWER_STEPS) {
            powerLevel++;
            playPowerChangeSound();
            displayPowerLevel();
        }
    }

    private static void decreasePower() {
        if (powerLevel > 1) {
            powerLevel--;
            playPowerChangeSound();
            displayPowerLevel();
        }
    }

    private static void displayPowerLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        int percentage = (int) (getPowerFactor() * 100);
        Component message = Component.translatable("message." + Constants.MOD_ID + ".power_level", powerLevel, MAX_POWER_STEPS, percentage);
        player.displayClientMessage(message, true);
    }

    private static void playPowerChangeSound() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        float pitch = 0.5f + getPowerFactor();
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.2f, pitch, false);
    }
}
