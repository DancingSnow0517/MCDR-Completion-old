package cn.dancingsnow.mcdrssc.mixin;

import cn.dancingsnow.mcdrssc.command.NodeData;
import cn.dancingsnow.mcdrssc.networking.CommandNetwork;
import cn.dancingsnow.mcdrssc.server.MCDRCommandServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {

    @Unique
    private int mcdrc$sentCommandTree = 0;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        final NodeData nodeData = MCDRCommandServer.getNodeData();
        if (System.identityHashCode(nodeData) != mcdrc$sentCommandTree) {
            CommandNetwork.sendNodeDataToClient((ServerPlayNetworkHandler) (Object) this, nodeData);
            mcdrc$sentCommandTree = System.identityHashCode(nodeData);
        }
    }

}
