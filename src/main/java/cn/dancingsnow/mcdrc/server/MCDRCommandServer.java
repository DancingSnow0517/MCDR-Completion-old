package cn.dancingsnow.mcdrc.server;

import cn.dancingsnow.mcdrc.command.NodeData;
import cn.dancingsnow.mcdrc.config.ModConfig;
import cn.dancingsnow.mcdrc.networking.CommandNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.Reader;

import static net.minecraft.server.command.CommandManager.literal;

public class MCDRCommandServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "mcdrc";

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static Logger LOGGER = LogManager.getLogger();

    public static ModConfig modConfig;
    public static NodeData nodeData = null;

    @Override
    public void onInitializeServer() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        modConfig = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("mcdrc").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(literal("reload").executes(context -> {
                        context.getSource().sendFeedback(Text.literal("Reloading nodes..."), true);
                        loadNodeData();
                        return 1;
                    })));
        }));

        NodeChangeWatcher.init();
        loadNodeData();
    }

    public static void loadNodeData() {
        try {
            Reader reader = new FileReader(modConfig.node_path);
            NodeData data = GSON.fromJson(reader, NodeData.class);
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
