package org.ashmeetop.overdrive_2.client.rendering;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import org.ashmeetop.overdrive_2.Overdrive_2;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.ashmeetop.overdrive_2.Overdrive_2.*;

public class RenderOptimizer {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static void applyRenderOptimizations() {
        if (MC.options == null) return;

        GameOptions options = MC.options;

        // Apply mobile-friendly render settings from config
        options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        options.getEntityDistanceScaling().setValue(0.5);
        options.getEntityShadows().setValue(false);
        options.getAo().setValue(false);


        // Apply render distance from config
        options.getViewDistance().setValue(CONFIG.maxRenderDistance);

        // Disable vignette if configured
        if (CONFIG.disableVignette) {
            options.getEnableVsync().setValue(false);
        }

        LOGGER.info("Render optimizations applied successfully");
    }

    public static void dynamicChunkManagement() {
        if (MC.worldRenderer == null || MC.world == null) return;

        // Reduce chunk updates when FPS drops
        int currentFPS = MC.getCurrentFps();

        if (currentFPS < 20) {
            // Force slower chunk updates for low-end devices
            adjustChunkUpdateSpeed(2); // Slower updates
        } else if (currentFPS < 40) {
            adjustChunkUpdateSpeed(1); // Moderate updates
        } else {
            adjustChunkUpdateSpeed(0); // Normal updates
        }
    }

    private static void adjustChunkUpdateSpeed(int speedLevel) {
        // This would typically interact with chunk rendering settings
        // For now, we'll adjust entity rendering distance dynamically
        if (MC.options != null) {
            switch (speedLevel) {
                case 0 -> MC.options.getEntityDistanceScaling().setValue(1.0); // Normal
                case 1 -> MC.options.getEntityDistanceScaling().setValue(0.7); // Reduced
                case 2 -> MC.options.getEntityDistanceScaling().setValue(0.4); // Minimal
            }
        }
    }

    public static void applySkyOptimizations() {
        if (CONFIG.reduceSkyEffects && MC.options != null) {
            MC.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            // Additional sky rendering optimizations would go here
        }
    }

    public static void applyWeatherOptimizations() {
        if (CONFIG.reduceWeatherEffects) {
            // Reduce weather rendering intensity
            // This would typically be done through mixins
        }
    }
}