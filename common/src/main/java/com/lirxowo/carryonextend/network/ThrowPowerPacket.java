package com.lirxowo.carryonextend.network;

import com.lirxowo.carryonextend.handler.BlockThrowHandler;
import com.lirxowo.carryonextend.handler.EntityThrowHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ThrowPowerPacket(float power, boolean entity) {

    public static ThrowPowerPacket decode(FriendlyByteBuf buffer) {
        return new ThrowPowerPacket(buffer.readFloat(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(power);
        buffer.writeBoolean(entity);
    }

    public void handle(ServerPlayer player) {
        if (entity) {
            EntityThrowHandler.throwCarriedEntityWithPower(player, power);
        } else {
            BlockThrowHandler.throwCarriedBlockWithPower(player, power);
        }
    }
}
