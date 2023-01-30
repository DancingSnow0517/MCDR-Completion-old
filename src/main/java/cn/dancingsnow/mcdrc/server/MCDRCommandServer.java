package cn.dancingsnow.mcdrc.server;

import cn.dancingsnow.mcdrc.command.NodeData;
import cn.dancingsnow.mcdrc.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public class MCDRCommandServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "mcdrc";
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static Logger LOGGER = LogManager.getLogger();
    public static ModConfig modConfig = new ModConfig(
            FabricLoader.getInstance().getConfigDir().resolve("%s.json".formatted(MOD_ID))
    );
    public static NodeData nodeData = null;

    @Override
    public void onInitializeServer() {
        if (!modConfig.load()) {
            LOGGER.error("MCDR-Completion load config fail.");
            throw new IllegalStateException("MCDR-Completion init server fail");
        }

        modConfig.save();

        CommandRegistrationCallback.EVENT.register(NodeReloadCommand::register);

        loadNodeData();
    }

    public static void loadNodeData() {
        try {
            Path nodePath = Path.of(modConfig.getNodePath());
            if (Files.exists(nodePath)) {
                NodeData data = GSON.fromJson(Files.newBufferedReader(nodePath), NodeData.class);
                if (data != null) nodeData = data;
            } else {
                LOGGER.error("MCDR-Completion node file not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public static NodeData getNodeData() {
        return nodeData;
    }

}
