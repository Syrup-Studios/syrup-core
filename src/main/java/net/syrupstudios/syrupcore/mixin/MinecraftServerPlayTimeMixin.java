package net.syrupstudios.syrupcore.mixin;

import net.minecraft.client.Minecraft;
import net.syrupstudios.syrupcore.multiplayer.ServerPlayTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftServerPlayTimeMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void syrupCore$countServerPlayTime(CallbackInfo ci) {
        ServerPlayTime.tick();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void syrupCore$saveServerPlayTime(CallbackInfo ci) {
        ServerPlayTime.disconnected();
    }
}
