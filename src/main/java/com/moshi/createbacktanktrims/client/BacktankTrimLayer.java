package com.moshi.createbacktanktrims.client;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

/**
 * Renders an armor trim for the Create netherite backtank.
 *
 * <p>Create's plain armor pieces (diving helmet, diving boots, copper backtank) render through the
 * vanilla {@code HumanoidArmorLayer}, so vanilla draws their trims automatically once the items are
 * trimmable again. The netherite backtank however is a {@code BacktankItem.Layered} and is drawn by
 * Create's custom {@code LayeredArmorItem#renderArmorPiece}, which cancels the vanilla path and
 * never performs the trim pass. This layer reproduces that missing pass.
 *
 * <p>The backtank is matched by registry id so this mod needs no compile-time dependency on Create.
 */
public class BacktankTrimLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	private static final ResourceLocation NETHERITE_BACKTANK_ID =
		new ResourceLocation("create", "netherite_backtank");

	/** A freshly baked outer-armor model used purely to stamp the trim decal. */
	private final HumanoidModel<T> trimModel;

	public BacktankTrimLayer(RenderLayerParent<T, M> parent, HumanoidModel<T> trimModel) {
		super(parent);
		this.trimModel = trimModel;
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer, int light, T entity,
					   float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
					   float netHeadYaw, float headPitch) {
		if (entity.getPose() == Pose.SLEEPING)
			return;

		ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (!isNetheriteBacktank(stack))
			return;

		// 1.20.1 stores the trim in item NBT, read back through ArmorTrim#getTrim.
		Optional<ArmorTrim> trimOpt = ArmorTrim.getTrim(entity.level().registryAccess(), stack);
		if (trimOpt.isEmpty())
			return;
		ArmorTrim trim = trimOpt.get();

		if (!(getParentModel() instanceof HumanoidModel<?> parentModel))
			return;

		// Pose the trim model exactly like HumanoidArmorLayer poses the outer armor model.
		copyProperties(parentModel, trimModel);
		setChestVisibility(trimModel);

		// The netherite backtank uses the vanilla netherite armor material, so the standard
		// trim textures from the vanilla armor_trims atlas apply.
		ResourceLocation textureLoc = trim.outerTexture(ArmorMaterials.NETHERITE);
		TextureAtlasSprite sprite = Minecraft.getInstance()
			.getModelManager()
			.getAtlas(Sheets.ARMOR_TRIMS_SHEET)
			.getSprite(textureLoc);

		VertexConsumer vc = sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet()));
		trimModel.renderToBuffer(ms, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static boolean isNetheriteBacktank(ItemStack stack) {
		return !stack.isEmpty()
			&& NETHERITE_BACKTANK_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void copyProperties(HumanoidModel<?> from, HumanoidModel to) {
		from.copyPropertiesTo(to);
	}

	/** Mirrors HumanoidArmorLayer#setPartVisibility for the CHEST slot. */
	private static void setChestVisibility(HumanoidModel<?> model) {
		model.setAllVisible(false);
		model.body.visible = true;
		model.rightArm.visible = true;
		model.leftArm.visible = true;
	}
}
