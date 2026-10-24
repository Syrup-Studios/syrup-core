package net.syrupstudios.syrupcore.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.syrupstudios.syrupcore.SyrupCore;

public final class LegacyFavoriteImporter {
    private static final Path FILE = Minecraft.getInstance().gameDirectory.toPath().resolve("cherishedworlds-favorites.dat");
    private static final Optional<Set<String>> LEGACY_FAVORITES = loadLegacyFavorites();
    private static boolean serverImportNeedsSave;

    private LegacyFavoriteImporter() {}

    public static boolean importServerFavorite(CompoundTag tag, ServerData server) {
        if (tag.contains("SyrupCoreFavorite", Tag.TAG_BYTE)) return tag.getBoolean("SyrupCoreFavorite");
        boolean favorite = LEGACY_FAVORITES.map(favorites -> favorites.contains(server.name + server.ip)).orElse(false);
        serverImportNeedsSave |= favorite;
        return favorite;
    }

    public static boolean consumeServerImportSave() {
        boolean save = serverImportNeedsSave;
        serverImportNeedsSave = false;
        return save;
    }

    public static Optional<Set<String>> getLegacyFavorites() {
        return LEGACY_FAVORITES;
    }

    private static Optional<Set<String>> loadLegacyFavorites() {
        try {
            if (Files.notExists(FILE)) return Optional.empty();
            CompoundTag legacy = NbtIo.read(FILE);
            if (legacy == null || !(legacy.get("favorites") instanceof ListTag entries)
                    || (entries.getElementType() != 0 && entries.getElementType() != Tag.TAG_STRING)) return Optional.empty();
            Set<String> favorites = new HashSet<>();
            for (int i = 0; i < entries.size(); i++) favorites.add(entries.getString(i));
            return Optional.of(Set.copyOf(favorites));
        } catch (Exception exception) {
            SyrupCore.LOGGER.error("Could not read Cherished Worlds favorites", exception);
            return Optional.empty();
        }
    }
}
