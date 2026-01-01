package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.render.BackgroundRenderer;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void optimizeFogRendering(net.minecraft.client.render.Camera camera,
                                             BackgroundRenderer.FogType fogType,
                                             float viewDistance, boolean thickFog,
                                             float tickDelta, CallbackInfo ci) {
        // Reduce fog distance for performance
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.reduceSkyEffects) {
            if (MobileRenderEngine.isEmergencyMode()) {
                // Drastically reduce fog distance in emergency mode
                net.minecraft.client.render.GameRenderer gameRenderer = net.minecraft.client.MinecraftClient.getInstance().gameRenderer;
                gameRenderer.getClient().getBufferBuilders().getEntityVertexConsumers();
                ci.cancel();
                // Apply optimized fog settings
                applyOptimizedFog();
            }
        }
    }

    private static void applyOptimizedFog() {
        // Implementation for optimized fog rendering
        com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(4.0f);
        com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(12.0f);
    }
}