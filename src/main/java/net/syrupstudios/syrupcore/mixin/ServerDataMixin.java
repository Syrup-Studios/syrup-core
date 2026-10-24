package net.syrupstudios.syrupcore.mixin;

import net.syrupstudios.syrupcore.client.FavoriteServerData;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerData.class)
public abstract class ServerDataMixin implements FavoriteServerData {
    @Unique private boolean syrupCore$favorite;

    @Inject(method = "write", at = @At("RETURN"))
    private void syrupCore$writeFavorite(CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean("SyrupCoreFavorite", syrupCore$favorite);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void syrupCore$readFavorite(CompoundTag tag, CallbackInfoReturnable<ServerData> cir) {
        ((FavoriteServerData)cir.getReturnValue()).setFavorite(tag.getBoolean("SyrupCoreFavorite"));
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void syrupCore$copyFavorite(ServerData source, CallbackInfo ci) {
        syrupCore$favorite = ((FavoriteServerData)source).isFavorite();
    }

    @Unique
    public boolean isFavorite() {
        return syrupCore$favorite;
    }

    @Unique
    public void setFavorite(boolean favorite) {
        syrupCore$favorite = favorite;
    }
}
