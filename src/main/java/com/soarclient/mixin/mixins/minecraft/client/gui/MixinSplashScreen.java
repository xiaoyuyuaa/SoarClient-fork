package com.soarclient.mixin.mixins.minecraft.client.gui;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class MixinSplashScreen {

	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private boolean reloading;
	@Shadow @Final private Consumer<Optional<Throwable>> exceptionHandler;
	@Unique private long soar_animationStartTime = -1L;
	@Unique private long soar_reloadStartTime = -1L;
	@Unique private static final long MAX_RELOAD_TIME = 15_000L;
	@Unique private static final Identifier CUSTOM_LOGO = Identifier.of("soar", "logo.png");
	@Unique private static final int LOGO_ACTUAL_SIZE = 1080;
	@Unique private static final float LOGO_SCALE = 0.15f;
	@Unique private static final long ANIMATION_TOTAL_TIME = 4500L;
	@Unique private static final long FADE_DURATION = 500L;
	@Unique private static final int PROGRESS_BAR_HEIGHT = 2;
	@Unique private int lastWindowWidth = -1;
	@Unique private int lastWindowHeight = -1;
	@Unique private boolean skipNextFrame;

	@Unique
	private void ensureLogoTexture() {
		var textureManager = client.getTextureManager();
		if (textureManager.getTexture(CUSTOM_LOGO) == null) {
			textureManager.registerTexture(CUSTOM_LOGO, new ResourceTexture(CUSTOM_LOGO));
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void soar_takeOverAndRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		int width = context.getScaledWindowWidth();
		int height = context.getScaledWindowHeight();
		if (lastWindowWidth != -1 && lastWindowHeight != -1
				&& (width != lastWindowWidth || height != lastWindowHeight)) {
			skipNextFrame = true;
		}
		lastWindowWidth = width;
		lastWindowHeight = height;
		if (skipNextFrame || width <= 0 || height <= 0) {
			skipNextFrame = false;
			return;
		}

		ci.cancel();
		ensureLogoTexture();
		if (reloading) {
			renderReloading(context, width, height);
		} else {
			renderInitial(context, width, height);
		}
	}

	@Unique
	private void renderReloading(DrawContext context, int width, int height) {
		if (soar_reloadStartTime == -1L) {
			soar_reloadStartTime = Util.getMeasuringTimeMs();
		}
		soar_animationStartTime = -1L;
		long elapsed = Util.getMeasuringTimeMs() - soar_reloadStartTime;
		if (elapsed > MAX_RELOAD_TIME) {
			finishReload();
			return;
		}

		context.fill(0, 0, width, height, 0xFF000000);
		blitLogo(context, width, height, 1.0f);
		long cycle = 1500L;
		float progress = (float) (Util.getMeasuringTimeMs() % cycle) / cycle;
		int barWidth = Math.max(1, width / 3);
		int start = (int) ((width + barWidth) * progress) - barWidth;
		int end = start + barWidth;
		int barY = height - PROGRESS_BAR_HEIGHT;
		context.fill(0, barY, width, height, 0xFF303030);
		context.fill(Math.max(0, start), barY, Math.min(width, end), height, 0xFFFFFFFF);
	}

	@Unique
	private void renderInitial(DrawContext context, int width, int height) {
		soar_reloadStartTime = -1L;
		if (soar_animationStartTime == -1L) {
			soar_animationStartTime = Util.getMeasuringTimeMs();
		}
		long elapsed = Util.getMeasuringTimeMs() - soar_animationStartTime;
		if (elapsed >= ANIMATION_TOTAL_TIME) {
			finishReload();
			return;
		}

		long fadeStart = ANIMATION_TOTAL_TIME - FADE_DURATION;
		float alpha = elapsed > fadeStart ? 1.0f - (float) (elapsed - fadeStart) / FADE_DURATION : 1.0f;
		alpha = Math.max(0.0f, alpha);
		context.fill(0, 0, width, height, 0xFF000000);
		blitLogo(context, width, height, alpha);

		int barY = height - PROGRESS_BAR_HEIGHT;
		int progressWidth = (int) (width * Math.min(1.0f, (float) elapsed / ANIMATION_TOTAL_TIME));
		context.fill(0, barY, width, height, 0xFF303030);
		context.fill(0, barY, progressWidth, height, ((int) (alpha * 255.0f) << 24) | 0xFFFFFF);
	}

	@Unique
	private void blitLogo(DrawContext context, int width, int height, float alpha) {
		int scaledSize = (int) (LOGO_ACTUAL_SIZE * LOGO_SCALE);
		int logoX = (width - scaledSize) / 2;
		int logoY = (height - scaledSize) / 2;
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		try {
			matrices.translate(logoX + scaledSize / 2.0f, logoY + scaledSize / 2.0f);
			matrices.scale(LOGO_SCALE);
			matrices.translate(-LOGO_ACTUAL_SIZE / 2.0f, -LOGO_ACTUAL_SIZE / 2.0f);
			context.drawTexture(
					RenderPipelines.GUI_TEXTURED,
					CUSTOM_LOGO,
					0, 0, 0.0f, 0.0f,
					LOGO_ACTUAL_SIZE, LOGO_ACTUAL_SIZE,
					LOGO_ACTUAL_SIZE, LOGO_ACTUAL_SIZE,
					((int) (alpha * 255.0f) << 24) | 0xFFFFFF
			);
		} finally {
			matrices.popMatrix();
		}
	}

	@Unique
	private void finishReload() {
		client.setOverlay(null);
		exceptionHandler.accept(Optional.empty());
		soar_animationStartTime = -1L;
		soar_reloadStartTime = -1L;
	}
}
