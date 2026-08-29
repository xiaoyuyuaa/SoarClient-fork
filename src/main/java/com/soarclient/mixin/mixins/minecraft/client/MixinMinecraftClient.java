package com.soarclient.mixin.mixins.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.soarclient.Soar;
import com.soarclient.event.EventBus;
import com.soarclient.event.client.ClientTickEvent;
import com.soarclient.event.client.GameLoopEvent;
import com.soarclient.management.mod.impl.player.HitDelayFixMod;
import com.soarclient.management.mod.impl.player.OldAnimationsMod;
import com.soarclient.mixin.interfaces.IMixinLivingEntity;
import com.soarclient.mixin.interfaces.IMixinMinecraftClient;
import com.soarclient.skia.Skia;
import com.soarclient.skia.context.SkiaContext;
import java.io.File;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftClient.class, priority = 300)
public abstract class MixinMinecraftClient implements IMixinMinecraftClient {

    @Shadow @Final private Window window;
    @Shadow private int attackCooldown;
    @Shadow public GameOptions options;
    @Shadow public HitResult crosshairTarget;
    @Shadow public ClientWorld world;
    @Shadow public ClientPlayerEntity player;

    @Unique
    private File assetDir;

    @Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At("TAIL"))
    private void onInit(RunArgs config, CallbackInfo ci) {
        assetDir = config.directories.assetDir;
        Soar.getInstance().start();
    }

    @Inject(method = "handleBlockBreaking(Z)V", at = @At("HEAD"))
    private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
        OldAnimationsMod mod = OldAnimationsMod.getInstance();
        if (mod == null || !mod.isEnabled() || !mod.isOldBreaking()) {
            return;
        }
        if (!options.attackKey.isPressed() || !options.useKey.isPressed()) {
            return;
        }
        if (!breaking || !(crosshairTarget instanceof BlockHitResult blockHitResult) || world == null || player == null) {
            return;
        }
        if (!world.getBlockState(blockHitResult.getBlockPos()).isAir()) {
            world.spawnBlockBreakingParticle(blockHitResult.getBlockPos(), blockHitResult.getSide());
            ((IMixinLivingEntity) player).fakeSwingHand(Hand.MAIN_HAND);
        }
    }

    @Inject(method = "doAttack()Z", at = @At("HEAD"))
    private void onHitDelayFix(CallbackInfoReturnable<Boolean> cir) {
        HitDelayFixMod mod = HitDelayFixMod.getInstance();
        if (mod != null && mod.isEnabled()) {
            attackCooldown = 0;
        }
    }

    @ModifyReturnValue(method = "getWindowTitle()Ljava/lang/String;", at = @At("RETURN"))
    private String customizeWindowTitle(String original) {
        return Soar.getInstance().getName() + " Client v" + Soar.getInstance().getVersion() + " for " + original;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        EventBus.getInstance().post(new ClientTickEvent());
    }

    @Inject(method = "render(Z)V", at = @At("HEAD"))
    private void onGameLoop(boolean advanceGameTime, CallbackInfo ci) {
        EventBus.getInstance().post(new GameLoopEvent());
    }

    @Inject(method = "onResolutionChanged()V", at = @At("HEAD"))
    private void onResolutionChanged(CallbackInfo ci) {
        SkiaContext.invalidate();
    }

    @Inject(method = "close()V", at = @At("HEAD"))
    private void closeSkia(CallbackInfo ci) {
        Soar.getInstance().close();
        Skia.close();
    }

    @Override
    public File getAssetDir() {
        return assetDir;
    }
}
