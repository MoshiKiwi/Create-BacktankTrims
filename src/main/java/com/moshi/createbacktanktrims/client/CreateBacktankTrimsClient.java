package com.moshi.createbacktanktrims.client;

import com.moshi.createbacktanktrims.CreateBacktankTrims;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers {@link BacktankTrimLayer} and {@link HelmetVisorLayer} onto every humanoid
 * living-entity renderer (players and mobs), mirroring how Create registers its own
 * {@code BacktankArmorLayer}.
 */
@Mod.EventBusSubscriber(modid = CreateBacktankTrims.MOD_ID,
	bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CreateBacktankTrimsClient {

	private CreateBacktankTrimsClient() {}

	@SubscribeEvent
	@SuppressWarnings({"unchecked", "rawtypes"})
	static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		// Player renderers (one per skin model: "default" and "slim").
		for (String skin : event.getSkins()) {
			addLayer(event.getSkin(skin), event);
		}
		// Every other living entity renderer (zombies, skeletons, armor stands, ...).
		// Forge 1.20.1's AddLayers has no getEntityTypes(), so we walk the entity registry.
		// getRenderer is generically bound to LivingEntity, hence the raw type; it returns
		// null for non-living entities, which addLayer ignores.
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			addLayer(event.getRenderer((EntityType) type), event);
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

		// The visor uses the same inner-armor model as the helmet; HelmetVisorLayer nudges
		// it clear of the helmet surface with a small offset (see HelmetVisorLayer).
		HumanoidModel visorModel = new HumanoidModel<>(
			event.getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
		livingRenderer.addLayer(new HelmetVisorLayer(livingRenderer, visorModel));
	}
}
