package net.syrupstudios.syrupcore.mixin;

import net.syrupstudios.syrupcore.client.FavoriteServerData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin {
    @Shadow @Final private JoinMultiplayerScreen screen;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void syrupCore$toggleSelectedFavorite(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (key != 80) return;
        ServerSelectionList.Entry selected = ((ServerSelectionList)(Object)this).getSelected();
        if (selected instanceof OnlineServerEntryAccessor accessor) {
            ServerData data = accessor.syrupCore$getServerData();
            FavoriteServerData favorite = (FavoriteServerData)data;
            favorite.setFavorite(!favorite.isFavorite());
            screen.getServers().save();
            ((JoinMultiplayerScreenAccessor)screen).syrupCore$refreshServerList();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateOnlineServers", at = @At("HEAD"))
    private void syrupCore$sortFavorites(ServerList servers, CallbackInfo ci) {
        List<ServerData> serverList = ((ServerListAccessor)servers).syrupCore$getServerList();
        List<ServerData> sorted = new ArrayList<>(serverList);
        sorted.sort(Comparator.comparing((ServerData server) -> ((FavoriteServerData)server).isFavorite()).reversed());
        if (serverList.equals(sorted)) return;
        for (int i = 0; i < sorted.size(); i++) serverList.set(i, sorted.get(i));
        servers.save();
    }
}
