package com.moshi.createbacktanktrims.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moshi.createbacktanktrims.ModComponents;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Recolours the eye-visor of Create's copper and netherite diving helmets to the helmet's dye
 * colour.
 *
 * <p>Create's diving helmets render through the vanilla {@code HumanoidArmorLayer} on the standard
 * armor head model, so a mask derived from Create's own {@code *_diving_layer_1.png} lines up with
 * the helmet UV exactly. Each mask holds only the visor pixels, desaturated to greyscale; rendering
 * the head model with that mask and the dye colour as a tint produces the recoloured visor.
 *
 * <p>The layer does nothing until the helmet carries a {@code minecraft:dyed_color} component, so
 * an undyed helmet keeps Create's original visor colour. When dyed, the mask is fully opaque over
 * the visor region and therefore hides the original colour underneath.
 *
 * <p>The {@code visorModel} is the same size as the helmet's own armor model; the visor is nudged
 * clear of the helmet surface by {@link #visorOffsetX}/{@link #visorOffsetY}/{@link #visorOffsetZ}
 * (live-tunable in dev with {@code /cbtvisor}) so it does not z-fight coplanar with the helmet.
 */
public class HelmetVisorLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	private static final ResourceLocation COPPER_HELMET_ID =
		ResourceLocation.fromNamespaceAndPath("create", "copper_diving_helmet");
	private static final ResourceLocation NETHERITE_HELMET_ID =
		ResourceLocation.fromNamespaceAndPath("create", "netherite_diving_helmet");

	private static final ResourceLocation COPPER_VISOR_TEXTURE =
		ResourceLocation.fromNamespaceAndPath("createbacktanktrims",
			"textures/models/armor/copper_diving_helmet_visor.png");
	private static final ResourceLocation NETHERITE_VISOR_TEXTURE =
		ResourceLocation.fromNamespaceAndPath("createbacktanktrims",
			"textures/models/armor/netherite_diving_helmet_visor.png");

	// ---------------------------------------------------------------------------------------
	// Fine-tuning knobs. These nudge the visor overlay clear of the helmet surface so it is
	// visible instead of buried/z-fighting. Units are model units (1/16 of a block); negative
	// Z pushes the visor forward, out of the face. Adjust live in-game with /cbtvisor, then
	// copy the values you like back here as the new defaults.
	// ---------------------------------------------------------------------------------------
	public static volatile float visorOffsetX = 0.0F;
	public static volatile float visorOffsetY = 0.0275F;
	public static volatile float visorOffsetZ = -0.00035F;
	/** Uniform scale applied to the visor model. 1.0 = helmet-sized. */
	public static volatile float visorScale = 1.11F;

	/** The amethyst pulse breathes the visor brightness between these two multipliers. */
	private static final float PULSE_DARKEST = 0.85F;
	private static final float PULSE_BRIGHTEST = 1.15F;

	/** An armor model (helmet-sized) used purely to stamp the visor mask. */
	private final HumanoidModel<T> visorModel;

	public HelmetVisorLayer(RenderLayerParent<T, M> parent, HumanoidModel<T> visorModel) {
		super(parent);
		this.visorModel = visorModel;
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer, int light, T entity,
					   float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
					   float netHeadYaw, float headPitch) {
		if (entity.getPose() == Pose.SLEEPING)
			return;

		ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
		if (stack.isEmpty())
			return;

		ResourceLocation visorTexture = visorTextureFor(stack);
		if (visorTexture == null)
			return;

		// Only override the visor once the helmet has actually been dyed; an undyed helmet
		// keeps Create's original visor colour.
		DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
		if (dyed == null)
			return;

		if (!(getParentModel() instanceof HumanoidModel<?> parentModel))
			return;

		// Pose the visor model exactly like HumanoidArmorLayer poses the head armor model.
		copyProperties(parentModel, visorModel);
		setHeadVisibility(visorModel);

		VertexConsumer vc = buffer.getBuffer(RenderType.armorCutoutNoCull(visorTexture));
		// Greyscale mask * colour = the recoloured visor, drawn opaque over the original.
		int color = dyed.rgb() & 0xFFFFFF;
		if (stack.has(ModComponents.AMETHYST_PULSE.get())) {
			// Amethyst-crafted helmets pulse; one renamed "jeb_" rainbow-cycles instead.
			color = isJebNamed(stack) ? rainbowColor(ageInTicks) : pulseColor(color, ageInTicks);
		}
		int argb = 0xFF000000 | color;

		// Rotate the offset into the head's own frame, using the head part's posed rotation
		// (which already accounts for players looking around and armor-stand head poses), so
		// the visor stays glued to the face instead of drifting when the head looks up/down.
		Vector3f offset = new Vector3f(visorOffsetX, visorOffsetY, visorOffsetZ);
		new Quaternionf()
			.rotateZYX(visorModel.head.zRot, visorModel.head.yRot, visorModel.head.xRot)
			.transform(offset);

		// Head pivot in block space (model parts are built in 1/16-block units). Scaling
		// around the pivot rather than the model origin keeps the visor aligned identically
		// on players and armor stands, whose models seat the head at different heights.
		float px = visorModel.head.x / 16.0F;
		float py = visorModel.head.y / 16.0F;
		float pz = visorModel.head.z / 16.0F;

		ms.pushPose();
		ms.translate(offset.x, offset.y, offset.z);   // nudge clear of the helmet
		ms.translate(px, py, pz);
		ms.scale(visorScale, visorScale, visorScale); // scale around the head pivot
		ms.translate(-px, -py, -pz);
		visorModel.renderToBuffer(ms, vc, light, OverlayTexture.NO_OVERLAY, argb);
		ms.popPose();
	}

	/** @return the visor mask for the given head item, or {@code null} if it is not a diving helmet. */
	private static ResourceLocation visorTextureFor(ItemStack stack) {
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (COPPER_HELMET_ID.equals(id))
			return COPPER_VISOR_TEXTURE;
		if (NETHERITE_HELMET_ID.equals(id))
			return NETHERITE_VISOR_TEXTURE;
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void copyProperties(HumanoidModel<?> from, HumanoidModel to) {
		from.copyPropertiesTo(to);
	}

	/** Mirrors HumanoidArmorLayer#setPartVisibility for the HEAD slot. */
	private static void setHeadVisibility(HumanoidModel<?> model) {
		model.setAllVisible(false);
		model.head.visible = true;
		model.hat.visible = true;
	}

	/**
	 * Oscillates a colour's brightness with a slow sine wave, so an amethyst-pulsing visor
	 * appears to breathe through lighter and darker tones of the same hue.
	 */
	private static int pulseColor(int rgb, float ageInTicks) {
		// 2*pi / 100 ticks -> one full pulse roughly every five seconds.
		float wave = (Mth.sin(ageInTicks * 0.0628F) + 1.0F) * 0.5F; // 0..1
		float factor = PULSE_DARKEST + (PULSE_BRIGHTEST - PULSE_DARKEST) * wave;
		int r = clampChannel(Math.round(((rgb >> 16) & 0xFF) * factor));
		int g = clampChannel(Math.round(((rgb >> 8) & 0xFF) * factor));
		int b = clampChannel(Math.round((rgb & 0xFF) * factor));
		return (r << 16) | (g << 8) | b;
	}

	private static int clampChannel(int value) {
		return value < 0 ? 0 : (value > 255 ? 255 : value);
	}

	/** Cycles through the full hue wheel — the easter egg for a helmet renamed "jeb_". */
	private static int rainbowColor(float ageInTicks) {
		float hue = (ageInTicks * 0.01F) % 1.0F;
		return Mth.hsvToRgb(hue, 1.0F, 1.0F);
	}

	private static boolean isJebNamed(ItemStack stack) {
		Component name = stack.get(DataComponents.CUSTOM_NAME);
		return name != null && "jeb_".equals(name.getString());
	}
}
