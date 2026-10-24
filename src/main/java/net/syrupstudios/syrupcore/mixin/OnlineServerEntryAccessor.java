package net.syrupstudios.syrupcore.mixin;

import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$OnlineServerEntry")
public interface OnlineServerEntryAccessor {
    @Accessor("serverData")
    ServerData syrupCore$getServerData();
}
