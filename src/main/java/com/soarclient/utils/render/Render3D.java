package com.soarclient.utils.render;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderWorldEvent;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

/**
 * World-space rendering for mods.
 *
 * <p>Shapes are collected during Fabric's extraction phase so Minecraft can draw
 * them with the rest of the frame. Mods draw by listening for {@link RenderWorldEvent}.
 */
public final class Render3D {

	private static final float DEFAULT_LINE_WIDTH = 2.0f;

	private static boolean initialized;

	private Render3D() {
	}

	public static void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		WorldRenderEvents.END_EXTRACTION.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null || client.player == null || context.worldState() == null
					|| context.worldState().cameraRenderState == null) {
				return;
			}

			try (var ignored = context.worldRenderer().startDrawingGizmos()) {
				EventBus.getInstance().post(new RenderWorldEvent(new Renderer()));
			}
		});
	}

	public static final class Renderer {

		private Renderer() {
		}

		public void line(Vec3d from, Vec3d to, int color) {
			line(from, to, color, DEFAULT_LINE_WIDTH);
		}

		public void line(Vec3d from, Vec3d to, int color, float lineWidth) {
			GizmoDrawing.line(from, to, color, lineWidth);
		}

		public void quad(Vec3d a, Vec3d b, Vec3d c, Vec3d d, int color) {
			quad(a, b, c, d, color, DEFAULT_LINE_WIDTH);
		}

		public void quad(Vec3d a, Vec3d b, Vec3d c, Vec3d d, int color, float lineWidth) {
			line(a, b, color, lineWidth);
			line(b, c, color, lineWidth);
			line(c, d, color, lineWidth);
			line(d, a, color, lineWidth);
		}

		public void box(Box box, int color) {
			box(box, color, DEFAULT_LINE_WIDTH);
		}

		public void box(Box box, int color, float lineWidth) {
			Vec3d p000 = new Vec3d(box.minX, box.minY, box.minZ);
			Vec3d p001 = new Vec3d(box.minX, box.minY, box.maxZ);
			Vec3d p010 = new Vec3d(box.minX, box.maxY, box.minZ);
			Vec3d p011 = new Vec3d(box.minX, box.maxY, box.maxZ);
			Vec3d p100 = new Vec3d(box.maxX, box.minY, box.minZ);
			Vec3d p101 = new Vec3d(box.maxX, box.minY, box.maxZ);
			Vec3d p110 = new Vec3d(box.maxX, box.maxY, box.minZ);
			Vec3d p111 = new Vec3d(box.maxX, box.maxY, box.maxZ);

			quad(p000, p001, p101, p100, color, lineWidth);
			quad(p010, p011, p111, p110, color, lineWidth);
			line(p000, p010, color, lineWidth);
			line(p001, p011, color, lineWidth);
			line(p100, p110, color, lineWidth);
			line(p101, p111, color, lineWidth);
		}
	}
}
