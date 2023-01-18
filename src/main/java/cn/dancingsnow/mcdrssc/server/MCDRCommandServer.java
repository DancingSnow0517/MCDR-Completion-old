package cn.dancingsnow.mcdrssc.server;

import cn.dancingsnow.mcdrssc.client.MCDRCommandClient;
import cn.dancingsnow.mcdrssc.command.NodeData;
import cn.dancingsnow.mcdrssc.config.ModConfig;
import cn.dancingsnow.mcdrssc.networking.CommandNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;

import java.io.FileReader;
import java.io.Reader;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class MCDRCommandServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "mcdrssc";

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static ModConfig modConfig;
    public static Optional<NodeData> nodeData;

    @Override
    public void onInitializeServer() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        modConfig = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("mcdrssc").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(literal("reload").executes(context -> {
                        context.getSource().sendMessage(Text.literal("Reloading nodes..."));
                        loadNodeData();
                        return 1;
                    })));
        }));

        loadNodeData();

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            nodeData.ifPresent(data -> CommandNetwork.sendNodeDataToClient(handler.player, data));
        }));
    }

    public static void loadNodeData() {
        try {
            Reader reader = new FileReader(modConfig.node_path);
            NodeData data = GSON.fromJson(reader, NodeData.class);
            nodeData = Optional.ofNullable(data);
        } catch (Exception e) {
            e.printStackTrace();
            MCDRCommandClient.LOGGER.error(e);
            nodeData = Optional.empty();
        }
    }

}
