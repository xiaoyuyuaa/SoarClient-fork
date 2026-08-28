package com.soarclient.mixin.mixins.minecraft.client.render;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.event.client.RenderSkiaPostEvent;
import com.soarclient.skia.Skia;
import com.soarclient.skia.context.SkiaContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

	@Inject(method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = At.Shift.BEFORE))
	private void renderSkiaBeforeGui(RenderTickCounter deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		if (!SkiaContext.beginFrame()) {
			SkiaContext.discardPending();
			return;
		}

		try {
			Skia.save();
			try {
				Skia.scale((float) MinecraftClient.getInstance().getWindow().getScaleFactor());
				EventBus.getInstance().post(new RenderSkiaEvent());
			} finally {
				Skia.restore();
			}
		} finally {
			SkiaContext.endFrame();
		}
	}

	@Inject(method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = At.Shift.AFTER))
	private void renderSkiaAfterGui(RenderTickCounter deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		if (!SkiaContext.beginFrame()) {
			SkiaContext.discardPending();
			return;
		}

		try {
			SkiaContext.drawPending();
			Skia.save();
			try {
				Skia.scale((float) MinecraftClient.getInstance().getWindow().getScaleFactor());
				EventBus.getInstance().post(new RenderSkiaPostEvent());
			} finally {
				Skia.restore();
			}
		} finally {
			SkiaContext.endFrame();
		}
	}
}
