package net.syrupstudios.syrupcore.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.syrupcore.client.ArmorBarRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Gui.class)
public class InGameHudArmorMixin {

    @Unique
    private static final ResourceLocation VANILLA_GUI_ICONS = new ResourceLocation("textures/gui/icons.png");

    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    private int syrupCore$skipVanillaArmor(Player instance) {
        return 0;
    }

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void syrupCore$customRenderArmor(GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        int armorValue = player.getArmorValue();
        if (armorValue <= 0) {
            return;
        }

        int left = guiGraphics.guiWidth() / 2 - 91;
        int top = guiGraphics.guiHeight() - 39;

        int maxHealth = (int) Math.ceil(player.getMaxHealth());
        int absorption = (int) Math.ceil(player.getAbsorptionAmount());
        int healthRows = (int) Math.ceil((maxHealth + absorption) / 20.0);
        int armorTop = top - (healthRows - 1) * 10 - 10;

        ResourceLocation[] points = new ResourceLocation[20];
        Arrays.fill(points, null);

        int pointIndex = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armorItem)) {
                continue;
            }

            String materialName = armorItem.getMaterial().getName();
            ResourceLocation texture = ArmorBarRegistry.getTexture(materialName);
            int protection = armorItem.getDefense();

            for (int p = 0; p < protection && pointIndex < 20; p++) {
                points[pointIndex++] = texture;
            }
        }

        for (int j = 0; j < 10; ++j) {
            int x = left + j * 8;

            guiGraphics.blit(VANILLA_GUI_ICONS, x, armorTop, 16, 9, 9, 9);

            ResourceLocation leftTex = points[j * 2];
            ResourceLocation rightTex = points[j * 2 + 1];

            if (leftTex == rightTex && leftTex != null) {
                guiGraphics.blit(leftTex, x, armorTop, 0, 0, 9, 9, 18, 9);
            } else if (leftTex != null && rightTex == null) {
                guiGraphics.blit(leftTex, x, armorTop, 9, 0, 9, 9, 18, 9);
            } else {
                if (leftTex != null) {
                    guiGraphics.blit(leftTex, x, armorTop, 0, 0, 5, 9, 18, 9);
                }
                if (rightTex != null) {
                    guiGraphics.blit(rightTex, x + 5, armorTop, 5, 0, 4, 9, 18, 9);
                }
            }
        }
    }
}