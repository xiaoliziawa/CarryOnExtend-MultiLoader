package com.lirxowo.carryonextend.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ClientThrowHandler {

    private static final int THROW_EFFECT_DURATION = 10;

    private static long throwTime = Long.MIN_VALUE;
    private static double throwVelocityX;
    private static double throwVelocityY;
    private static double throwVelocityZ;

    public static void applyPlayerThrow(double x, double y, double z) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        player.setDeltaMovement(x, y, z);
        player.hurtMarked = true;
        player.setOnGround(false);

        throwTime = player.level().getGameTime();
        throwVelocityX = x;
        throwVelocityY = y;
        throwVelocityZ = z;

        Vec3 currentMotion = player.getDeltaMovement();
        player.setDeltaMovement(currentMotion.x, currentMotion.y + 0.1, currentMotion.z);
        player.fallDistance = 0.0f;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || throwTime == Long.MIN_VALUE) {
            return;
        }

        Player player = minecraft.player;
        long elapsedTicks = player.level().getGameTime() - throwTime;
        if (elapsedTicks >= THROW_EFFECT_DURATION) {
            clearThrowData();
            return;
        }

        Vec3 currentMotion = player.getDeltaMovement();
        float factor = 1.0f - elapsedTicks / (float) THROW_EFFECT_DURATION;
        double additionalY = currentMotion.y < 0 ? 0.08 * factor : 0;

        double newVelocityX = currentMotion.x * 0.6 + throwVelocityX * 0.4 * factor;
        double newVelocityY = Math.max(currentMotion.y, throwVelocityY * factor) + additionalY;
        double newVelocityZ = currentMotion.z * 0.6 + throwVelocityZ * 0.4 * factor;

        player.setDeltaMovement(newVelocityX, newVelocityY, newVelocityZ);
        player.hurtMarked = true;

        if (player.onGround() && currentMotion.y > 0) {
            player.setOnGround(false);
        }

        player.fallDistance = 0.0f;
    }

    private static void clearThrowData() {
        throwTime = Long.MIN_VALUE;
        throwVelocityX = 0;
        throwVelocityY = 0;
        throwVelocityZ = 0;
    }
}
