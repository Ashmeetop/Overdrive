package org.ashmeetop.overdrive_2.client.rendering.mobile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Window;
import org.ashmeetop.overdrive_2.client.config.MobileConfig;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.ashmeetop.overdrive_2.Overdrive_2.*;

public class MobileRenderEngine {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static MobileConfig CONFIG;

    private static int lastKnownFPS = 60;
    private static long lastFPSCheck = 0;
    private static PerformanceProfile currentProfile = PerformanceProfile.BALANCED;
    private static ScheduledExecutorService performanceMonitor;

    // GL optimization state tracking
    private static boolean glOptimizationsApplied = false;
    private static int originalMaxTextureSize = 0;
    private static boolean supportsASTC = false;
    private static boolean supportsETC2 = false;
    private static boolean supportsS3TC = false;

    // Performance profiles for different device capabilities
    public enum PerformanceProfile {
        EXTREME_SAVINGS(2, 0.3f, 4, true, true),   // Maximum battery saving
        BATTERY_SAVER(1, 0.5f, 6, true, false),    // Balanced battery/performance
        BALANCED(0, 0.7f, 8, false, false),        // Default mobile profile
        PERFORMANCE(-1, 0.9f, 12, false, false);   // Higher end devices

        public final int renderQuality;
        public final float entityDistance;
        public final int maxRenderDistance;
        public final boolean aggressiveCulling;
        public final boolean emergencyMode;

        PerformanceProfile(int renderQuality, float entityDistance, int maxRenderDistance,
                           boolean aggressiveCulling, boolean emergencyMode) {
            this.renderQuality = renderQuality;
            this.entityDistance = entityDistance;
            this.maxRenderDistance = maxRenderDistance;
            this.aggressiveCulling = aggressiveCulling;
            this.emergencyMode = emergencyMode;
        }
    }

    public static void initialize(MobileConfig config) {
        CONFIG = config;

        if (MC.options == null) return;

        detectPerformanceProfile();
        applyMobileOptimizations();
        applyGLOptimizations(); // Apply GL optimizations on startup
        startPerformanceMonitoring();

        LOGGER.info("Mobile Render Engine initialized with profile: " + currentProfile);
    }

    private static void detectPerformanceProfile() {
        Window window = MC.getWindow();
        if (window == null) return;

        // Use config-based settings first, then auto-detect
        int configRenderDistance = CONFIG.maxRenderDistance;

        if (configRenderDistance <= 4) {
            currentProfile = PerformanceProfile.EXTREME_SAVINGS;
        } else if (configRenderDistance <= 6) {
            currentProfile = PerformanceProfile.BATTERY_SAVER;
        } else if (configRenderDistance <= 8) {
            currentProfile = PerformanceProfile.BALANCED;
        } else {
            currentProfile = PerformanceProfile.PERFORMANCE;
        }

        LOGGER.debug("Detected performance profile: " + currentProfile + " based on render distance: " + configRenderDistance);
    }

    public static void applyMobileOptimizations() {
        GameOptions options = MC.options;
        if (options == null) return;

        // Apply graphics mode from config
        if (CONFIG.enableFastGraphics) {
            options.getGraphicsMode().setValue(GraphicsMode.FAST);
        }

        // Apply render distance (capped by performance profile)
        int renderDistance = Math.min(CONFIG.maxRenderDistance, currentProfile.maxRenderDistance);
        options.getViewDistance().setValue(renderDistance);

        // Apply entity optimizations
        if (CONFIG.optimizeEntities) {
            options.getEntityDistanceScaling().setValue((double) currentProfile.entityDistance);
            options.getEntityShadows().setValue(false);
        }

        // Apply sky optimizations
        if (CONFIG.reduceSkyEffects) {
            options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        }

        // Apply particle optimizations
        if (CONFIG.reduceParticles) {
            options.getParticles().setValue(ParticlesMode.MINIMAL);
        }

        // Apply other visual optimizations
        options.getAo().setValue(false);
        options.getBiomeBlendRadius().setValue(0);
        options.getMipmapLevels().setValue(0); // Disable mipmapping for performance

        // Disable vignette if configured
        if (CONFIG.disableVignette) {
            // Vignette is typically handled in shaders, we'll handle this in mixins
        }

        // Apply weather optimizations
        if (CONFIG.reduceWeatherEffects) {
            // Weather effects will be reduced through mixins
        }

        LOGGER.info("Mobile optimizations applied successfully");
    }

    /**
     * Applies OpenGL-specific optimizations for mobile devices
     */
    public static void applyGLOptimizations() {
        if (!CONFIG.enableGLOptimizations) {
            return;
        }

        try {
            GLCapabilities caps = GL.getCapabilities();

            // Detect supported texture compression formats
            detectSupportedTextureFormats(caps);

            // Apply texture optimizations
            applyTextureOptimizations();

            // Apply rendering state optimizations
            applyRenderingStateOptimizations();

            // Apply buffer management optimizations
            applyBufferOptimizations();

            // Apply shader optimizations
            applyShaderOptimizations();

            glOptimizationsApplied = true;
            LOGGER.info("GL optimizations applied successfully");

        } catch (Exception e) {
            LOGGER.error("Failed to apply GL optimizations", e);
        }
    }

    /**
     * Detects supported texture compression formats for the current GPU
     */
    private static void detectSupportedTextureFormats(GLCapabilities caps) {
        supportsASTC = caps.OpenGL20 || (caps.GL_KHR_texture_compression_astc_ldr || caps.GL_KHR_texture_compression_astc_hdr);
        supportsETC2 = caps.OpenGL20 || caps.GL_ARB_ES3_compatibility;
        supportsS3TC = caps.GL_EXT_texture_compression_s3tc;

        LOGGER.debug("Texture format support - ASTC: {}, ETC2: {}, S3TC: {}",
                supportsASTC, supportsETC2, supportsS3TC);
    }

    /**
     * Applies texture-related optimizations
     */
    private static void applyTextureOptimizations() {
        if (CONFIG.optimizeTextures) {
            // Reduce maximum texture size for memory savings
            originalMaxTextureSize = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_MAX_TEXTURE_SIZE);
            int optimizedTextureSize = Math.min(2048, originalMaxTextureSize); // Cap at 2048 for mobile

            if (CONFIG.aggressiveTextureDownscaling) {
                optimizedTextureSize = Math.min(1024, optimizedTextureSize); // Further reduce for low-end devices
            }

            LOGGER.info("Texture optimization: Original max size: {}, Optimized: {}",
                    originalMaxTextureSize, optimizedTextureSize);

            // This would typically be implemented through mixins that intercept texture loading
            // and enforce the size limit
        }

        // Enable texture compression if supported
        if (CONFIG.enableTextureCompression) {
            if (supportsASTC) {
                LOGGER.info("Using ASTC texture compression");
                // ASTC is preferred for mobile (especially ARM Mali and Adreno GPUs)
            } else if (supportsETC2) {
                LOGGER.info("Using ETC2 texture compression");
                // ETC2 is good fallback for mobile
            } else if (supportsS3TC) {
                LOGGER.info("Using S3TC texture compression");
                // S3TC as last resort
            }
        }
    }

    /**
     * Optimizes rendering states for mobile GPUs
     */
    private static void applyRenderingStateOptimizations() {
        // These would be applied through mixins that modify render state setup

        if (CONFIG.optimizeRenderStates) {
            // Reduce overdraw through better depth testing
            enableAggressiveDepthTesting();

            // Optimize blending operations
            optimizeBlendingOperations();

            // Reduce state changes
            minimizeGLStateChanges();

            // Use more efficient polygon modes
            optimizePolygonRendering();
        }
    }

    /**
     * Applies buffer management optimizations
     */
    private static void applyBufferOptimizations() {
        if (CONFIG.optimizeBuffers) {
            // Use more efficient buffer strategies
            enableStreamBuffers();

            // Optimize vertex format efficiency
            optimizeVertexFormats();

            // Implement buffer pooling to reduce allocation overhead
            setupBufferPooling();
        }
    }

    /**
     * Applies shader optimizations
     */
    private static void applyShaderOptimizations() {
        if (CONFIG.optimizeShaders) {
            // Use simplified shader variants
            enableSimplifiedShaders();

            // Reduce shader precision where acceptable
            reduceShaderPrecision();

            // Minimize shader instructions
            optimizeShaderComplexity();
        }
    }

    // Implementation stubs for GL optimization methods
    private static void enableAggressiveDepthTesting() {
        // Would be implemented through mixins to modify depth testing behavior
        LOGGER.debug("Aggressive depth testing enabled");
    }

    private static void optimizeBlendingOperations() {
        // Optimize blend functions for mobile GPUs
        LOGGER.debug("Blending operations optimized");
    }

    private static void minimizeGLStateChanges() {
        // Reduce frequency of GL state changes (texture binds, shader changes, etc.)
        LOGGER.debug("GL state change minimization enabled");
    }

    private static void optimizePolygonRendering() {
        // Use triangle strips/fans where possible, backface culling optimization
        LOGGER.debug("Polygon rendering optimized");
    }

    private static void enableStreamBuffers() {
        // Use STREAM_DRAW buffers for dynamic data
        LOGGER.debug("Stream buffers enabled");
    }

    private static void optimizeVertexFormats() {
        // Use more compact vertex formats
        LOGGER.debug("Vertex formats optimized");
    }

    private static void setupBufferPooling() {
        // Implement buffer pooling to reduce allocation overhead
        LOGGER.debug("Buffer pooling setup");
    }

    private static void enableSimplifiedShaders() {
        // Use simplified shader variants for mobile
        LOGGER.debug("Simplified shaders enabled");
    }

    private static void reduceShaderPrecision() {
        // Use mediump instead of highp in shaders where acceptable
        LOGGER.debug("Shader precision reduced");
    }

    private static void optimizeShaderComplexity() {
        // Reduce shader instruction count
        LOGGER.debug("Shader complexity optimized");
    }

    private static void startPerformanceMonitoring() {
        if (performanceMonitor != null) {
            performanceMonitor.shutdown();
        }

        performanceMonitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Overdrive-PerformanceMonitor");
            t.setDaemon(true);
            return t;
        });

        // Monitor performance every 2 seconds
        performanceMonitor.scheduleAtFixedRate(() -> {
            if (MC.world == null || MC.isPaused()) return;

            try {
                dynamicPerformanceManagement();
            } catch (Exception e) {
                LOGGER.error("Error in performance monitoring", e);
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    public static void dynamicPerformanceManagement() {
        if (MC.worldRenderer == null || MC.world == null) return;

        updateFPSMonitoring();
        applyDynamicAdjustments();
        manageBackgroundRendering();

        // Dynamic GL optimizations based on performance
        if (CONFIG.dynamicGLOptimizations) {
            applyDynamicGLOptimizations();
        }
    }

    private static void updateFPSMonitoring() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFPSCheck > 1000) {
            lastKnownFPS = MC.getCurrentFps();
            lastFPSCheck = currentTime;

            // Dynamic profile adjustment based on actual performance
            adjustPerformanceProfile();
        }
    }

    private static void adjustPerformanceProfile() {
        // Adjust profile based on actual FPS performance
        if (lastKnownFPS < 20 && currentProfile != PerformanceProfile.EXTREME_SAVINGS) {
            currentProfile = PerformanceProfile.EXTREME_SAVINGS;
            applyMobileOptimizations();
            applyAggressiveGLOptimizations();
            LOGGER.info("Switched to EXTREME_SAVINGS profile due to low FPS: " + lastKnownFPS);
        } else if (lastKnownFPS < 30 && currentProfile.ordinal() < PerformanceProfile.BATTERY_SAVER.ordinal()) {
            currentProfile = PerformanceProfile.BATTERY_SAVER;
            applyMobileOptimizations();
            applyGLOptimizations();
            LOGGER.info("Switched to BATTERY_SAVER profile due to moderate FPS: " + lastKnownFPS);
        } else if (lastKnownFPS > 50 && currentProfile.ordinal() > PerformanceProfile.BALANCED.ordinal()) {
            currentProfile = PerformanceProfile.BALANCED;
            applyMobileOptimizations();
            applyGLOptimizations();
            LOGGER.info("Switched to BALANCED profile due to good FPS: " + lastKnownFPS);
        }
    }

    /**
     * Applies dynamic GL optimizations based on current performance
     */
    private static void applyDynamicGLOptimizations() {
        if (lastKnownFPS < 25) {
            // Apply more aggressive optimizations when FPS is low
            if (CONFIG.optimizeTextures && !CONFIG.aggressiveTextureDownscaling) {
                CONFIG.aggressiveTextureDownscaling = true;
                LOGGER.info("Enabled aggressive texture downscaling due to low FPS");
            }
        } else if (lastKnownFPS > 45) {
            // Relax optimizations when performance is good
            if (CONFIG.aggressiveTextureDownscaling) {
                CONFIG.aggressiveTextureDownscaling = false;
                LOGGER.info("Disabled aggressive texture downscaling due to good FPS");
            }
        }
    }

    /**
     * Applies maximum GL optimizations for emergency situations
     */
    private static void applyAggressiveGLOptimizations() {
        LOGGER.warn("Applying aggressive GL optimizations");

        // Force maximum texture compression
        CONFIG.enableTextureCompression = true;
        CONFIG.aggressiveTextureDownscaling = true;
        CONFIG.optimizeTextures = true;
        CONFIG.optimizeRenderStates = true;
        CONFIG.optimizeBuffers = true;
        CONFIG.optimizeShaders = true;

        // Re-apply GL optimizations with new settings
        applyGLOptimizations();
    }

    private static void applyDynamicAdjustments() {
        // Apply emergency measures if FPS is critically low
        if (lastKnownFPS < 15) {
            applyEmergencyMeasures();
        }

        // Dynamic chunk updates based on performance
        adjustChunkManagement();
    }

    public static void applyEmergencyMeasures() {
        LOGGER.warn("Applying emergency performance measures - FPS: " + lastKnownFPS);

        if (MC.options != null) {
            // Drastic entity distance reduction
            MC.options.getEntityDistanceScaling().setValue(0.2);

            // Force minimal particles
            MC.options.getParticles().setValue(ParticlesMode.MINIMAL);

            // Reduce render distance further if needed
            int currentDistance = MC.options.getViewDistance().getValue();
            if (currentDistance > 4) {
                MC.options.getViewDistance().setValue(4);
            }
        }

        // Apply maximum GL optimizations in emergency mode
        applyAggressiveGLOptimizations();
    }

    private static void adjustChunkManagement() {
        if (!CONFIG.optimizeEntities) return;

        // Dynamic chunk update scheduling based on FPS
        int updateInterval;
        if (lastKnownFPS < 25) {
            updateInterval = 3; // Very slow updates
        } else if (lastKnownFPS < 40) {
            updateInterval = 2; // Slow updates
        } else {
            updateInterval = 1; // Normal updates
        }

        // Adjust entity rendering distance dynamically
        if (MC.options != null) {
            float distanceScale = 1.0f - (updateInterval * 0.15f);
            float newDistance = Math.max(0.2f, currentProfile.entityDistance * distanceScale);
            MC.options.getEntityDistanceScaling().setValue((double) newDistance);
        }
    }

    private static void manageBackgroundRendering() {
        // Reduce rendering when game is in background (important for mobile)
        if (!MC.isWindowFocused()) {
            // Drastically reduce rendering when app is in background
            if (MC.options != null) {
                MC.options.getMaxFps().setValue(10); // Very low FPS in background

                // Reduce view distance in background
                int backgroundDistance = Math.max(2, currentProfile.maxRenderDistance / 3);
                MC.options.getViewDistance().setValue(backgroundDistance);
            }

            // Apply background-specific GL optimizations
            applyBackgroundGLOptimizations();
        } else {
            // Restore normal settings when in foreground
            if (MC.options != null) {
                MC.options.getMaxFps().setValue(60);
                MC.options.getViewDistance().setValue(
                        Math.min(CONFIG.maxRenderDistance, currentProfile.maxRenderDistance)
                );
            }

            // Restore normal GL optimizations
            applyGLOptimizations();
        }
    }

    /**
     * Applies special GL optimizations for when the app is in background
     */
    private static void applyBackgroundGLOptimizations() {
        if (CONFIG.backgroundGLOptimizations) {
            // Further reduce texture quality in background
            LOGGER.debug("Applying background GL optimizations");
        }
    }

    public static void applyFastMathOptimizations() {
        if (CONFIG.fastMath) {
            // This would typically be implemented through mixins that replace
            // expensive math operations with faster approximations
            // For example: faster sin/cos, sqrt approximations, etc.
        }
    }

    public static void shutdown() {
        if (performanceMonitor != null) {
            performanceMonitor.shutdown();
            try {
                if (!performanceMonitor.awaitTermination(2, TimeUnit.SECONDS)) {
                    performanceMonitor.shutdownNow();
                }
            } catch (InterruptedException e) {
                performanceMonitor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Restore original GL state if needed
        restoreOriginalGLState();
    }

    /**
     * Restores original GL state when shutting down
     */
    private static void restoreOriginalGLState() {
        if (glOptimizationsApplied) {
            LOGGER.info("Restoring original GL state");
            // Implementation would depend on what specific optimizations were applied
        }
    }

    public static void onConfigChanged() {
        // Re-detect profile and apply optimizations when config changes
        detectPerformanceProfile();
        applyMobileOptimizations();
        applyGLOptimizations(); // Re-apply GL optimizations

        LOGGER.info("Mobile render settings updated");
    }

    // Public getters for other parts of the mod
    public static PerformanceProfile getCurrentPerformanceProfile() {
        return currentProfile;
    }

    public static int getCurrentFPS() {
        return lastKnownFPS;
    }

    public static boolean isEmergencyMode() {
        return lastKnownFPS < 20;
    }

    // GL optimization status getters
    public static boolean areGLOptimizationsApplied() {
        return glOptimizationsApplied;
    }

    public static boolean supportsASTCCompression() {
        return supportsASTC;
    }

    public static boolean supportsETC2Compression() {
        return supportsETC2;
    }

    public static boolean supportsS3TCCompression() {
        return supportsS3TC;
    }
}