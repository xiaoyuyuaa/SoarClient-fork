package com.soarclient.mixin.mixins.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.soarclient.Soar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractClientPlayerEntity.class, priority = 2000)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity {

    @Unique
    private boolean enableCape;

    @Unique
    private boolean shownCape = false;

    public MixinAbstractClientPlayerEntity(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }


    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    public void getSkin(CallbackInfoReturnable<SkinTextures> cir) {
        ClientPlayerEntity localPlayer = MinecraftClient.getInstance().player;
        if (localPlayer == null) {
            return;
        }

        boolean isSameUuid = this.getUuid().equals(localPlayer.getUuid());
        boolean isSameName = this.getName().getString().equals(localPlayer.getName().getString());

        if (!isSameUuid || !isSameName) {
            return;
        }

        Identifier customCape = Soar.getInstance().getCapeManager().getSelectedCapeTexture();
        if (customCape != null) {
            SkinTextures current = cir.getReturnValue();
            cir.setReturnValue(new SkinTextures(
                current.body(),
                new AssetInfo.TextureAssetInfo(customCape, customCape),
                current.elytra(),
                current.model(),
                current.secure()
            ));
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (PLAYER_MODE_CUSTOMIZATION_ID.equals(data)) {
            boolean showCape = isModelPartVisible(PlayerModelPart.CAPE);
            if (showCape != shownCape) {
                shownCape = showCape;
            }
        }
    }

    @Unique
    public void enableCapeNextTick() {
        enableCape = true;
    }
}
