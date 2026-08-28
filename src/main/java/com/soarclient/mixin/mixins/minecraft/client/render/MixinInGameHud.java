package com.soarclient.mixin.mixins.minecraft.client.render;

import com.soarclient.Soar;
import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderGameOverlayEvent;
import com.soarclient.event.client.RenderHotbarEvent;
import com.soarclient.event.impl.Render3DEvent;
import com.soarclient.management.mod.impl.hud.ModernHotBarMod;
import com.soarclient.management.mod.impl.hud.PotionStatusMod;
import com.soarclient.management.mod.impl.player.OldAnimationsMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    /**
     * Overrides the vanilla heart rendering method to support old animation mod features.
     * When the old animations mod is enabled and heart flashing is disabled, it will override the vanilla flashing logic.
     *
     * @reason To provide customizable heart rendering behavior for old animation preferences
     * @author EldoDebug
     * @param context The drawing context
     * @param type The type of heart to render
     * @param x The x coordinate to render at
     * @param y The y coordinate to render at
     * @param hardcore Whether in hardcore mode
     * @param blinking Whether the heart should blink (vanilla logic)
     * @param half Whether to render half a heart
     */
	@Overwrite
	private void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half) {
		OldAnimationsMod mod = OldAnimationsMod.getInstance();

		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, type.getTexture(hardcore, half, (!mod.isEnabled() || !mod.isDisableHeartFlash()) && blinking), x, y, 9, 9);
	}

	@Inject(method = "renderMainHud", at = @At("TAIL"))
	private void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		EventBus.getInstance().post(new Render3DEvent(tickCounter.getTickProgress(false), context));
		EventBus.getInstance().post(new RenderGameOverlayEvent(context));
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		PotionStatusMod mod = PotionStatusMod.getInstance();
		if (mod != null && mod.shouldDisableVanillaDisplay()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		RenderHotbarEvent event = new RenderHotbarEvent(context, tickCounter.getTickProgress(false));
		EventBus.getInstance().post(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
	private void onRenderStatusBars(DrawContext context, CallbackInfo ci) {
		ModernHotBarMod mod = Soar.getInstance().getModManager().getMod(ModernHotBarMod.class);
		if (mod != null && mod.isEnabled()) {
			ci.cancel();
		}
	}

	@Redirect(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/bar/Bar;renderBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void renderExperienceBar(Bar bar, DrawContext context, RenderTickCounter tickCounter) {
		if (!(bar instanceof ExperienceBar) || !isModernHotBarEnabled()) {
			bar.renderBar(context, tickCounter);
		}
	}

	@Redirect(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/bar/Bar;drawExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V"))
	private void renderExperienceLevel(DrawContext context, TextRenderer textRenderer, int level) {
		if (!isModernHotBarEnabled()) {
			Bar.drawExperienceLevel(context, textRenderer, level);
		}
	}

	private boolean isModernHotBarEnabled() {
		ModernHotBarMod mod = Soar.getInstance().getModManager().getMod(ModernHotBarMod.class);
		return mod != null && mod.isEnabled();
	}
}
