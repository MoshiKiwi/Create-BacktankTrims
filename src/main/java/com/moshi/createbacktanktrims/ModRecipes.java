package com.moshi.createbacktanktrims;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Custom recipe serializers registered by this mod. */
public final class ModRecipes {

	private static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
		DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateBacktankTrims.MOD_ID);

	/** Serializer for the {@link AmethystPulseRecipe} special crafting recipe. */
	public static final RegistryObject<SimpleCraftingRecipeSerializer<AmethystPulseRecipe>> AMETHYST_PULSE =
		REGISTRY.register("amethyst_pulse",
			() -> new SimpleCraftingRecipeSerializer<>(AmethystPulseRecipe::new));

	/** Serializer for the {@link VisorDyeRecipe} special crafting recipe. */
	public static final RegistryObject<SimpleCraftingRecipeSerializer<VisorDyeRecipe>> VISOR_DYE =
		REGISTRY.register("visor_dye",
			() -> new SimpleCraftingRecipeSerializer<>(VisorDyeRecipe::new));

	private ModRecipes() {}

	public static void register(IEventBus modEventBus) {
		REGISTRY.register(modEventBus);
	}
}
