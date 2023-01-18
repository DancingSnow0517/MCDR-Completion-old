package cn.dancingsnow.mcdrssc.networking;

import cn.dancingsnow.mcdrssc.client.MCDRCommandClient;

import cn.dancingsnow.mcdrssc.command.NodeData;
import cn.dancingsnow.mcdrssc.server.MCDRCommandServer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CommandNetwork {

    public static final Identifier COMMAND_PACKET_ID = new Identifier(MCDRCommandClient.MOD_ID, "command");

    public static void sendNodeDataToClient(ServerPlayNetworkHandler handler, NodeData nodeData) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(MCDRCommandServer.GSON.toJson(nodeData));
        ServerPlayNetworking.getSender(handler).sendPacket(COMMAND_PACKET_ID, buf);
    }

}
