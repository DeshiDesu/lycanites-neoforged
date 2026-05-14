package com.lycanitesmobs.core.entity.pets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerFamiliars {
    public static PlayerFamiliars INSTANCE = new PlayerFamiliars();
    public Map<UUID, Map<UUID, PetEntry>> playerFamiliars = new ConcurrentHashMap<>();
    public Map<UUID, Long> playerFamiliarLoadedTimes = new ConcurrentHashMap<>();
    public List<String> familiarBlacklist = new ArrayList<>();
    public java.util.Set<UUID> familiarLoadsInFlight = ConcurrentHashMap.newKeySet();
    public java.util.Set<UUID> pendingFamiliars = ConcurrentHashMap.newKeySet();
    public SSLContext sslContext;

    public PlayerFamiliars() {
        // Trust Self Signed SSL Certificates:
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            X509Certificate[] certs, String authType) {
                    }
                }
        };
        try {
            this.sslContext = SSLContext.getInstance("SSL");
            this.sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FamiliarLoader implements Runnable {
        UUID playerUUID;

        public FamiliarLoader(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public void run() {
            try {
                this.loadForPlayerUUID(this.playerUUID);
            } finally {
                PlayerFamiliars.INSTANCE.familiarLoadsInFlight.remove(this.playerUUID);
            }
        }

        private void loadForPlayerUUID(UUID uuid) {
            String jsonString;
            try {
                URL url = new URL(LycanitesMobs.serviceAPI + "/familiars?minecraft_uuid=" + uuid.toString());
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setSSLSocketFactory(PlayerFamiliars.INSTANCE.sslContext.getSocketFactory());
                urlConnection.setRequestProperty("Authorization", "Bearer 7ed1f44cbc1aff693e604075f23d56402983a4a0"); // This is a public api so the key is safe to be exposed like this. :)
                String osName = System.getProperty("os.name");
                urlConnection.setRequestProperty("User-Agent", "Minecraft " + LycanitesMobs.versionMC + " (" + osName + ") LycanitesMobs " + LycanitesMobs.versionNumber);
                InputStream inputStream = urlConnection.getInputStream();
                try {
                    jsonString = IOUtils.toString(inputStream, (Charset) null);
                } catch (Exception e) {
                    throw e;
                } finally {
                    inputStream.close();
                }
                LMHelperClass.logInfo("", "Online familiars loaded successfully for " + uuid + ".");
            } catch (Throwable e) {
                LMHelperClass.logInfo("", "Unable to access the online familiars service.");
                e.printStackTrace();
                return;
            }

            // Parse JSON File:
            PlayerFamiliars.INSTANCE.parseFamiliarJSON(uuid, jsonString);
            PlayerFamiliars.INSTANCE.pendingFamiliars.add(uuid);
        }
    }

    // Parses JSON to Familiars, returns false if the JSON is invalid.
    public void parseFamiliarJSON(UUID playerUUID, String jsonString) {
        Map<UUID, PetEntry> parsedEntries = new HashMap<>();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse(jsonString).getAsJsonObject();
            JsonArray jsonArray = json.getAsJsonArray("data");
            for (JsonElement jsonElement : jsonArray) {
                try {
                    // Familiar UUIDs:
                    JsonObject familiarJson = jsonElement.getAsJsonObject();
                    UUID minecraft_uuid = UUID.fromString(familiarJson.get("minecraft_uuid").getAsString());
                    if (!playerUUID.equals(minecraft_uuid)) {
                        continue;
                    }
                    if (this.familiarBlacklist.contains(minecraft_uuid.toString())) {
                        continue;
                    }
                    UUID familiar_uuid = UUID.fromString(familiarJson.get("familiar_uuid").getAsString());

                    // Familiar Properties:
                    String familiar_species = familiarJson.get("familiar_species").getAsString();
                    CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(familiar_species);
                    int familiar_subspecies = familiarJson.get("familiar_subspecies").getAsInt();
                    int familiar_variant = familiarJson.get("familiar_variant").getAsInt();
                    Variant creatureVariant = creatureInfo != null ? creatureInfo.getSubspecies(familiar_subspecies).getVariant(familiar_variant) : null;
                    String familiar_name = familiarJson.get("familiar_name").getAsString();
                    String familiar_color = familiarJson.get("familiar_color").getAsString();
                    double familiar_size = familiarJson.get("familiar_size").getAsDouble();
                    if (familiar_size <= 0) { // Default Reduced Size:
                        familiar_size = 0.5D;
                        if (creatureVariant != null && creatureVariant.scale != 1) {
                            familiar_size *= 1 / creatureVariant.scale;
                        }
                    }

                    // Create Familiar Pet Entry:
                    PetEntryFamiliar familiarEntry = new PetEntryFamiliar(familiar_uuid, null, familiar_species.toLowerCase());
                    familiarEntry.setEntitySubspecies(familiar_subspecies);
                    familiarEntry.setEntityVariant(familiar_variant);
                    familiarEntry.setEntitySize(familiar_size);
                    if (!"".equals(familiar_name)) {
                        familiarEntry.setEntityName(familiar_name);
                    }
                    familiarEntry.setColor(familiar_color);
                    parsedEntries.put(familiar_uuid, familiarEntry);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            /*LycanitesMobs.logWarning("", "A problem occurred when loading online player familiars:");
            e.printStackTrace();*/
        }
        this.playerFamiliars.put(playerUUID, parsedEntries);
    }

    public Map<UUID, PetEntry> getFamiliarsForPlayer(Player player) {
        UUID playerUUID = player.getUUID();
        long currentTime = System.currentTimeMillis() / 1000;
        long loadedTime = this.getPlayerFamiliarLoadedTime(player);
        if ((loadedTime < 0 || currentTime - loadedTime > 30 * 60) && this.familiarLoadsInFlight.add(playerUUID)) {
            this.updatePlayerFamiliarLoadedTime(playerUUID);
            FamiliarLoader familiarLoader = new FamiliarLoader(playerUUID);
            Thread thread = new Thread(familiarLoader, "LycanitesFamiliars-" + playerUUID);
            thread.setDaemon(true);
            thread.start();
        }

        Map<UUID, PetEntry> playerFamiliarEntries = new HashMap<>();
        if (this.playerFamiliars.containsKey(playerUUID)) {
            playerFamiliarEntries.putAll(this.playerFamiliars.get(playerUUID));
            for (PetEntry familiarEntry : playerFamiliarEntries.values()) {
                if (familiarEntry.host == null) {
                    familiarEntry.host = player;
                }
            }
        }
        return playerFamiliarEntries;
    }

    public long getPlayerFamiliarLoadedTime(Player player) {
        UUID uuid = player.getUUID();
        if (!this.playerFamiliarLoadedTimes.containsKey(uuid)) {
            return -1;
        }
        return this.playerFamiliarLoadedTimes.get(uuid);
    }

    public void updatePlayerFamiliarLoadedTime(Player player) {
        this.updatePlayerFamiliarLoadedTime(player.getUUID());
    }

    public void updatePlayerFamiliarLoadedTime(UUID playerUUID) {
        this.playerFamiliarLoadedTimes.put(playerUUID, System.currentTimeMillis() / 1000);
    }

    public void applyPendingFamiliars(Player player) {
        if (!this.pendingFamiliars.remove(player.getUUID())) {
            return;
        }

        ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
        if (extendedPlayer != null) {
            extendedPlayer.loadFamiliars();
        }
    }
}
