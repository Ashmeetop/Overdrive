package org.ashmeetop.overdrive_2.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MobileConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "overdrive-mobile.json"
    );

    // Performance settings
    public boolean enableFastGraphics = true;
    public int maxRenderDistance = 6;
    public boolean reduceParticles = true;
    public boolean optimizeEntities = true;
    public boolean reduceSkyEffects = true;
    public boolean fastMath = true;
    public boolean disableVignette = true;
    public boolean reduceWeatherEffects = true;
    // Add these to your MobileConfig class
    public boolean enableDebugLogging = false;
    public boolean optimizeWorldRendering = true;
    public boolean optimizeBiomeRendering = true;
    // Add these to your MobileConfig class
    public boolean enableGLOptimizations = true;
    public boolean optimizeTextures = true;
    public boolean aggressiveTextureDownscaling = false;
    public boolean enableTextureCompression = true;
    public boolean optimizeRenderStates = true;
    public boolean optimizeBuffers = true;
    public boolean optimizeShaders = true;
    public boolean dynamicGLOptimizations = true;
    public boolean backgroundGLOptimizations = true;


    public static MobileConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, MobileConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MobileConfig config = new MobileConfig();
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}