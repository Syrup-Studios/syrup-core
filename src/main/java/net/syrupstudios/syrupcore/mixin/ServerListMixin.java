package net.syrupstudios.syrupcore.mixin;

import net.minecraft.client.multiplayer.ServerList;
import net.syrupstudios.syrupcore.client.LegacyFavoriteImporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerList.class)
public abstract class ServerListMixin {
    @Inject(method = "load", at = @At("RETURN"))
    private void syrupCore$saveImportedFavorites(CallbackInfo ci) {
        if (LegacyFavoriteImporter.consumeServerImportSave()) ((ServerList)(Object)this).save();
    }
}
