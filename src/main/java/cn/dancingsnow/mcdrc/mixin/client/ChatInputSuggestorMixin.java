package cn.dancingsnow.mcdrc.mixin.client;

import cn.dancingsnow.mcdrc.client.MCDRCommandClient;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow
    @Final
    private TextFieldWidget textField;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected static int getLastPlayerNameStart(String input) {
        return 0;
    }

    @Shadow private boolean completingSuggestions;

    @Shadow @Nullable private CommandSuggestor.@Nullable SuggestionWindow window;

    @Shadow public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "refresh()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I", shift = At.Shift.AFTER), cancellable = true)
    public void refreshMixin(CallbackInfo ci) {
        String text = this.textField.getText();
        if (text.startsWith("!") || text.startsWith("！")) {
            text = text.replace('！', '!');
            String string = text.substring(0, this.textField.getCursor());
            if (this.window == null || !this.completingSuggestions) {
                int word = getLastPlayerNameStart(string);
                Collection<String> suggestion = MCDRCommandClient.getSuggestion(string);
                if (suggestion.size() > 0) {
                    this.pendingSuggestions = CommandSource.suggestMatching(suggestion,
                            new SuggestionsBuilder(string, word));
                    this.pendingSuggestions.thenRun(() -> {
                        if (this.pendingSuggestions.isDone()) {
                            this.showSuggestions(true);
                        }
                    });
                } else {
                    Collection<String> player_list = this.client.player.networkHandler.getCommandSource().getPlayerNames();
                    this.pendingSuggestions = CommandSource.suggestMatching(player_list, new SuggestionsBuilder(string, word));
                }
            }
            ci.cancel();
        }
    }
}
