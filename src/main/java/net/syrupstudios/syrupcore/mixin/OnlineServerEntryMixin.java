package net.syrupstudios.syrupcore.mixin;

import net.syrupstudios.syrupcore.client.FavoriteServerData;
import net.syrupstudios.syrupcore.client.PlayTimeBadge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.syrupstudios.syrupcore.multiplayer.ServerPlayTime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$OnlineServerEntry")
public abstract class OnlineServerEntryMixin {
    @Shadow @Final private JoinMultiplayerScreen screen;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private ServerData serverData;
    @Unique private int syrupCore$rowY;
    @Unique private int syrupCore$rowX;

    @Inject(method = "getNarration", at = @At("RETURN"), cancellable = true)
    private void syrupCore$addFavoriteNarration(CallbackInfoReturnable<Component> cir) {
        String state = ((FavoriteServerData)serverData).isFavorite() ? "Pinned" : "Not pinned. Press P to pin.";
        cir.setReturnValue(CommonComponents.joinForNarration(cir.getReturnValue(), Component.literal(state)));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void syrupCore$renderServerDetails(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick, CallbackInfo ci) {
        syrupCore$rowY = y;
        syrupCore$rowX = x;
        boolean favorite = ((FavoriteServerData)serverData).isFavorite();
        PlayTimeBadge.drawStar(graphics, x - 15, y + 11, favorite);
        if (mouseX >= x - 16 && mouseX < x - 4 && mouseY >= y + 10 && mouseY < y + 22) {
            minecraft.screen.setTooltipForNextRenderPass(Component.literal(favorite ? "Remove from Favorites" : "Add to Favorites"));
        }
        String playTime = ServerPlayTime.format(ServerPlayTime.ticks(serverData.ip));
        var motdLines = minecraft.font.split(serverData.motd, width - 32);
        int badgeRight = x + width - 2;
        int nameRight = x + 35 + minecraft.font.width(serverData.name);
        int inlineBadgeRight = nameRight + 4 + PlayTimeBadge.width(minecraft, playTime);
        int statusWidth = minecraft.font.width(serverData.state() == ServerData.State.INCOMPATIBLE
                ? serverData.version : serverData.status);
        if (inlineBadgeRight <= x + width - 20 - statusWidth - 4) {
            PlayTimeBadge.drawRightAligned(graphics, minecraft, inlineBadgeRight, y + 1, playTime);
            return;
        }
        if (motdLines.size() > 1 && x + 35 + minecraft.font.width(motdLines.get(1)) + 4 > badgeRight - PlayTimeBadge.width(minecraft, playTime)) return;
        PlayTimeBadge.drawRightAligned(graphics, minecraft, badgeRight, y + 25, playTime);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void syrupCore$toggleFavorite(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (mouseX >= syrupCore$rowX - 16 && mouseX < syrupCore$rowX - 4) {
            if (button == 0 && mouseY >= syrupCore$rowY + 10 && mouseY < syrupCore$rowY + 22) {
                FavoriteServerData data = (FavoriteServerData)serverData;
                data.setFavorite(!data.isFavorite());
                screen.getServers().save();
                ((JoinMultiplayerScreenAccessor)screen).syrupCore$refreshServerList();
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

}
