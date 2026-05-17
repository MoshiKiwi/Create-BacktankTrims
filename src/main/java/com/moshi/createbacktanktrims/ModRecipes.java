package com.moshi.createbacktanktrims;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom recipe serializers registered by this mod. */
public final class ModRecipes {

	private static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
		DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateBacktankTrims.MOD_ID);

	/** Serializer for the {@link AmethystPulseRecipe} special crafting recipe. */
	public static final Supplier<SimpleCraftingRecipeSerializer<AmethystPulseRecipe>> AMETHYST_PULSE =
		REGISTRY.register("amethyst_pulse",
			() -> new SimpleCraftingRecipeSerializer<>(AmethystPulseRecipe::new));

	private ModRecipes() {}

	public static void register(IEventBus modEventBus) {
		REGISTRY.register(modEventBus);
	}
}
