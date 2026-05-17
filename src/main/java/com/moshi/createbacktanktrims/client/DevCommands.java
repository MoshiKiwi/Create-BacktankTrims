package com.moshi.createbacktanktrims.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.moshi.createbacktanktrims.CreateBacktankTrims;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Registers the dev-only client commands. All are registered only outside production (i.e. in
 * the dev environment), so they never ship in the released jar.
 *
 * <ul>
 *   <li>{@code /cbticon} — renders the {@value #ICON_ENTITY_NAME} armor stand to a PNG.</li>
 *   <li>{@code /cbtvisor x|y|z|scale <value>} — live-adjusts the helmet visor overlay so it can
 *       be aligned in-game; {@code /cbtvisor} with no arguments prints the current values.</li>
 *   <li>{@code /cbtlook <pitch>} — tilts the head of every armor stand near the player, for
 *       checking that the visor tracks the head when it looks up or down.</li>
 * </ul>
 */
@EventBusSubscriber(modid = CreateBacktankTrims.MOD_ID, value = Dist.CLIENT)
public final class DevCommands {

	/** The custom name the target armor stand must carry for {@code /cbticon} to find it. */
	static final String ICON_ENTITY_NAME = "c:bt-icon";

	/** Radius (blocks) within which {@code /cbtlook} tilts armor stands. */
	private static final double LOOK_RADIUS = 24.0;

	private DevCommands() {}

	@SubscribeEvent
	static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		if (FMLEnvironment.production)
			return; // dev-only tools — never exposed to players in a released jar

		event.getDispatcher().register(
			Commands.literal("cbticon")
				.executes(ctx -> {
					ctx.getSource().sendSuccess(() -> Component.literal(IconCapture.capture()), false);
					return 1;
				}));

		event.getDispatcher().register(
			Commands.literal("cbtvisor")
				.executes(ctx -> {
					ctx.getSource().sendSuccess(() -> Component.literal(visorStatus()), false);
					return 1;
				})
				.then(visorArg("x"))
				.then(visorArg("y"))
				.then(visorArg("z"))
				.then(visorArg("scale")));

		event.getDispatcher().register(
			Commands.literal("cbtlook")
				.then(Commands.argument("pitch", FloatArgumentType.floatArg())
					.executes(DevCommands::tiltNearbyArmorStands)));
	}

	/** Builds a {@code /cbtvisor <key> <value>} sub-command for one tunable. */
	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> visorArg(String key) {
		return Commands.literal(key).then(Commands.argument("value", FloatArgumentType.floatArg())
			.executes(ctx -> setVisor(ctx, key)));
	}

	private static int setVisor(CommandContext<CommandSourceStack> ctx, String key) {
		float value = FloatArgumentType.getFloat(ctx, "value");
		switch (key) {
			case "x" -> HelmetVisorLayer.visorOffsetX = value;
			case "y" -> HelmetVisorLayer.visorOffsetY = value;
			case "z" -> HelmetVisorLayer.visorOffsetZ = value;
			case "scale" -> HelmetVisorLayer.visorScale = value;
			default -> { /* unreachable */ }
		}
		ctx.getSource().sendSuccess(() -> Component.literal("§a" + visorStatus()), false);
		return 1;
	}

	private static String visorStatus() {
		return "visor  x=" + HelmetVisorLayer.visorOffsetX
			+ "  y=" + HelmetVisorLayer.visorOffsetY
			+ "  z=" + HelmetVisorLayer.visorOffsetZ
			+ "  scale=" + HelmetVisorLayer.visorScale;
	}

	/** Sets the head-pose pitch of every armor stand within {@link #LOOK_RADIUS} of the player. */
	private static int tiltNearbyArmorStands(CommandContext<CommandSourceStack> ctx) {
		float pitch = FloatArgumentType.getFloat(ctx, "pitch");
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			ctx.getSource().sendFailure(Component.literal("No world loaded."));
			return 0;
		}
		int count = 0;
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (entity instanceof ArmorStand stand && stand.distanceTo(mc.player) <= LOOK_RADIUS) {
				stand.setHeadPose(new Rotations(pitch, 0.0F, 0.0F));
				count++;
			}
		}
		int n = count;
		ctx.getSource().sendSuccess(
			() -> Component.literal("§aHead pitch " + pitch + " set on " + n + " armor stand(s)"), false);
		return 1;
	}
}
