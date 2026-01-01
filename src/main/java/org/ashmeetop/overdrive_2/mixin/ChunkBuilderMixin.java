package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    @Inject(method = "scheduleRunTasks", at = @At("HEAD"), cancellable = true)
    private void optimizeChunkUpdates(CallbackInfo ci) {
        // Reduce chunk update frequency based on performance
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.optimizeWorldRendering) {
            if (MobileRenderEngine.isEmergencyMode()) {
                // Skip some chunk updates in emergency mode
                if (ci.hashCode() % 3 != 0) { // Only update 1/3 of chunks
                    ci.cancel();
                }
            } else if (MobileRenderEngine.getCurrentPerformanceProfile().aggressiveCulling) {
                // Skip some chunk updates in battery saver mode
                if (ci.hashCode() % 2 != 0) { // Only update 1/2 of chunks
                    ci.cancel();
                }
            }
        }
    }

}