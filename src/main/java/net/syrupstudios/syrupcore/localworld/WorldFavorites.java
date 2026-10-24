package net.syrupstudios.syrupcore.localworld;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.syrupstudios.syrupcore.client.LegacyFavoriteImporter;
import net.syrupstudios.syrupcore.SyrupCore;

public final class WorldFavorites {
    private static final Path FILE = Minecraft.getInstance().gameDirectory.toPath().resolve("config/syrup-core-favorites.txt");
    private static boolean loadSucceeded;
    private static final Set<String> FAVORITES = load();
    private static final Path MIGRATION_MARKER = FILE.resolveSibling("syrup-core-favorites-migrated");

    static {
        importLegacyFavorites();
    }

    private WorldFavorites() {}

    public static synchronized boolean contains(String worldId) {
        return FAVORITES.contains(worldId);
    }

    public static synchronized boolean toggle(String worldId) {
        if (!loadSucceeded) return false;
        boolean added = FAVORITES.add(worldId);
        if (!added) FAVORITES.remove(worldId);
        if (!save()) {
            if (added) FAVORITES.remove(worldId);
            else FAVORITES.add(worldId);
        }
        return added;
    }

    private static Set<String> load() {
        try {
            if (Files.notExists(FILE)) {
                loadSucceeded = true;
                return new HashSet<>();
            }
            Set<String> favorites = new HashSet<>(Files.readAllLines(FILE, StandardCharsets.UTF_8));
            loadSucceeded = true;
            return favorites;
        } catch (IOException exception) {
            SyrupCore.LOGGER.error("Could not read world favorites", exception);
            return new HashSet<>();
        }
    }

    private static boolean save() {
        Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(FILE.getParent());
            Files.write(temporary, FAVORITES, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, FILE, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException exception) {
            SyrupCore.LOGGER.error("Could not save world favorites", exception);
            return false;
        }
    }

    private static void importLegacyFavorites() {
        if (!loadSucceeded || Files.exists(MIGRATION_MARKER)) return;
        Set<String> legacyFavorites = LegacyFavoriteImporter.getLegacyFavorites().orElse(null);
        if (legacyFavorites == null) return;
        try {
            FAVORITES.addAll(legacyFavorites);
            if (!save()) return;
            Files.createDirectories(MIGRATION_MARKER.getParent());
            Files.createFile(MIGRATION_MARKER);
        } catch (Exception exception) {
            SyrupCore.LOGGER.error("Could not import Cherished Worlds favorites", exception);
        }
    }
}
