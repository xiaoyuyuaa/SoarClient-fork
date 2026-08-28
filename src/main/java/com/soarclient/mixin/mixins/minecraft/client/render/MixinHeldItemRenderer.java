package com.soarclient.mixin.mixins.minecraft.client.render;

import com.soarclient.management.mod.impl.player.OldAnimationsMod;
import com.soarclient.management.mod.impl.render.CustomHandMod;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Shadow
    private void applySwingOffset(MatrixStack poseStack, Arm arm, float attack) {
        throw new AssertionError();
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void applyHandTransforms(AbstractClientPlayerEntity player, float frameInterp, float xRot,
            Hand hand, float attack, ItemStack itemStack, float inverseArmHeight,
            MatrixStack poseStack, OrderedRenderCommandQueue collector, int lightCoords, CallbackInfo ci) {
        CustomHandMod handMod = CustomHandMod.getInstance();
        if (handMod != null && handMod.isEnabled()) {
            poseStack.translate(handMod.getX(), handMod.getY(), handMod.getZ());
            poseStack.scale(handMod.getScale(), handMod.getScale(), handMod.getScale());
        }

        OldAnimationsMod animations = OldAnimationsMod.getInstance();
        if (animations == null || !animations.isEnabled()) {
            return;
        }
        if (itemStack.getItem() instanceof BowItem && animations.isOldBow()) {
            poseStack.translate(0.0F, 0.05F, 0.04F);
            poseStack.scale(0.93F, 1.0F, 1.0F);
        } else if (itemStack.getItem() instanceof FishingRodItem && animations.isOldRod()) {
            poseStack.translate(0.08F, -0.027F, -0.33F);
            poseStack.scale(0.93F, 1.0F, 1.0F);
        }

        if (animations.isOldBreaking() && player.isUsingItem()) {
            UseAction useAnimation = itemStack.getUseAction();
            if (useAnimation == UseAction.EAT || useAnimation == UseAction.DRINK
                    || useAnimation == UseAction.BLOCK || useAnimation == UseAction.BOW) {
                Arm arm = hand == Hand.MAIN_HAND
                        ? player.getMainArm()
                        : player.getMainArm().getOpposite();
                applySwingOffset(poseStack, arm, attack);
            }
        }
    }
}
