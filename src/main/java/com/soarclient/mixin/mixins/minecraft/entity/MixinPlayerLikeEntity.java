package com.soarclient.mixin.mixins.minecraft.entity;

import com.soarclient.management.mod.impl.player.ForceMainHandMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerLikeEntity.class)
public class MixinPlayerLikeEntity {

	@Inject(method = "getMainArm", at = @At("HEAD"), cancellable = true)
	private void injectGetMainArm(CallbackInfoReturnable<Arm> cir) {
		MinecraftClient client = MinecraftClient.getInstance();
		PlayerEntity clientPlayer = client.player;
		PlayerLikeEntity player = (PlayerLikeEntity) (Object) this;

		if (clientPlayer != null && ForceMainHandMod.getInstance().isEnabled()
				&& player.getId() != clientPlayer.getId()) {
			cir.setReturnValue(ForceMainHandMod.getInstance().isRightHand() ? Arm.RIGHT : Arm.LEFT);
		}
	}
}
