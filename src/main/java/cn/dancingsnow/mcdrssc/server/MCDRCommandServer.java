package cn.dancingsnow.mcdrssc.server;

import cn.dancingsnow.mcdrssc.command.NodeData;
import cn.dancingsnow.mcdrssc.config.ModConfig;
import cn.dancingsnow.mcdrssc.networking.CommandNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.Reader;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class MCDRCommandServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "mcdrssc";

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static Logger LOGGER = LogManager.getLogger();

    public static ModConfig modConfig;
    public static Optional<NodeData> nodeData;

    @Override
    public void onInitializeServer() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        modConfig = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            dispatcher.register(literal("mcdrssc").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(literal("reload").executes(context -> {
                        context.getSource().sendFeedback(Text.of("Reloading nodes..."), true);
                        loadNodeData();
                        MinecraftServer server = context.getSource().getServer();
                        if (nodeData.isPresent()) {
                            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                CommandNetwork.sendNodeDataToClient(player, nodeData.get());
                            }
                        }
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
            LOGGER.error(e);
            nodeData = Optional.empty();
        }
    }

}
