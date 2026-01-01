package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void optimizeEntityRendering(E entity, double x, double y, double z,
                                                            float yaw, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices,
                                                            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
                                                            int light, CallbackInfo ci) {
        // Skip entity rendering if too far or in emergency mode
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.optimizeEntities) {
            if (MobileRenderEngine.isEmergencyMode() &&
                    entity.squaredDistanceTo(
                            net.minecraft.client.MinecraftClient.getInstance().player) > 256.0) {
                ci.cancel();
                return;
            }

            // Additional entity culling based on performance profile
            MobileRenderEngine.PerformanceProfile profile = MobileRenderEngine.getCurrentPerformanceProfile();
            if (profile.aggressiveCulling &&
                    entity.squaredDistanceTo(
                            net.minecraft.client.MinecraftClient.getInstance().player) > 64.0) {
                ci.cancel();
            }
        }
    }
}