package cn.dancingsnow.mcdr_command;

import cn.dancingsnow.mcdr_command.command.Node;
import cn.dancingsnow.mcdr_command.command.NodeData;
import cn.dancingsnow.mcdr_command.command.NodeType;
import cn.dancingsnow.mcdr_command.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class MCDRCommand implements ModInitializer {
    public static final String MOD_ID = "mcdr_command";
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static Logger LOGGER = LogManager.getLogger();
    public static ModConfig modConfig;
    public static Optional<NodeData> nodeData;

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        modConfig = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("mcdreforged").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(literal("reload").executes(context -> {
                        // TODO: reload
                        context.getSource().sendMessage(Text.literal("Reloading nodes..."));
                        loadNodeData();
                        return 1;
                    })));
        }));

        loadNodeData();

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

    public static Collection<String> getSuggestion(String text) {
        if (nodeData.isPresent()) {
            Collection<String> rt = new ArrayList<>();
            String[] args = text.split(" ");
            int word = args.length;
            if (word == 1 && !text.endsWith(" ")) {
                for (Node node : nodeData.get().data) {
                    if (node.type.equals(NodeType.LITERAL)) rt.add(node.name);
                }
                return rt;
            } else {
                Node currNode = null;
                for (Node node : nodeData.get().data) {
                    if (node.name.equalsIgnoreCase(args[0])) {
                        currNode = node;
                    }
                }
                if (currNode != null) {
                    int times;
                    if (text.endsWith(" ")) {
                        // find args[word-1] suggestion
                         times = word-1;
                    } else {
                        // find args[word-2] suggestion
                        times = word-2;
                    }
                    for (int i = 1; i<=times; i++) {
                        boolean flag = false;
                        for (Node node : currNode.children) {
                            if (args[i].equalsIgnoreCase(node.name)) {
                                currNode = node;
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            return rt;
                        }
                    }
                    LOGGER.info(currNode.name);
                    for (Node node : currNode.children) {
                        if (node.type.equals(NodeType.LITERAL)) rt.add(node.name);
                    }
                } else {
                    return rt;
                }

            }
            return rt;
        } else {
            return new ArrayList<>();
        }
    }

}
