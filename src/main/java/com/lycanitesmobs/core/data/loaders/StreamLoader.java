package com.lycanitesmobs.core.data.loaders;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;

import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class StreamLoader {
    public static StreamLoader CLIENT;
    public static StreamLoader SERVER;
    public static StreamLoader COMMON;

    /**
     * Creates and initializes all file loaders for the provided mod domain.
     *
     * @param domain The mod domain to load from, ex: "lycanitesmobs"
     */
    public static void initAll(String domain) {
        CLIENT = new StreamLoader("assets", domain);
        SERVER = new StreamLoader("data", domain);
        COMMON = new StreamLoader("common", domain);
        LMHelperClass.logDebug("", "All StreamLoaders initialized successfully.");
    }

    private final String rootPath;
    private final String domain;

    /**
     * Constructor
     *
     * @param type   The type of directory to load from, ex: "assets", "data", "common"
     * @param domain The mod domain to load from, ex: "lycanitesmobs"
     */
    public StreamLoader(String type, String domain) {
        this.domain = domain;
        this.rootPath = type + "/" + domain;
    }

    /**
     * Locates the mod jar file.
     *
     * @return The File object representing the mod jar file.
     */
    public File getJarFile() {
        try {
            Path path = Paths.get("mods");
            try (Stream<Path> pathStream = Files.walk(path)) {
                return pathStream
                        .filter(p -> p.getFileName().toString().toLowerCase().contains(this.domain))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Unable to locate mod jar file."))
                        .toFile();
            }
        } catch (Exception e) {
            LMHelperClass.logError("Error locating mod jar file.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an input stream from this Stream Loader for the provided sub path.
     *
     * @param subPath The sub directory or file path to load, ex: "creatures/grue.json", "globalspawning.json"
     * @return InputStream for the specified sub path
     */
    public InputStream getStream(String subPath) {
        return this.getClass().getResourceAsStream("/" + this.rootPath + "/" + subPath);
    }

    /**
     * Creates a list of input streams from this Stream Loader for the provided sub directory.
     *
     * @param dir The directory to stream files from, ex: "creatures", "textures/blocks"
     * @return List of InputStreams for the specified directory
     */
    public List<InputStream> getStreams(String dir) {
        List<InputStream> inputStreams = new ArrayList<>();
        try (JarFile jar = new JarFile(getJarFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(this.rootPath + "/" + dir + "/") && !name.endsWith("/")) {
                    InputStream inputStream = this.getClass().getResourceAsStream("/" + name);
                    if (inputStream == null) {
                        throw new RuntimeException("Error streaming file from mod jar: /" + name);
                    }
                    inputStreams.add(inputStream);
                }
            }
        } catch (Exception e) {
            LMHelperClass.logError("Error reading files within jar file for dir: " + dir);
            throw new RuntimeException(e);
        }
        return inputStreams;
    }
}
