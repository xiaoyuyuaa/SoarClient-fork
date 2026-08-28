package com.soarclient.mixin.mixins.minecraft.client;

import com.soarclient.Soar;
import com.soarclient.management.mod.settings.impl.KeybindSetting;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("TAIL"))
    private void handleKey(long window, int action, KeyInput event, CallbackInfo ci) {
        InputUtil.Key key = InputUtil.fromKeyCode(event);
        boolean down = action != GLFW.GLFW_RELEASE;
        for (KeybindSetting setting : Soar.getInstance().getModManager().getKeybindSettings()) {
            if (setting.getKey().equals(key)) {
                if (action == GLFW.GLFW_PRESS) {
                    setting.setPressed();
                }
                setting.setKeyDown(down);
            }
        }
    }
}
