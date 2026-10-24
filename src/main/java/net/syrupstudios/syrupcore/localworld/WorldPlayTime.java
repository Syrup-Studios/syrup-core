package net.syrupstudios.syrupcore.localworld;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.LinkOption;
import java.util.Map;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldPlayTime {
    private static final Map<String, OptionalLong> CACHE = new ConcurrentHashMap<>();

    private WorldPlayTime() {}

    public static OptionalLong ticks(String worldId) {
        return CACHE.computeIfAbsent(worldId, WorldPlayTime::readTicks);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static OptionalLong readTicks(String worldId) {
        Path stats = net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath().resolve("saves").resolve(worldId).resolve("stats");
        if (!Files.isDirectory(stats, LinkOption.NOFOLLOW_LINKS)) return OptionalLong.empty();
        long total = 0;
        boolean found = false;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(stats, "*.json")) {
            for (Path file : files) {
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) continue;
                try (Reader reader = Files.newBufferedReader(file)) {
                    JsonElement root = JsonParser.parseReader(reader);
                    if (!root.isJsonObject()) continue;
                    JsonElement statsData = root.getAsJsonObject().get("stats");
                    if (statsData == null || !statsData.isJsonObject()) continue;
                    JsonElement custom = statsData.getAsJsonObject().get("minecraft:custom");
                    if (custom == null || !custom.isJsonObject()) continue;
                    JsonElement playTime = custom.getAsJsonObject().get("minecraft:play_time");
                    if (playTime == null || !playTime.isJsonPrimitive() || !playTime.getAsJsonPrimitive().isNumber()) continue;
                    long value = playTime.getAsLong();
                    if (value < 0) continue;
                    total = Long.MAX_VALUE - total < value ? Long.MAX_VALUE : total + value;
                    found = true;
                } catch (RuntimeException | IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return found ? OptionalLong.of(total) : OptionalLong.empty();
    }

    public static String format(long ticks) {
        return String.format(Locale.ROOT, "%.1fh", ticks / 72000.0);
    }
}
