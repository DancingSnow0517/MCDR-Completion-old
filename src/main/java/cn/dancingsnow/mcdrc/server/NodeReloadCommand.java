package cn.dancingsnow.mcdrc.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class NodeReloadCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("mcdrc")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(literal("reload").executes(context -> {
                            context.getSource().sendFeedback(Text.literal("Reloading nodes..."), true);
                            MCDRCommandServer.loadNodeData();
                            return 1;
                        })));
    }

}
