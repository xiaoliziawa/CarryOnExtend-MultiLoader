package com.lirxowo.carryonextend.mixin;

import com.lirxowo.carryonextend.client.PowerThrowHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void carryonextend$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo callbackInfo) {
        if (windowPointer == Minecraft.getInstance().getWindow().getWindow() && PowerThrowHandler.handleScroll(yOffset)) {
            callbackInfo.cancel();
        }
    }
}
