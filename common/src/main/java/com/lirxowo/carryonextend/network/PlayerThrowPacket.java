package com.lirxowo.carryonextend.network;

import net.minecraft.network.FriendlyByteBuf;

public record PlayerThrowPacket(double x, double y, double z) {

    public static PlayerThrowPacket decode(FriendlyByteBuf buffer) {
        return new PlayerThrowPacket(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }
}
