package com.moshi.createbacktanktrims.client;

import java.lang.reflect.Field;
import java.util.Map;

import com.moshi.createbacktanktrims.CreateBacktankTrims;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
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
	static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		// Player renderers (one per skin model: "default" and "slim").
		for (String skin : event.getSkins()) {
			addLayer(event.getSkin(skin), event);
		}
		// Every other entity renderer (zombies, skeletons, armor stands, ...).
		// Forge 1.20.1's AddLayers exposes no entity-type list, and event.getRenderer(EntityType)
		// casts unconditionally to LivingEntityRenderer — it throws ClassCastException for any
		// non-living entity (e.g. a NoopRenderer). So read the event's own renderer map and let
		// addLayer filter out the renderers we don't care about.
		for (EntityRenderer<?> renderer : entityRenderers(event).values()) {
			addLayer(renderer, event);
		}
	}

	/**
	 * The {@code AddLayers} entity-renderer map, which Forge 1.20.1 keeps private. Reflective
	 * access is stable here: {@code EntityRenderersEvent} is one of Forge's own classes and is
	 * not obfuscated, so the field is named {@code renderers} in both dev and production.
	 */
	@SuppressWarnings("unchecked")
	private static Map<EntityType<?>, EntityRenderer<?>> entityRenderers(EntityRenderersEvent.AddLayers event) {
		try {
			Field field = EntityRenderersEvent.AddLayers.class.getDeclaredField("renderers");
			field.setAccessible(true);
			return (Map<EntityType<?>, EntityRenderer<?>>) field.get(event);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Cannot read EntityRenderersEvent.AddLayers#renderers", e);
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
