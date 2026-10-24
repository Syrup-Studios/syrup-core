package net.syrupstudios.syrupcore.mixin;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import net.syrupstudios.syrupcore.multiplayer.ServerPlayTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientPacketListenerServerPlayTimeMixin {
    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void syrupCore$saveServerPlayTime(DisconnectionDetails reason, CallbackInfo ci) {
        ServerPlayTime.disconnected();
    }
}
