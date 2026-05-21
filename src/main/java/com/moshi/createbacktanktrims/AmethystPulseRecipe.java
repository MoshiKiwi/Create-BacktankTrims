package com.moshi.createbacktanktrims;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Special crafting recipe: a diving helmet plus an amethyst shard yields the same helmet — with
 * every bit of NBT it already carried (visor colour, armor trim, ...) preserved — and the
 * {@link HelmetData} pulse flag set so its visor pulses.
 *
 * <p>It must be a special recipe because a plain shapeless recipe produces a fixed result and
 * cannot copy the input helmet's existing NBT onto the output.
 */
public class AmethystPulseRecipe extends CustomRecipe {

	public AmethystPulseRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		ItemStack helmet = ItemStack.EMPTY;
		boolean foundShard = false;
		for (int i = 0; i < input.getContainerSize(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty())
				continue;
			if (stack.is(Items.AMETHYST_SHARD)) {
				if (foundShard)
					return false; // only one shard
				foundShard = true;
			} else if (DivingHelmets.isDivingHelmet(stack)) {
				if (!helmet.isEmpty())
					return false; // only one helmet
				helmet = stack;
			} else {
				return false; // any other item disqualifies the grid
			}
		}
		return foundShard && !helmet.isEmpty() && !HelmetData.isPulsing(helmet);
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		for (int i = 0; i < input.getContainerSize(); i++) {
			ItemStack stack = input.getItem(i);
			if (DivingHelmets.isDivingHelmet(stack)) {
				ItemStack result = stack.copyWithCount(1); // keeps visor colour, trim, everything
				HelmetData.setPulsing(result, true);
				return result;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.AMETHYST_PULSE.get();
	}
}
