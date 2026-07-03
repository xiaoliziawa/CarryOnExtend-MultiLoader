package com.lirxowo.carryonextend.network;

import com.lirxowo.carryonextend.Constants;
import com.lirxowo.carryonextend.client.ClientThrowHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkHandler {

    public static final net.minecraft.resources.ResourceLocation THROW_POWER = Constants.id("throw_power");
    public static final net.minecraft.resources.ResourceLocation PLAYER_THROW = Constants.id("player_throw");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(THROW_POWER, (server, player, handler, buffer, responseSender) -> {
            ThrowPowerPacket packet = ThrowPowerPacket.decode(buffer);
            server.execute(() -> packet.handle(player));
        });
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(PLAYER_THROW, (client, handler, buffer, responseSender) -> {
            PlayerThrowPacket packet = PlayerThrowPacket.decode(buffer);
            client.execute(() -> ClientThrowHandler.applyPlayerThrow(packet.x(), packet.y(), packet.z()));
        });
    }

    public static void sendThrowPowerToServer(float power, boolean entity) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        new ThrowPowerPacket(power, entity).encode(buffer);
        ClientPlayNetworking.send(THROW_POWER, buffer);
    }

    public static void sendPlayerThrow(ServerPlayer player, double x, double y, double z) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        new PlayerThrowPacket(x, y, z).encode(buffer);
        ServerPlayNetworking.send(player, PLAYER_THROW, buffer);
    }
}
