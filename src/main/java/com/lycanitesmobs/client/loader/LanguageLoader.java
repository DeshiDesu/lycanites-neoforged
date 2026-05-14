package com.lycanitesmobs.client.loader;

import com.lycanitesmobs.client.manager.LanguageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@OnlyIn(Dist.CLIENT)
public class LanguageLoader implements PreparableReloadListener {
    public static LanguageLoader INSTANCE;

    protected Map<String, String> map = new HashMap<>();

    /**
     * Returns the main Item Manager instance or creates it and returns it.
     **/
    public static LanguageLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LanguageLoader();
        }
        return INSTANCE;
    }


    /**
     * Called when the Resource Manager is reloaded included the initial load up of the game.
     *
     * @param resourceManager The resource manager instance.
     */
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier,
                                          ResourceManager resourceManager,
                                          ProfilerFiller prepProfiler,
                                          ProfilerFiller applyProfiler,
                                          Executor backgroundExecutor,
                                          Executor gameExecutor) {
        String langCode = Minecraft.getInstance().options.languageCode;

        return CompletableFuture
                .runAsync(() -> {
                }, backgroundExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenRunAsync(() -> {
                    LanguageManager.getInstance().loadLanguage(langCode);
                }, gameExecutor);
    }


}
