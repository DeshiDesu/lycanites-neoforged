package com.lycanitesmobs.core.data.loaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class JSONLoader {
    public File getMinecraftDir() {
        return new File(".");
    }

    /**
     * Loads all JSON files into this manager. Should only be done on pre-init.
     *
     * @param modInfo      The group that this manager should load from.
     * @param loadGroup    The name of the group of JSON files being loaded, used both for debug output and for passing to parseJson for loading multiple sets of json files.
     * @param dataPath     The path to load json files from relative to the group data folder and the config folder.
     * @param mapKey       The json value to use as the map key, usually the "name" field.
     * @param loadCustom   If true, additional custom json files will also be loaded from the config directory for adding custom entries.
     * @param jsonType     If set, a "type" value is checked in the JSON and must match.
     * @param fileLoader   The file loader to get paths from.
     * @param streamLoader The stream loader to get jar files from.
     */
    public void loadAllJson(ModInfo modInfo, String loadGroup, String dataPath, String mapKey, boolean loadCustom, @Nullable String jsonType, FileLoader fileLoader, StreamLoader streamLoader) {
        LMHelperClass.logDebug(loadGroup, "Loading JSON " + loadGroup + "...");
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        Map<String, JsonObject> jsons = new HashMap<>();

        // Load Default:
        Map<String, JsonObject> defaultJsons = new HashMap<>();
        if (fileLoader.ready) {
            Path path = fileLoader.getPath(dataPath);
            this.loadJsonObjects(gson, path, defaultJsons, mapKey, jsonType);
        } else {
            this.loadJsonObjects(gson, streamLoader.getStreams(dataPath), defaultJsons, mapKey, jsonType);
        }

        // Load Custom:
        String configPath = this.getMinecraftDir() + "/config/" + LycanitesMobs.MODID + "/";
        File customDir = new File(configPath + dataPath);
        customDir.mkdirs();
        Path customPath = customDir.toPath();
        Map<String, JsonObject> customJsons = new HashMap<>();
        this.loadJsonObjects(gson, customPath, customJsons, mapKey, jsonType);


        // Write Defaults:
        this.writeDefaultJSONObjects(gson, defaultJsons, customJsons, jsons, loadCustom, dataPath);


        // Parse Json:
        LMHelperClass.logDebug(loadGroup, "Loading " + jsons.size() + " " + loadGroup + "...");
        for (String jsonName : jsons.keySet()) {
            JsonObject json;
            try {
                json = jsons.get(jsonName);
                LMHelperClass.logDebug(loadGroup, "Loading " + loadGroup + " JSON: " + json);
                if (json.isJsonNull())
                    throw new RuntimeException("Tried to load JSON data from a null json object.");
            } catch (JsonParseException e) {
                LMHelperClass.logError("Parsing error loading JSON " + loadGroup + ": " + jsonName);
                throw new RuntimeException(e);
            } catch (Exception e) {
                LMHelperClass.logError("There was a problem loading JSON " + loadGroup + ": " + jsonName);
                throw new RuntimeException(e);
            }
            this.parseJson(modInfo, loadGroup, json);
        }
    }


    /**
     * Reads a JSON object and adds it to this JSON Loader.
     *
     * @param modInfo   The Mod Info to load with.
     * @param loadGroup The Json group name.
     * @param json      The Json Object to read.
     */
    public abstract void parseJson(ModInfo modInfo, String loadGroup, JsonObject json);


    /**
     * Loads JSON objects from the specified input stream with additional options.
     *
     * @param gson          The JSON parser.
     * @param inputStreams  The stream to load from.
     * @param jsonObjectMap The map to add the loaded JSON to.
     * @param mapKey        The JSON value to use as the map key.
     * @param jsonType      If set, a "type" value is checked in the JSON and must match.
     */
    public void loadJsonObjects(Gson gson, List<InputStream> inputStreams, Map<String, JsonObject> jsonObjectMap, String mapKey, String jsonType) {
        for (InputStream inputStream : inputStreams) {
            JsonObject json = this.loadJsonObject(gson, inputStream);
            boolean validJSON = true;
            if (jsonType != null) {
                if (!json.has("type")) {
                    validJSON = false;
                } else {
                    validJSON = jsonType.equalsIgnoreCase(json.get("type").getAsString());
                }
            }
            if (validJSON) {
                jsonObjectMap.put(json.get(mapKey).getAsString(), json);
            }
        }
    }

    /**
     * Loads a JSON object from the specified input stream with additional options.
     *
     * @param gson        The JSON parser.
     * @param inputStream The input stream to load from.
     * @return An instance of the json object.
     */
    public JsonObject loadJsonObject(Gson gson, InputStream inputStream) {
        try {
            JsonObject json;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                json = GsonHelper.fromJson(gson, reader, JsonObject.class, false);
            } finally {
                IOUtils.closeQuietly(reader);
            }
            if (json.isJsonNull()) {
                throw new RuntimeException("Unable to load JSON from Input Stream: " + inputStream);
            }
            inputStream.close();
            return json;
        } catch (Exception e) {
            LMHelperClass.logWarning("", "Unable to read file from path.\n" + e.toString());
        }
        return null;
    }


    /**
     * Loads JSON objects from the specified path with additional options.
     *
     * @param gson          The JSON parser.
     * @param path          The path to load from.
     * @param jsonObjectMap The map to add the loaded JSON to.
     * @param mapKey        The JSON value to use as the map key.
     * @param jsonType      If set, a "type" value is checked in the JSON and must match.
     */
    public void loadJsonObjects(Gson gson, Path path, Map<String, JsonObject> jsonObjectMap, String mapKey, String jsonType) {
        if (path == null) {
            return;
        }
        try {
            Iterator<Path> iterator = Files.walk(path).iterator();
            while (iterator.hasNext()) {
                Path filePath = iterator.next();
                Path relativePath = path.relativize(filePath);
                if (!"json".equals(FilenameUtils.getExtension(filePath.toString()))) {
                    continue;
                }
                BufferedReader reader = null;
                try {
                    try {
                        reader = Files.newBufferedReader(filePath);
                        JsonObject json = GsonHelper.fromJson(gson, reader, JsonObject.class, false);
                        boolean validJSON = true;
                        if (jsonType != null) {
                            if (!json.has("type")) {
                                validJSON = false;
                            } else {
                                validJSON = jsonType.equalsIgnoreCase(json.get("type").getAsString());
                            }
                        }
                        if (validJSON) {
                            jsonObjectMap.put(json.get(mapKey).getAsString(), json);
                        }
                    } catch (JsonParseException e) {
                        LMHelperClass.logWarning("", "Parsing error loading JSON " + relativePath + "\n" + e.toString());
                        e.printStackTrace();
                    } catch (Exception e) {
                        LMHelperClass.logWarning("", "There was a problem loading JSON " + relativePath + "\n" + e.toString());
                        e.printStackTrace();
                    }
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("", "Unable to read files from directory.\n" + e.toString());
            //e.printStackTrace();
        }
    }

    /**
     * Loads a JSON object from the specified path with additional options.
     *
     * @param gson The JSON parser.
     * @param path The path to load from.
     * @return An instance of the json object.
     */
    public JsonObject loadJsonObject(Gson gson, Path path) {
        if (path == null) {
            return null;
        }
        try {
            BufferedReader reader = null;
            try {
                try {
                    reader = Files.newBufferedReader(path);
                    JsonObject json = GsonHelper.fromJson(gson, reader, JsonObject.class, false);
                    return json;
                } catch (JsonParseException e) {
                    LMHelperClass.logWarning("", "Parsing error loading JSON from path: " + path + "\n" + e.toString());
                    e.printStackTrace();
                } catch (Exception e) {
                    LMHelperClass.logWarning("", "There was a problem loading JSON from path: " + path + "\n" + e.toString());
                    e.printStackTrace();
                }
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (Exception e) {
            LMHelperClass.logWarning("", "Unable to read file from path.\n" + e.toString());
        }
        return null;
    }


    /**
     * Cycles through both maps of JSON Objects, a default and a custom map and determines if the defaults should overwrite the custom JSON. Puts the chosen JSON into the mixed map.
     **/
    public void writeDefaultJSONObjects(Gson gson, Map<String, JsonObject> defaultJSONs, Map<String, JsonObject> customJSONs, Map<String, JsonObject> mixedJSONs, boolean custom, String dataPath) {
        // Add Default/Overridden JSON:
        for (String jsonName : defaultJSONs.keySet()) {
            try {
                JsonObject defaultJSON = defaultJSONs.get(jsonName);
                boolean loadDefault = true;

                // If Custom Replacement Exists:
                JsonObject customJSON = null;
                if (customJSONs.containsKey(jsonName)) {
                    loadDefault = false;
                    customJSON = customJSONs.get(jsonName);
                    if (customJSON.has("loadDefault")) {
                        loadDefault = customJSON.get("loadDefault").getAsBoolean();
                    }
                }

                // Write Default:
                if (loadDefault) {
                    this.saveJsonObject(gson, defaultJSON, jsonName, dataPath);
                    mixedJSONs.put(jsonName, defaultJSON);
                } else if (customJSON != null) {
                    mixedJSONs.put(jsonName, customJSON);
                }
            } catch (JsonParseException e) {
                LMHelperClass.logWarning("", "Parsing error loading JSON: " + jsonName);
                e.printStackTrace();
            } catch (Exception e) {
                LMHelperClass.logWarning("", "There was a problem loading JSON: " + jsonName);
                e.printStackTrace();
            }
        }

        // Add Custom JSON:
        if (custom) {
            for (String jsonName : customJSONs.keySet()) {
                if (!defaultJSONs.containsKey(jsonName)) {
                    mixedJSONs.put(jsonName, customJSONs.get(jsonName));
                }
            }
        }
    }


    /**
     * Compares two json objects a default and a custom and determines if the defaults should overwrite the custom JSON. Returns the chosen JSON.
     **/
    public JsonObject writeDefaultJSONObject(Gson gson, String jsonName, JsonObject defaultJSON, JsonObject customJSON) {
        // Add Default/Overridden JSON:
        try {
            boolean loadDefault = true;

            // If Custom Replacement Exists:
            if (customJSON != null) {
                loadDefault = false;
                if (customJSON.has("loadDefault")) {
                    loadDefault = customJSON.get("loadDefault").getAsBoolean();
                }
            }

            // Write Default:
            if (loadDefault) {
                this.saveJsonObject(gson, defaultJSON, jsonName, "");
                return defaultJSON;
            } else if (customJSON != null) {
                return customJSON;
            }
        } catch (JsonParseException e) {
            LMHelperClass.logWarning("", "Parsing error loading JSON: " + jsonName);
            throw new RuntimeException("Error Parsing JSON: " + jsonName + "\n" + e);
        } catch (Exception e) {
            LMHelperClass.logWarning("", "There was a problem loading JSON: " + jsonName);
            throw new RuntimeException("Error Loading JSON: " + jsonName + "\n" + e);
        }
        return null;
    }


    /**
     * Saves a JSON object into the config folder.
     **/
    public void saveJsonObject(Gson gson, JsonObject jsonObject, String name, String dataPath) {
        String configPath = this.getMinecraftDir() + "/config/" + LycanitesMobs.MODID + "/";
        try {
            File jsonFile = new File(configPath + (!"".equals(dataPath) ? dataPath + "/" : "") + name + ".json");
            jsonFile.getParentFile().mkdirs();
            jsonFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(jsonFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(gson.toJson(jsonObject));
            outputStreamWriter.close();
            outputStream.close();
        } catch (Exception e) {
            LMHelperClass.logWarning("", "Unable to save JSON into the config folder.");
            e.printStackTrace();
        }
    }
}
