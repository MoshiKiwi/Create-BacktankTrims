package com.moshi.createbacktanktrims.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

/**
 * Renders the icon armor stand ({@link DevCommands#ICON_ENTITY_NAME}) on its own into a
 * transparent off-screen buffer and writes the result to a PNG.
 *
 * <p>The render reuses {@link InventoryScreen#renderEntityInInventory} — the same path
 * Minecraft uses to draw entities in GUI slots — which is orthographic, giving a clean
 * isometric look with no perspective distortion. Because we render into a freshly cleared
 * off-screen target, only the target armor stand appears in the output; the rest of the
 * world is never drawn.
 *
 * <p>Tune {@link #SIZE}, {@link #ENTITY_SCALE} and the pose angles below to taste.
 */
public final class IconCapture {

	/** Output image is {@code SIZE x SIZE} pixels. Modrinth icons display at 512x512. */
	private static final int SIZE = 512;

	/** Bigger = the armor stand fills more of the frame. Tune in-game. */
	private static final float ENTITY_SCALE = 220.0F;

	/** Camera tilt, in degrees. Negative looks down on the stand (top-down isometric). */
	private static final float PITCH_DEGREES = -30.0F;

	/** Horizontal spin, in degrees — -135 shows a front-corner 3/4 view. */
	private static final float YAW_DEGREES = -135.0F;

	private IconCapture() {}

	/** Renders the icon armor stand to a PNG. Returns a chat-ready status message. */
	public static String capture() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null)
			return "§cNo world loaded.";

		ArmorStand target = findIconStand(level);
		if (target == null)
			return "§cNo armor stand named \"" + DevCommands.ICON_ENTITY_NAME + "\" found.";

		try {
			Path out = renderToFile(mc, target);
			return "§aIcon written to " + out;
		} catch (IOException e) {
			return "§cFailed to write icon: " + e.getMessage();
		}
	}

	private static ArmorStand findIconStand(ClientLevel level) {
		for (Entity e : level.entitiesForRendering()) {
			if (e instanceof ArmorStand stand
				&& stand.getCustomName() != null
				&& DevCommands.ICON_ENTITY_NAME.equals(stand.getCustomName().getString())) {
				return stand;
			}
		}
		return null;
	}

	private static Path renderToFile(Minecraft mc, ArmorStand stand) throws IOException {
		TextureTarget fbo = new TextureTarget(SIZE, SIZE, true, Minecraft.ON_OSX);
		fbo.setClearColor(0.0F, 0.0F, 0.0F, 0.0F); // transparent background
		fbo.clear(Minecraft.ON_OSX);
		fbo.bindWrite(true);

		// GUI-style orthographic projection -> true isometric, no perspective distortion.
		Matrix4f projection = new Matrix4f().setOrtho(0.0F, SIZE, SIZE, 0.0F, -10000.0F, 10000.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

		// Pose: flip upright (GUI Y points down), tilt down, then spin to the 3/4 view.
		Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
		pose.rotateX((float) Math.toRadians(PITCH_DEGREES));
		pose.rotateY((float) Math.toRadians(YAW_DEGREES));
		Quaternionf cameraOrientation = new Quaternionf().rotateX((float) Math.toRadians(PITCH_DEGREES));

		// Translation lifts the entity so its body centre sits at the target screen point.
		Vector3f translation = new Vector3f(0.0F, stand.getBbHeight() / 2.0F, 0.0F);

		GuiGraphics gui = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
		InventoryScreen.renderEntityInInventory(
			gui, SIZE / 2.0F, SIZE * 0.5F, ENTITY_SCALE, translation, pose, cameraOrientation, stand);
		gui.flush();

		RenderSystem.restoreProjectionMatrix();
		fbo.unbindWrite();

		// Read the off-screen colour buffer back into CPU memory and write it out.
		NativeImage image = new NativeImage(SIZE, SIZE, false);
		fbo.bindRead();
		image.downloadTexture(0, false);
		image.flipY(); // OpenGL textures are bottom-up
		fbo.unbindRead();

		Path out = mc.gameDirectory.toPath()
			.resolve("screenshots")
			.resolve("cbt-icon-" + LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".png");
		Files.createDirectories(out.getParent());
		image.writeToFile(out);

		image.close();
		fbo.destroyBuffers();
		mc.getMainRenderTarget().bindWrite(true); // restore the normal render target

		return out;
	}
}
