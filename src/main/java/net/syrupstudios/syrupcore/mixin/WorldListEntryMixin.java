package net.syrupstudios.syrupcore.mixin;

import net.syrupstudios.syrupcore.client.WorldListEntryAccess;
import net.syrupstudios.syrupcore.client.WorldSelectionListAccess;
import net.syrupstudios.syrupcore.client.PlayTimeBadge;

import java.util.OptionalLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import net.syrupstudios.syrupcore.localworld.WorldFavorites;
import net.syrupstudios.syrupcore.localworld.WorldPlayTime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin implements WorldListEntryAccess {
    @Unique private int syrupCore$rowX;
    @Unique private int syrupCore$rowY;

    @Shadow @Final private LevelSummary summary;
    @Shadow @Final private WorldSelectionList field_19135;
    @Shadow @Final private Minecraft minecraft;

    @Override
    public String syrupCore$getWorldId() { return summary.getLevelId(); }

    @org.spongepowered.asm.mixin.injection.Inject(method = "getNarration", at = @At("RETURN"), cancellable = true)
    private void syrupCore$addFavoriteNarration(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(CommonComponents.joinForNarration(cir.getReturnValue(), Component.literal(WorldFavorites.contains(summary.getLevelId()) ? "Pinned" : "Not pinned. Press P to pin.")));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void syrupCore$renderWorldDetails(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick, CallbackInfo ci) {
        syrupCore$rowX = x;
        syrupCore$rowY = y;
        boolean favorite = WorldFavorites.contains(summary.getLevelId());
        PlayTimeBadge.drawStar(graphics, x - 15, y + 11, favorite);
        if (mouseX >= x - 16 && mouseX < x - 4 && mouseY >= y + 10 && mouseY < y + 22) {
            minecraft.screen.setTooltipForNextRenderPass(Component.literal(favorite ? "Remove from Favorites" : "Add to Favorites"));
        }
        OptionalLong ticks = WorldPlayTime.ticks(summary.getLevelId());
        PlayTimeBadge.drawRightAligned(graphics, minecraft, x + width - 2, y + 1,
                WorldPlayTime.format(ticks.orElse(0)));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void syrupCore$toggleFavorite(double mouseX, double mouseY, int button, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (mouseX >= syrupCore$rowX - 16 && mouseX < syrupCore$rowX - 4) {
            if (button == 0 && mouseY >= syrupCore$rowY + 10 && mouseY < syrupCore$rowY + 22) {
                WorldFavorites.toggle(summary.getLevelId());
                ((WorldSelectionListAccess)field_19135).syrupCore$refreshFavorites();
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "deleteWorld", at = @At("HEAD"), cancellable = true)
    private void syrupCore$preventFavoriteDelete(CallbackInfo ci) {
        if (WorldFavorites.contains(summary.getLevelId())) ci.cancel();
    }
}
