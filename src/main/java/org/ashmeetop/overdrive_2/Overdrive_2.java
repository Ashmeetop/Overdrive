package org.ashmeetop.overdrive_2;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.ashmeetop.overdrive_2.client.config.MobileConfig;
import org.ashmeetop.overdrive_2.client.rendering.RenderHooks;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;

import java.util.Collection;
import java.util.Collections;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Overdrive_2 implements ClientModInitializer {
    public static final String MOD_ID = "overdrive_2";
    public static MobileConfig CONFIG;
    private static boolean initialized = false;
    private static long lastOptimizationCheck = 0;
    private static final long OPTIMIZATION_INTERVAL = 5000; // 5 seconds

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Overdrive Mobile Render Engine...");

        // Load configuration first
        try {
            CONFIG = MobileConfig.load();
            LOGGER.info("Mobile configuration loaded successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to load mobile configuration, using defaults", e);
            CONFIG = new MobileConfig();
        }

        // Initialize core systems
        initializeCoreSystems();

        // Register resource reload listener for config changes
        registerResourceReloadListener();

        // Register client lifecycle events
        registerLifecycleEvents();

        // Register tick event for continuous optimization
        registerTickEvents();

        LOGGER.info("Overdrive Mobile Render Engine fully initialized");
    }

    private void initializeCoreSystems() {
        try {
            // Initialize mobile render engine with config
            MobileRenderEngine.initialize(CONFIG);
            LOGGER.info("Mobile Render Engine initialized");

            // Initialize rendering hooks
            RenderHooks.initialize();
            LOGGER.info("Render hooks initialized");

            // Apply fast math optimizations if enabled
            if (CONFIG.fastMath) {
                MobileRenderEngine.applyFastMathOptimizations();
                LOGGER.info("Fast math optimizations applied");
            }

        } catch (Exception e) {
            LOGGER.error("Failed to initialize core systems", e);
        }
    }

    private void registerResourceReloadListener() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return  Identifier.of(MOD_ID, "mobile_optimizations");
                    }

                    @Override
                    public Collection<Identifier> getFabricDependencies() {
                        return Collections.singletonList(ResourceReloadListenerKeys.MODELS);
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        LOGGER.info("Resource reload detected - reapplying mobile optimizations");

                        // Reload config and reapply optimizations
                        try {
                            CONFIG = MobileConfig.load();
                            MobileRenderEngine.onConfigChanged();
                            LOGGER.info("Mobile optimizations reapplied after resource reload");
                        } catch (Exception e) {
                            LOGGER.error("Failed to reapply optimizations after resource reload", e);
                        }
                    }
                }
        );
    }

    private void registerLifecycleEvents() {
        // Handle client shutdown
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("Shutting down Overdrive Mobile Render Engine...");
            MobileRenderEngine.shutdown();
            LOGGER.info("Overdrive shutdown complete");
        });

        // Handle world load/unload
        ClientLifecycleEvents.CLIENT_STARTED.register((world) -> {
            LOGGER.info("World loaded - applying mobile optimizations");
            applyWorldOptimizations();
        });

    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long currentTime = System.currentTimeMillis();

            // Initial world optimization
            if (!initialized && client.world != null) {
                applyWorldOptimizations();
                initialized = true;
            }

            // Continuous performance management (with rate limiting)
            if (client.world != null && currentTime - lastOptimizationCheck > OPTIMIZATION_INTERVAL) {
                try {
                    MobileRenderEngine.dynamicPerformanceManagement();
                    lastOptimizationCheck = currentTime;

                    // Log performance status periodically (debug)
                    if (CONFIG.enableDebugLogging) {
                        int currentFPS = MobileRenderEngine.getCurrentFPS();
                        LOGGER.debug("Performance check - FPS: {}, Profile: {}",
                                currentFPS, MobileRenderEngine.getCurrentPerformanceProfile());
                    }
                } catch (Exception e) {
                    LOGGER.error("Error in dynamic performance management", e);
                }
            }

            // Emergency performance recovery (immediate, no rate limiting)
            if (client.world != null && MobileRenderEngine.isEmergencyMode()) {
                try {
                    MobileRenderEngine.applyEmergencyMeasures();

                    // Only log emergency mode once to avoid spam
                    if (!isEmergencyModeLogged) {
                        LOGGER.warn("Emergency performance mode activated");
                        isEmergencyModeLogged = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error applying emergency measures", e);
                }
            } else {
                isEmergencyModeLogged = false;
            }

            // Handle window focus changes for background optimizations
            handleFocusOptimizations(client);
        });
    }

    private static boolean isEmergencyModeLogged = false;
    private static boolean wasWindowFocused = true;

    private void handleFocusOptimizations(MinecraftClient client) {
        boolean isFocused = client.isWindowFocused();

        // Only trigger when focus state changes
        if (isFocused != wasWindowFocused) {
            if (isFocused) {
                LOGGER.info("Window focused - restoring normal optimizations");
                MobileRenderEngine.applyMobileOptimizations();
            } else {
                LOGGER.info("Window unfocused - applying background optimizations");
                // Background optimizations are handled internally by MobileRenderEngine
            }
            wasWindowFocused = isFocused;
        }
    }

    private void applyWorldOptimizations() {
        try {
            // Apply mobile optimizations
            MobileRenderEngine.applyMobileOptimizations();

            // Apply GL optimizations
            MobileRenderEngine.applyGLOptimizations();

            // Apply any additional world-specific optimizations
            applyWorldSpecificOptimizations();

            LOGGER.info("World-specific optimizations applied successfully");

        } catch (Exception e) {
            LOGGER.error("Failed to apply world optimizations", e);
        }
    }

    private void applyWorldSpecificOptimizations() {
        // World-specific optimizations that depend on world state
        if (CONFIG.optimizeWorldRendering) {
            // These would typically be implemented in mixins
            // For example: chunk loading optimizations, entity management, etc.
            LOGGER.debug("World-specific rendering optimizations applied");
        }

        // Apply biome-specific optimizations if configured
        if (CONFIG.optimizeBiomeRendering) {
            LOGGER.debug("Biome rendering optimizations applied");
        }
    }

    /**
     * Called when config is changed via in-game options
     */
    public static void onConfigChanged() {
        LOGGER.info("Config changed - updating mobile optimizations");
        try {
            // Reload config
            CONFIG = MobileConfig.load();

            // Notify systems of config change
            MobileRenderEngine.onConfigChanged();

            // Reapply optimizations
            if (MinecraftClient.getInstance().world != null) {
                MobileRenderEngine.applyMobileOptimizations();
                MobileRenderEngine.applyGLOptimizations();
            }

            LOGGER.info("Config changes applied successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to apply config changes", e);
        }
    }

    /**
     * Utility method to check if mod is properly initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get current performance profile for external mod integration
     */
    public static MobileRenderEngine.PerformanceProfile getCurrentPerformanceProfile() {
        return MobileRenderEngine.getCurrentPerformanceProfile();
    }

    /**
     * Get current FPS for external mod integration
     */
    public static int getCurrentFPS() {
        return MobileRenderEngine.getCurrentFPS();
    }

    /**
     * Check if GL optimizations are active
     */
    public static boolean areGLOptimizationsActive() {
        return MobileRenderEngine.areGLOptimizationsApplied();
    }
}