package cn.dancingsnow.mcdrc.server;

import cn.dancingsnow.mcdrc.command.NodeData;
import cn.dancingsnow.mcdrc.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.literal;

public class MCDRCommandServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "mcdrc";
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static Logger LOGGER = LogManager.getLogger();
    public static ModConfig modConfig = new ModConfig("config/%s.json".formatted(MOD_ID));
    public static NodeData nodeData = null;

    @Override
    public void onInitializeServer() {
        Path config = Path.of("config");
        if (!Files.exists(config)) {
            try {
                Files.createDirectory(config);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("MCDR-Completion init server fail");
            }
        }
        if (!modConfig.load()) {
            LOGGER.error("MCDR-Completion load config fail.");
            throw new IllegalStateException("MCDR-Completion init server fail");
        }

        modConfig.save();

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("mcdrc")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(literal("reload").executes(context -> {
                            context.getSource().sendFeedback(Text.literal("Reloading nodes..."), true);
                            loadNodeData();
                            return 1;
                        })))));

        NodeChangeWatcher.init();
        loadNodeData();
    }

    public static void loadNodeData() {
        try {
            NodeData data = GSON.fromJson(Files.newBufferedReader(Path.of(modConfig.getNodePath())), NodeData.class);
            if (data != null) nodeData = data;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public static NodeData getNodeData() {
        return nodeData;
    }

}
