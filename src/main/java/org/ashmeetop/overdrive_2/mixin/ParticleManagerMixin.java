package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.ashmeetop.overdrive_2.client.rendering.mobile.MobileRenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void disableParticleSpawning(ParticleEffect parameters, double x, double y, double z,
                                         double velocityX, double velocityY, double velocityZ,
                                         CallbackInfoReturnable<net.minecraft.client.particle.Particle> cir) {
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.reduceParticles) {
            MobileRenderEngine.PerformanceProfile profile = MobileRenderEngine.getCurrentPerformanceProfile();

            // Completely disable particles based on performance profile
            if (profile.emergencyMode) {
                cir.setReturnValue(null);
                return;
            }

            if (profile.aggressiveCulling) {
                cir.setReturnValue(null);
                return;
            }

            // Disable heavy particles in all non-performance modes
            if (isHeavyParticle(parameters) && profile != MobileRenderEngine.PerformanceProfile.PERFORMANCE) {
                cir.setReturnValue(null);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void disableParticleUpdates(CallbackInfo ci) {
        // Completely disable particle updates in low-performance modes
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.reduceParticles) {
            if (MobileRenderEngine.isEmergencyMode() ||
                    MobileRenderEngine.getCurrentPerformanceProfile().aggressiveCulling) {
                ci.cancel();
            }
        }
    }

    private boolean isHeavyParticle(ParticleEffect parameters) {
        // Identify computationally expensive particles to disable
        String particleName = parameters.toString().toLowerCase();
        return particleName.contains("explosion") ||
                particleName.contains("cloud") ||
                particleName.contains("flame") ||
                particleName.contains("smoke") ||
                particleName.contains("portal") ||
                particleName.contains("bubble") ||
                particleName.contains("crit") ||
                particleName.contains("spell") ||
                particleName.contains("note") ||
                particleName.contains("heart") ||
                particleName.contains("lava") ||
                particleName.contains("water") ||
                particleName.contains("redstone") ||
                particleName.contains("sweep") ||
                particleName.contains("damage");
    }
}