package org.ashmeetop.overdrive_2.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;

public class RenderHooks {
    public static void initialize() {
        // Hook into world rendering for dynamic optimizations
        WorldRenderEvents.START.register(context -> {
            MobileRenderEngine.applyFastMathOptimizations();
        });

        WorldRenderEvents.END.register(context -> {
            MobileRenderEngine.applyMobileOptimizations();
        });
    }
}