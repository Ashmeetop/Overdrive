package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        // Apply pre-render optimizations based on current performance profile
        MobileRenderEngine.PerformanceProfile profile = MobileRenderEngine.getCurrentPerformanceProfile();
        if (profile.aggressiveCulling) {
            // Enable additional culling for low-end devices
            applyAggressiveCulling();
        }
    }

    private boolean modifyCaptureFrustum(boolean captureFrustum) {
        // Disable frustum capture in emergency mode for performance
        return !MobileRenderEngine.isEmergencyMode() && captureFrustum;
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void optimizeWeatherRendering(net.minecraft.client.render.LightmapTextureManager lightmapTextureManager,
                                          float f, double d, double e, double g, CallbackInfo ci) {
        // Reduce or disable weather effects based on config
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.reduceWeatherEffects) {
            if (MobileRenderEngine.isEmergencyMode()) {
                ci.cancel(); // Completely disable in emergency mode
            } else if (MobileRenderEngine.getCurrentPerformanceProfile().emergencyMode) {
                ci.cancel(); // Disable in extreme savings mode
            }
        }
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void optimizeCloudRendering(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        // Optimize cloud rendering based on config
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.reduceSkyEffects) {
            if (MobileRenderEngine.isEmergencyMode() || MobileRenderEngine.getCurrentPerformanceProfile().emergencyMode) {
                ci.cancel();
            }
        }
    }

    private void applyAggressiveCulling() {
        // Implementation for aggressive culling would go here
        // This would typically modify GL culling settings
    }
}