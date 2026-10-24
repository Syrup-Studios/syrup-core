package net.syrupstudios.syrupcore.mixin;

import net.syrupstudios.syrupcore.client.WorldListEntryAccess;
import net.syrupstudios.syrupcore.client.WorldSelectionListAccess;

import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import net.syrupstudios.syrupcore.localworld.WorldFavorites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSelectionList.class)
public abstract class WorldSelectionListMixin implements WorldSelectionListAccess {
    @org.spongepowered.asm.mixin.Shadow private String filter;
    @org.spongepowered.asm.mixin.Shadow private List<LevelSummary> currentlyDisplayedLevels;
    @org.spongepowered.asm.mixin.gen.Invoker("fillLevels") protected abstract void syrupCore$fillLevels(String filter, List<LevelSummary> levels);

    @Override
    public void syrupCore$refreshFavorites() {
        if (currentlyDisplayedLevels != null) syrupCore$fillLevels(filter, currentlyDisplayedLevels);
    }

    @ModifyVariable(method = "fillLevels", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private List<LevelSummary> syrupCore$sortFavorites(List<LevelSummary> levels) {
        return levels.stream().sorted(Comparator.comparing((LevelSummary level) -> WorldFavorites.contains(level.getLevelId())).reversed()).toList();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void syrupCore$toggleSelectedFavorite(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (key == 80) {
            ((WorldSelectionList)(Object)this).getSelectedOpt().ifPresent(entry -> {
                WorldFavorites.toggle(((WorldListEntryAccess)(Object)entry).syrupCore$getWorldId());
                syrupCore$refreshFavorites();
            });
            cir.setReturnValue(true);
        }
    }

}
