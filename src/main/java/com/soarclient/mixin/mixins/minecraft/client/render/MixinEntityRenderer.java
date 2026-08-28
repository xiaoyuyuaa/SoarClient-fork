package com.soarclient.mixin.mixins.minecraft.client.render;

import com.soarclient.Soar;
import com.soarclient.management.hypixel.api.HypixelUser;
import com.soarclient.management.mod.impl.misc.HypixelMod;
import com.soarclient.utils.server.Server;
import com.soarclient.utils.server.ServerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

	@Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
	private void renderLevelHead(S state, MatrixStack matrices, OrderedRenderCommandQueue queue,
			CameraRenderState camera, CallbackInfo ci) {
		if (!(state instanceof PlayerEntityRenderState playerState) || playerState.displayName == null
				|| !ServerUtils.isJoin(Server.HYPIXEL)) {
			return;
		}

		HypixelMod mod = HypixelMod.getInstance();
		MinecraftClient client = MinecraftClient.getInstance();
		if (mod == null || !mod.isEnabled() || !mod.getLevelHeadSetting().isEnabled() || client.world == null) {
			return;
		}
		if (!(client.world.getEntityById(playerState.id) instanceof AbstractClientPlayerEntity player)) {
			return;
		}

		HypixelUser user = Soar.getInstance().getHypixelManager()
				.getByUuid(player.getUuid().toString().replace("-", ""));
		if (user == null) {
			return;
		}

		Text levelText = Text.literal("Level: ").formatted(Formatting.AQUA)
				.append(Text.literal(user.getNetworkLevel()).formatted(Formatting.YELLOW));
		queue.submitLabel(matrices, playerState.nameLabelPos, playerState.playerName != null ? -20 : -10,
				levelText, !playerState.sneaking, playerState.light, playerState.squaredDistanceToCamera, camera);
	}
}
