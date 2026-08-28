package com.soarclient.gui.api;

import com.soarclient.skia.Skia;
import com.soarclient.skia.context.SkiaContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class SimpleSoarGui {

	protected MinecraftClient client = MinecraftClient.getInstance();
	private final boolean mcScale;

	public SimpleSoarGui(boolean mcScale) {
		this.mcScale = mcScale;
	}

	public void init() {
	}

	public void draw(double mouseX, double mouseY) {
	}

	public void mousePressed(double mouseX, double mouseY, int button) {
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
	}

	public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
	}

	public void charTyped(char chr, int modifiers) {
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
	}

	public Screen build() {
		return new Screen(Text.empty()) {

			@Override
			public void init() {
				SimpleSoarGui.this.init();
			}

			@Override
			public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
				SkiaContext.draw(canvas -> {
					Skia.save();
					if (mcScale) {
						Skia.scale((float) client.getWindow().getScaleFactor());
					}
					SimpleSoarGui.this.draw(mcScale ? mouseX : client.mouse.getX(),
							mcScale ? mouseY : client.mouse.getY());
					Skia.restore();
				});
			}

			@Override
			public boolean mouseClicked(Click event, boolean doubleClick) {
				SimpleSoarGui.this.mousePressed(mcScale ? event.x() : client.mouse.getX(),
						mcScale ? event.y() : client.mouse.getY(), event.button());
				return true;
			}

			@Override
			public boolean mouseReleased(Click event) {
				SimpleSoarGui.this.mouseReleased(mcScale ? event.x() : client.mouse.getX(),
						mcScale ? event.y() : client.mouse.getY(), event.button());
				return true;
			}

			@Override
			public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
				SimpleSoarGui.this.mouseScrolled(mcScale ? mouseX : client.mouse.getX(),
						mcScale ? mouseY : client.mouse.getY(), horizontalAmount, verticalAmount);
				return true;
			}

			@Override
			public boolean keyPressed(KeyInput event) {
				SimpleSoarGui.this.keyPressed(event.key(), event.scancode(), event.modifiers());
				return true;
			}

			@Override
			public boolean charTyped(CharInput event) {
				if (Character.isBmpCodePoint(event.codepoint())) {
					SimpleSoarGui.this.charTyped((char) event.codepoint(), 0);
				}
				return true;
			}

			@Override
			public boolean shouldPause() {
				return false;
			}
		};
	}
}
