package cn.dancingsnow.mcdr_command.client;

import cn.dancingsnow.mcdr_command.command.Node;
import cn.dancingsnow.mcdr_command.command.NodeData;
import cn.dancingsnow.mcdr_command.command.NodeType;
import cn.dancingsnow.mcdr_command.networking.CommandNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class MCDRCommandClient implements ClientModInitializer {
    public static final String MOD_ID = "mcdr_command";
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static Logger LOGGER = LogManager.getLogger();

    public static Optional<NodeData> nodeData = Optional.empty();


    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(CommandNetwork.COMMAND_PACKET_ID, ((client, handler, buf, responseSender) -> {
            try {
                nodeData = Optional.ofNullable(GSON.fromJson(buf.readString(), NodeData.class));
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("fail to receiver command packet: ", e);
            }
        }));

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
                        times = word - 1;
                    } else {
                        // find args[word-2] suggestion
                        times = word - 2;
                    }
                    for (int i = 1; i <= times; i++) {
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
