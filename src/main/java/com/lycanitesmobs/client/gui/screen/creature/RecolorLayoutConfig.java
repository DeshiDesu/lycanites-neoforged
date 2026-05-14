package com.lycanitesmobs.client.gui.screen.creature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class RecolorLayoutConfig {

    public static class Group {
        public int x;
        public int y;
        public int width;
        public int height;
    }

    public Group left = new Group();
    public Group right = new Group();

    private static RecolorLayoutConfig INSTANCE;

    public static RecolorLayoutConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        Path dir = FMLPaths.CONFIGDIR.get();
        Path path = dir.resolve("lycanitesmobs/lycanitesmobs-recolor-layout.json");
        Gson gson = new Gson();

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                INSTANCE = gson.fromJson(reader, RecolorLayoutConfig.class);
            } catch (IOException e) {
                INSTANCE = new RecolorLayoutConfig();
            }
        } else {
            INSTANCE = new RecolorLayoutConfig();
        }
    }

    public static void save() {
        if (INSTANCE == null) return;

        Path dir = FMLPaths.CONFIGDIR.get();
        Path path = dir.resolve("lycanitesmobs/lycanitesmobs-recolor-layout.json");

        try {
            Files.createDirectories(dir);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                gson.toJson(INSTANCE, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
