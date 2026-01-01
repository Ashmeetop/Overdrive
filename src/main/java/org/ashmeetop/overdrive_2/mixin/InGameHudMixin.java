package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onHudRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Apply HUD optimizations for mobile
        if (Overdrive_2.CONFIG != null) {
            if (MobileRenderEngine.isEmergencyMode()) {
                // Simplify HUD rendering in emergency mode
                simplifyHudRendering();
            }
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void disableVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        // Disable vignette effect if configured
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.disableVignette) {
            ci.cancel();
        }
    }

    private void simplifyHudRendering() {
        // Implementation for simplified HUD rendering
        // Would disable certain HUD elements for performance
    }
}