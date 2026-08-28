package com.soarclient.mixin.mixins.minecraft.client;

import com.soarclient.Soar;
import com.soarclient.event.EventBus;
import com.soarclient.event.client.MouseScrollEvent;
import com.soarclient.management.mod.impl.hud.CPSDisplayMod;
import com.soarclient.management.mod.settings.impl.KeybindSetting;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {

    @Inject(method = "onMouseButton", at = @At("TAIL"))
    private void handleMouseButton(long window, MouseInput buttonInfo, int action, CallbackInfo ci) {
        boolean pressed = action == GLFW.GLFW_PRESS;
        InputUtil.Key key = InputUtil.Type.MOUSE.createFromCode(buttonInfo.button());
        for (KeybindSetting setting : Soar.getInstance().getModManager().getKeybindSettings()) {
            if (setting.getKey().equals(key)) {
                if (pressed) {
                    setting.setPressed();
                }
                setting.setKeyDown(pressed);
            }
        }

        if (pressed) {
            CPSDisplayMod cps = Soar.getInstance().getModManager().getMods().stream()
                    .filter(CPSDisplayMod.class::isInstance)
                    .map(CPSDisplayMod.class::cast)
                    .findFirst()
                    .orElse(null);
            if (cps != null) {
                cps.onMouseClick(buttonInfo.button(), true);
            }
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void handleMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = new MouseScrollEvent(vertical);
        EventBus.getInstance().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
