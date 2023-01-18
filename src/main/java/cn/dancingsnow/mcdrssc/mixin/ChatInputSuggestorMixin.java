package cn.dancingsnow.mcdrssc.mixin;

import cn.dancingsnow.mcdrssc.client.MCDRCommandClient;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.CommandSuggestor;
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

    @Inject(method = "refresh()V", at = @At("RETURN"))
    public void refreshMixin(CallbackInfo ci) {
        String text = this.textField.getText();
        if (text.startsWith("!")) {
            String string = text.substring(0, this.textField.getCursor());
            int word = getLastPlayerNameStart(string);
            Collection<String> suggestion = MCDRCommandClient.getSuggestion(string);
            if (suggestion.size() > 0) {
                this.pendingSuggestions = CommandSource.suggestMatching(suggestion,
                        new SuggestionsBuilder(string, word));
            }
        }
    }
}
