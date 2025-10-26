package icu.suc.realinvisibility;

import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class Config {
    public static <T> @NotNull Pair<Data, Settings<T>> loadConfig(Path dir, Settings.Slots<T> function) {
        var ip = loadConfig(dir, "data.properties");
        var sp = loadConfig(dir, "settings.properties");

        return new Pair<>(
                new Data(
                        Integer.parseInt(ip.getProperty("DATA_EFFECT_PARTICLES")),
                        Integer.parseInt(ip.getProperty("DATA_ARROW_COUNT_ID")),
                        Integer.parseInt(ip.getProperty("DATA_STINGER_COUNT_ID")),
                        Integer.parseInt(ip.getProperty("DATA_SHARED_FLAGS_ID")),
                        Byte.parseByte(ip.getProperty("BIT_MAP_FIRE"))
                ),
                new Settings<>(
                        Boolean.parseBoolean(sp.getProperty("mainhand", "true")),
                        Boolean.parseBoolean(sp.getProperty("offhand", "true")),
                        Boolean.parseBoolean(sp.getProperty("boots", "true")),
                        Boolean.parseBoolean(sp.getProperty("leggings", "true")),
                        Boolean.parseBoolean(sp.getProperty("chestplate", "true")),
                        Boolean.parseBoolean(sp.getProperty("helmet", "true")),
                        Boolean.parseBoolean(sp.getProperty("body", "true")),
                        Boolean.parseBoolean(sp.getProperty("particles", "true")),
                        Boolean.parseBoolean(sp.getProperty("arrows", "true")),
                        Boolean.parseBoolean(sp.getProperty("stingers", "true")),
                        Boolean.parseBoolean(sp.getProperty("fire", "true"))
                ) {
                    @Override
                    protected Set<T> slots(boolean mainhand, boolean offhand, boolean boots, boolean leggings, boolean chestplate, boolean helmet, boolean body) {
                        return function.slots(mainhand, offhand, boots, leggings, chestplate, helmet, body);
                    }
                }
        );
    }

    private static @NotNull Properties loadConfig(@NotNull Path dir, String file) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        var path = dir.resolve(file);

        if (!Files.exists(path)) {
            try (var input = Config.class.getClassLoader().getResourceAsStream(file)) {
                Files.copy(Objects.requireNonNull(input), path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        var properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }
}
