package com.lirxowo.carryonextend.network;

import com.lirxowo.carryonextend.Constants;
import com.lirxowo.carryonextend.client.ClientThrowHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ForgeNetworkHandler {

    private static final String PROTOCOL_VERSION = "1.0";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Constants.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;
    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }

        INSTANCE.registerMessage(nextId(), ThrowPowerPacket.class, ThrowPowerPacket::encode, ThrowPowerPacket::decode, ForgeNetworkHandler::handleThrowPower);
        INSTANCE.registerMessage(nextId(), PlayerThrowPacket.class, PlayerThrowPacket::encode, PlayerThrowPacket::decode, ForgeNetworkHandler::handlePlayerThrow);
        registered = true;
    }

    public static void sendThrowPowerToServer(float power, boolean entity) {
        INSTANCE.sendToServer(new ThrowPowerPacket(power, entity));
    }

    public static void sendPlayerThrow(ServerPlayer player, double x, double y, double z) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PlayerThrowPacket(x, y, z));
    }

    private static int nextId() {
        return packetId++;
    }

    private static void handleThrowPower(ThrowPowerPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                packet.handle(player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handlePlayerThrow(PlayerThrowPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientThrowHandler.applyPlayerThrow(packet.x(), packet.y(), packet.z()));
        context.setPacketHandled(true);
    }
}
