package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.util.math.MathHelper;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MathHelper.class)
public class MathHelperMixin {

    private static final float[] SIN_TABLE = new float[65536];
    private static final float[] COS_TABLE = new float[65536];

    static {
        // Precompute trig tables for faster access
        for (int i = 0; i < 65536; ++i) {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
            COS_TABLE[i] = (float) Math.cos((double) i * Math.PI * 2.0 / 65536.0);
        }
    }

    @Inject(method = "sin", at = @At("HEAD"), cancellable = true)
    private static void fastSin(float value, CallbackInfoReturnable<Float> cir) {
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.fastMath) {
            // Use table lookup for faster sin calculation
            cir.setReturnValue(SIN_TABLE[(int) (value * 10430.378F) & 65535]);
        }
    }

    @Inject(method = "cos", at = @At("HEAD"), cancellable = true)
    private static void fastCos(float value, CallbackInfoReturnable<Float> cir) {
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.fastMath) {
            // Use table lookup for faster cos calculation
            cir.setReturnValue(COS_TABLE[(int) (value * 10430.378F) & 65535]);
        }
    }

    @Inject(method = "sqrt", at = @At("HEAD"), cancellable = true)
    private static void fastSqrt(float value, CallbackInfoReturnable<Float> cir) {
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.fastMath) {
            // Fast inverse square root approximation (Quake algorithm)
            float xhalf = 0.5f * value;
            int i = Float.floatToIntBits(value);
            i = 0x5f3759df - (i >> 1);
            value = Float.intBitsToFloat(i);
            value = value * (1.5f - xhalf * value * value);
            cir.setReturnValue(1.0f / value);
        }
    }
}