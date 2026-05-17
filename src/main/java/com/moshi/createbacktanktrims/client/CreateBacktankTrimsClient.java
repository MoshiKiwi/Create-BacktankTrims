package com.moshi.createbacktanktrims.client;

import com.moshi.createbacktanktrims.CreateBacktankTrims;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Registers {@link BacktankTrimLayer} onto every humanoid living-entity renderer (players and mobs),
 * mirroring how Create registers its own {@code BacktankArmorLayer}.
 */
@EventBusSubscriber(modid = CreateBacktankTrims.MOD_ID, value = Dist.CLIENT)
public final class CreateBacktankTrimsClient {

	private CreateBacktankTrimsClient() {}

	@SubscribeEvent
	static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		// Player renderers (one per skin model: "default" and "slim").
		for (var skin : event.getSkins()) {
			addLayer(event.getSkin(skin), event);
		}
		// Every other living entity renderer (zombies, skeletons, armor stands, ...).
		for (EntityType<?> type : event.getEntityTypes()) {
			addLayer(event.getRenderer(type), event);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void addLayer(EntityRenderer<?> renderer, EntityRenderersEvent.AddLayers event) {
		if (!(renderer instanceof LivingEntityRenderer livingRenderer))
			return;
		if (!(livingRenderer.getModel() instanceof HumanoidModel))
			return;

		HumanoidModel trimModel = new HumanoidModel<>(
			event.getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
		livingRenderer.addLayer(new BacktankTrimLayer(livingRenderer, trimModel));
	}
}
