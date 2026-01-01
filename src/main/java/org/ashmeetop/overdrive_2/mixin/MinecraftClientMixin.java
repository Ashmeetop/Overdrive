package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onClientInit(RunArgs args, CallbackInfo ci) {
        // Early initialization hook for mobile optimizations
        MobileRenderEngine.applyFastMathOptimizations();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.world != null && !client.isPaused()) {
            // Continuous performance monitoring integrated into main tick
            MobileRenderEngine.dynamicPerformanceManagement();
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        // Clean shutdown of performance monitoring
        MobileRenderEngine.shutdown();
    }
}