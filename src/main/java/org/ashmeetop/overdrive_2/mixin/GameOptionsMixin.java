package org.ashmeetop.overdrive_2.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import org.ashmeetop.overdrive_2.Overdrive_2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

    @Inject(method = "load", at = @At("RETURN"))
    private void onOptionsLoad(CallbackInfo ci) {
        // Apply mobile optimizations after options are loaded
        if (Overdrive_2.CONFIG != null) {
            Overdrive_2.onConfigChanged();
        }
    }

    @Inject(method = "setServerViewDistance", at = @At("HEAD"), cancellable = true)
    private void forceFastGraphics(int serverViewDistance, CallbackInfo ci) {
        // Force fast graphics if configured for mobile
        if (Overdrive_2.CONFIG != null && Overdrive_2.CONFIG.enableFastGraphics) {
            GameOptions options = (GameOptions) (Object) this;
            options.getGraphicsMode().setValue(GraphicsMode.FAST);
            ci.cancel();
        }
    }
}