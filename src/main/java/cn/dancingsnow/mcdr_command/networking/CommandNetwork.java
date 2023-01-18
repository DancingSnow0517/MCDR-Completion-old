package cn.dancingsnow.mcdr_command.networking;

import cn.dancingsnow.mcdr_command.client.MCDRCommandClient;

import cn.dancingsnow.mcdr_command.command.NodeData;
import cn.dancingsnow.mcdr_command.server.MCDRCommandServer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CommandNetwork {

    public static final Identifier COMMAND_PACKET_ID = new Identifier(MCDRCommandClient.MOD_ID, "command");

    public static void sendNodeDataToClient(ServerPlayerEntity player, NodeData nodeData) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(MCDRCommandServer.GSON.toJson(nodeData));
        ServerPlayNetworking.send(player, COMMAND_PACKET_ID, buf);
    }

}
