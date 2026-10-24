package net.syrupstudios.syrupcore.mixin;

import net.syrupstudios.syrupcore.client.FavoriteServerData;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {
    @Shadow private Button deleteButton;
    @Shadow private ServerSelectionList serverSelectionList;

    @Inject(method = "onSelectedChange", at = @At("TAIL"))
    private void syrupCore$disableFavoriteDeletion(CallbackInfo ci) {
        ServerSelectionList.Entry selected = serverSelectionList.getSelected();
        deleteButton.active = selected instanceof OnlineServerEntryAccessor accessor
                && !((FavoriteServerData)accessor.syrupCore$getServerData()).isFavorite();
    }

    @Inject(method = "deleteCallback", at = @At("HEAD"), cancellable = true)
    private void syrupCore$preventFavoriteDeletion(boolean confirmed, CallbackInfo ci) {
        if (!confirmed) return;
        ServerSelectionList.Entry selected = serverSelectionList.getSelected();
        if (selected instanceof OnlineServerEntryAccessor accessor) {
            ServerData server = accessor.syrupCore$getServerData();
            if (((FavoriteServerData)server).isFavorite()) ci.cancel();
        }
    }
}
