package com.lycanitesmobs.client.manager;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class LanguageManager {
    public static LanguageManager INSTANCE;
    protected static final Splitter SPLITTER = Splitter.on('=').limit(2);
    protected static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

    protected Map<String, String> map = new HashMap<>();

    /**
     * Returns the main Item Manager instance or creates it and returns it.
     **/
    public static LanguageManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LanguageManager();
        }
        return INSTANCE;
    }


    /**
     * Translates the provided text key, if it cannot be translated, attempts to translate it with the vanilla translator instead.
     *
     * @param key The key to translate into text.
     * @return
     */
    public static String translateOld(String key) {
        return key;
		/*if(!getInstance().map.containsKey(key)) {
			return I18n.format(key);
		}
		return getInstance().map.get(key).replace("\\n", "\n");*/
    }


    /**
     * Loads locale data from a specific file stream.
     *
     * @param inputStreamIn The input stream to read from.
     * @throws IOException
     */
    public void loadLocaleData(InputStream inputStreamIn) throws IOException {
        // TODO Load JSON instead.
        //inputStreamIn = net.minecraftforge.fml.common.FMLCommonHandler.instance().loadLanguage(this.map, inputStreamIn);
        if (inputStreamIn == null) {
            return;
        }
        for (String s : IOUtils.readLines(inputStreamIn, StandardCharsets.UTF_8)) {
            if (!s.isEmpty() && s.charAt(0) != '#') {
                String[] splitters = Iterables.toArray(SPLITTER.split(s), String.class);

                if (splitters != null && splitters.length == 2) {
                    String s1 = splitters[0];
                    String s2 = PATTERN.matcher(splitters[1]).replaceAll("%$1s");
                    this.map.put(s1, s2);
                }
            }
        }
    }


    /**
     * Loads the provided language.
     *
     * @param mainLanguage The language to load, default is en_us.
     */
    public void loadLanguage(String mainLanguage) {
        LMHelperClass.logDebug("Localisation", "Loading additional lang files...");

        // Get Languages To Load:
        List<String> languageList = Lists.newArrayList(mainLanguage);
        if (!"en_us".equals(mainLanguage)) {
            languageList.add(mainLanguage);
        }

		/*/ Load Languages Into Map:
		int laodedLangFiles = 0;
		for (String language : languageList) {
			String languageDir = String.format("lang/%s/", language);
			Path languageDirPath = FileLoader.CLIENT.getPath(languageDir);
			try {
				// Iterate Language Directories:
				Iterator<Path> languageDirIter = Files.walk(languageDirPath).iterator();
				while(languageDirIter.hasNext()) {
					Path subdirPath = languageDirIter.next();

					// Read Root Lang File:
					if(!Files.isDirectory(subdirPath)) {
						LycanitesMobs.logDebug("Localisation", "Reading translations from lang: " + subdirPath.toAbsolutePath());
						LanguageManager.getInstance().loadLocaleData(Files.newInputStream(subdirPath));
						laodedLangFiles++;
					}

				}
			}
			catch (Exception var9) {}
		}

		LycanitesMobs.logDebug("Localisation", laodedLangFiles + " Additional lang files loaded! Test translation: " + LanguageManager.translate("lycanitesmobs.test"));*/
    }
}
