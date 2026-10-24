package net.syrupstudios.syrupcore.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.world.level.storage.LevelSummary;
import net.syrupstudios.syrupcore.localworld.WorldFavorites;
import net.syrupstudios.syrupcore.localworld.WorldPlayTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin {
    @Shadow private Button deleteButton;

    @Inject(method = "init", at = @At("HEAD"))
    private void syrupCore$clearPlayTimeCache(CallbackInfo ci) {
        WorldPlayTime.clearCache();
    }

    @Inject(method = "updateButtonStatus", at = @At("RETURN"))
    private void syrupCore$protectFavoriteFromDelete(LevelSummary summary, CallbackInfo ci) {
        if (summary != null && WorldFavorites.contains(summary.getLevelId())) deleteButton.active = false;
    }
}
