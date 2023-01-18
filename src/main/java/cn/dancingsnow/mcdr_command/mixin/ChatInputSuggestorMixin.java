package cn.dancingsnow.mcdr_command.mixin;

import cn.dancingsnow.mcdr_command.MCDRCommand;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow @Final private TextFieldWidget textField;

    @Shadow @Nullable private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected static int getStartOfCurrentWord(String input) {
        return 0;
    }

    @Shadow protected abstract void showCommandSuggestions();

    @Inject(method = "refresh()V", at = @At("RETURN"))
    public void refreshMixin(CallbackInfo ci) {
        String text = this.textField.getText();
        String string = text.substring(0, this.textField.getCursor());
        int word = getStartOfCurrentWord(string);
        if (text.startsWith("!")) {
            this.pendingSuggestions = CommandSource.suggestMatching(MCDRCommand.getSuggestion(text),
                    new SuggestionsBuilder(string, word));
        }
    }
}
