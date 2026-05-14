package com.lycanitesmobs.core.data.config;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;

public class ConfigGeneral {
    public static ConfigGeneral INSTANCE;

    public final ForgeConfigSpec.ConfigValue<String> configVersion;

    public ConfigGeneral(ForgeConfigSpec.Builder builder) {
        builder.push("General");

        this.configVersion = builder
                .comment("The last version of Lycanites Mobs that this config ran with, if this is behind the Minimum Config Version then all configs (including jsons) will be reset, only manually update this if you have manually adapted your configs to the latest version of Lycanites Mobs.")
                .translation(CoreConfig.CONFIG_PREFIX + "version")
                .define("version", LycanitesMobs.versionNumber);

        builder.pop();
    }

    public void clearOldConfigs(String minVersion, String currentVersion) {
        // Get Config Version:
        String configVersion = this.configVersion.get();

        // Test Config Version:
        String[] minVersions = minVersion.split("\\.");
        String[] configVersions = configVersion.split("\\.");
        if (configVersions.length != 4)
            configVersions = "0.0.0.0".split("\\.");
        boolean oldVersion = false;
        for (int i = 0; i < 4; i++) {
            int minVerNum = NumberUtils.isCreatable(minVersions[i].replaceAll("[^\\d.]", "")) ? Integer.parseInt(minVersions[i].replaceAll("[^\\d.]", "")) : 0;
            int currentVerNum = NumberUtils.isCreatable(configVersions[i].replaceAll("[^\\d.]", "")) ? Integer.parseInt(configVersions[i].replaceAll("[^\\d.]", "")) : 0;
            if (currentVerNum < minVerNum) {
                oldVersion = true;
                break;
            }
            if (currentVerNum > minVerNum)
                break;
        }

        // Clear Old Configs:
        if (oldVersion) {
            String configDirPath = new File(".") + "/config/" + LycanitesMobs.MODID;
            File configDir = new File(configDirPath);
            configDir.mkdir();
            LMHelperClass.logWarning("", "[Config] The current configs are too old, clearing all configs now...");
            try {
                FileUtils.cleanDirectory(configDir);
            } catch (IOException e) {
                LMHelperClass.logWarning("", "[Config] Unable to clear the config directory! This could be a file permissions issue!");
                e.printStackTrace();
            }
        }

        // Update Config Version:
        //currentVersion = currentVersion.replace(" ", "");
        //currentVersion = currentVersion.split("-")[0];
    }
}
