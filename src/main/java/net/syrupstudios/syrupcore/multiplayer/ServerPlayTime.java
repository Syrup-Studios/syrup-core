package net.syrupstudios.syrupcore.multiplayer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.syrupstudios.syrupcore.SyrupCore;

public final class ServerPlayTime {
    private static final Path FILE = Minecraft.getInstance().gameDirectory.toPath().resolve("config/syrup-core-server-playtime.json");
    private static final Path LEGACY_FILE = Minecraft.getInstance().gameDirectory.toPath().resolve("data/servers_playtime.dat");
    private static final Path MIGRATION_MARKER = FILE.resolveSibling("syrup-core-server-playtime-migrated");
    private static final Map<String, Long> TIMES = load();
    private static String activeAddress;
    private static long activeTicks;
    private static long ticksSinceSave;

    private ServerPlayTime() {}

    public static synchronized long ticks(String address) {
        return TIMES.getOrDefault(address, 0L);
    }

    public static synchronized void tick() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        if (server == null || Minecraft.getInstance().getConnection() == null) return;
        if (!server.ip.equals(activeAddress)) {
            saveActive();
            activeAddress = server.ip;
            activeTicks = 0;
        }
        activeTicks++;
        TIMES.merge(activeAddress, 1L, (total, increment) -> total == Long.MAX_VALUE ? total : total + increment);
        if (++ticksSinceSave >= 6000) {
            saveActive();
            ticksSinceSave = 0;
        }
    }

    public static synchronized void disconnected() {
        saveActive();
        activeAddress = null;
        activeTicks = 0;
        ticksSinceSave = 0;
    }

    public static String format(long ticks) {
        return String.format(Locale.ROOT, "%.1fh", ticks / 72000.0);
    }

    private static Map<String, Long> load() {
        Map<String, Long> times = new HashMap<>();
        boolean nativeDataValid = true;
        if (Files.isRegularFile(FILE)) {
            try (var reader = Files.newBufferedReader(FILE)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                root.entrySet().forEach(entry -> {
                    try {
                        long value = entry.getValue().getAsLong();
                        if (value >= 0) times.put(entry.getKey(), value);
                    } catch (RuntimeException ignored) {
                    }
                });
            } catch (IOException | RuntimeException exception) {
                nativeDataValid = false;
                SyrupCore.LOGGER.error("Could not read server play time", exception);
            }
        }
        migrateLegacy(times, nativeDataValid);
        return times;
    }

    private static void migrateLegacy(Map<String, Long> times, boolean nativeDataValid) {
        if (!nativeDataValid || Files.exists(MIGRATION_MARKER) || !Files.isRegularFile(LEGACY_FILE)) return;
        try {
            CompoundTag legacy = NbtIo.read(LEGACY_FILE);
            if (legacy == null) return;
            for (String address : legacy.getAllKeys()) {
                int ticks = legacy.getInt(address);
                if (ticks > 0) times.merge(address, (long) ticks, Math::max);
            }
            if (!saveTimes(times)) return;
            Files.writeString(MIGRATION_MARKER, "Imported WorldPlayTime server play time.");
        } catch (IOException | RuntimeException exception) {
            SyrupCore.LOGGER.error("Could not import WorldPlayTime server play time", exception);
        }
    }

    private static boolean saveTimes(Map<String, Long> times) {
        Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        JsonObject root = new JsonObject();
        times.forEach(root::addProperty);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(temporary, root.toString());
            try {
                Files.move(temporary, FILE, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException exception) {
            SyrupCore.LOGGER.error("Could not save server play time", exception);
            return false;
        }
    }

    private static void saveActive() {
        if (activeAddress == null || activeTicks == 0) return;
        if (saveTimes(TIMES)) activeTicks = 0;
    }
}
