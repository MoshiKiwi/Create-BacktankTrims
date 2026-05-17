package com.moshi.createbacktanktrims;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Special crafting recipe: a diving helmet plus an amethyst shard yields the same helmet — with
 * every component it already carried (dye colour, armor trim, ...) preserved — and the
 * {@link ModComponents#AMETHYST_PULSE} component added so its visor pulses.
 *
 * <p>It must be a special recipe because a plain shapeless recipe produces a fixed result and
 * cannot copy the input helmet's existing components onto the output.
 */
public class AmethystPulseRecipe extends CustomRecipe {

	public AmethystPulseRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		ItemStack helmet = ItemStack.EMPTY;
		boolean foundShard = false;
		for (int i = 0; i < input.size(); i++) {
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
		return foundShard && !helmet.isEmpty() && !helmet.has(ModComponents.AMETHYST_PULSE.get());
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (DivingHelmets.isDivingHelmet(stack)) {
				ItemStack result = stack.copyWithCount(1); // keeps dye, trim, everything
				result.set(ModComponents.AMETHYST_PULSE.get(), Boolean.TRUE);
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
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.AMETHYST_PULSE.get();
	}
}
