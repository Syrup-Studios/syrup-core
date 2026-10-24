package net.syrupstudios.syrupcore.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {
    @Shadow protected abstract int getRowLeft();

    @ModifyArg(
            method = "mouseClicked",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractSelectionList;getEntryAtPosition(DD)Lnet/minecraft/client/gui/components/AbstractSelectionList$Entry;"),
            index = 0)
    private double syrupCore$routeWorldStarClick(double mouseX) {
        if (((Object)this instanceof WorldSelectionList || (Object)this instanceof ServerSelectionList)
                && mouseX >= getRowLeft() - 16 && mouseX < getRowLeft() - 4) {
            return getRowLeft();
        }
        return mouseX;
    }
}
