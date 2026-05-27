package net.syrupstudios.syrupcore.client;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public class ArmorBarRegistry {
    public static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("syrup-core", "textures/gui/armor/iron.png");
    private static final Map<String, ResourceLocation> ARMOR_TEXTURES = new HashMap<>();

    static {
        String[] vanillaMaterials = {"leather", "chainmail", "iron", "gold", "diamond", "netherite", "turtle"};
        for (String material : vanillaMaterials) {
            ARMOR_TEXTURES.put(material, new ResourceLocation("syrup-core", "textures/gui/armor/" + material + ".png"));
        }
    }


    public static void registerModdedArmor(String materialName, ResourceLocation textureLocation) {
        ARMOR_TEXTURES.put(materialName, textureLocation);
    }

    public static ResourceLocation getTexture(String materialName) {
        return ARMOR_TEXTURES.getOrDefault(materialName, FALLBACK_TEXTURE);
    }
}