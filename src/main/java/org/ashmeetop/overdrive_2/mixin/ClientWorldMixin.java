package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.world.ClientWorld;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @Inject(method = "tickEntities", at = @At("HEAD"))
    private void optimizeEntityTicking(CallbackInfo ci) {
        // Optimize entity ticking based on performance profile
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.optimizeEntities) {
            if (MobileRenderEngine.isEmergencyMode()) {
                // Reduce entity tick frequency in emergency mode
                ClientWorld world = (ClientWorld) (Object) this;
                if (world.getTime() % 2 == 0) { // Tick entities every other tick
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "tickEntities", at = @At("HEAD"))
    private void optimizeBlockEntityTicking(CallbackInfo ci) {
        // Optimize block entity ticking
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.optimizeWorldRendering) {
            if (MobileRenderEngine.isEmergencyMode()) {
                // Reduce block entity tick frequency
                ClientWorld world = (ClientWorld) (Object) this;
                if (world.getTime() % 3 == 0) { // Tick block entities every 3rd tick
                    ci.cancel();
                }
            }
        }
    }
}