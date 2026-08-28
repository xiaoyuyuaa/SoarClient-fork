package com.soarclient.mixin.interfaces;

import net.minecraft.util.Hand;

public interface IMixinLivingEntity {
	void fakeSwingHand(Hand hand);
}
