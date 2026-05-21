package com.moshi.createbacktanktrims;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Special crafting recipe that dyes a Create diving helmet's visor: a diving helmet plus one or
 * more dyes yields the same helmet with a blended visor colour, every other bit of NBT (armor
 * trim, pulse flag) preserved.
 *
 * <p>The 1.21 build makes the helmets dyeable by shipping a {@code minecraft:dyeable} item tag —
 * a data-driven system that does not exist in 1.20.1, where dyeing is the hard-coded
 * {@code DyeableLeatherItem} interface that Create's helmets do not implement. So this recipe
 * reproduces vanilla's {@code ArmorDyeRecipe} colour-blending and writes the result into the
 * mod's own {@link HelmetData} NBT.
 */
public class VisorDyeRecipe extends CustomRecipe {

	public VisorDyeRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		ItemStack helmet = ItemStack.EMPTY;
		boolean foundDye = false;
		for (int i = 0; i < input.getContainerSize(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty())
				continue;
			if (stack.getItem() instanceof DyeItem) {
				foundDye = true;
			} else if (DivingHelmets.isDivingHelmet(stack)) {
				if (!helmet.isEmpty())
					return false; // only one helmet
				helmet = stack;
			} else {
				return false; // any other item disqualifies the grid
			}
		}
		return foundDye && !helmet.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		ItemStack helmet = ItemStack.EMPTY;
		List<DyeItem> dyes = new ArrayList<>();
		for (int i = 0; i < input.getContainerSize(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty())
				continue;
			if (stack.getItem() instanceof DyeItem dye) {
				dyes.add(dye);
			} else if (DivingHelmets.isDivingHelmet(stack)) {
				if (!helmet.isEmpty())
					return ItemStack.EMPTY;
				helmet = stack;
			} else {
				return ItemStack.EMPTY;
			}
		}
		if (helmet.isEmpty() || dyes.isEmpty())
			return ItemStack.EMPTY;

		ItemStack result = helmet.copyWithCount(1); // keeps trim, pulse flag, everything
		HelmetData.setVisorColor(result, blend(helmet, dyes));
		return result;
	}

	/**
	 * Vanilla {@code ArmorDyeRecipe} colour-averaging: each dye — and the helmet's current visor
	 * colour, if it already has one — contributes its RGB; the mean is then rescaled so the
	 * brightest channel matches the average of the inputs' brightest channels.
	 */
	private static int blend(ItemStack helmet, List<DyeItem> dyes) {
		int[] sum = new int[3];
		int brightnessSum = 0;
		int count = 0;

		if (HelmetData.hasVisorColor(helmet)) {
			int color = HelmetData.getVisorColor(helmet);
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;
			brightnessSum += Math.max(r, Math.max(g, b));
			sum[0] += r;
			sum[1] += g;
			sum[2] += b;
			count++;
		}

		for (DyeItem dye : dyes) {
			float[] rgb = dye.getDyeColor().getTextureDiffuseColors();
			int r = (int) (rgb[0] * 255.0F);
			int g = (int) (rgb[1] * 255.0F);
			int b = (int) (rgb[2] * 255.0F);
			brightnessSum += Math.max(r, Math.max(g, b));
			sum[0] += r;
			sum[1] += g;
			sum[2] += b;
			count++;
		}

		int r = sum[0] / count;
		int g = sum[1] / count;
		int b = sum[2] / count;
		float avgBrightness = (float) brightnessSum / count;
		float maxChannel = Math.max(r, Math.max(g, b));
		r = (int) (r * avgBrightness / maxChannel);
		g = (int) (g * avgBrightness / maxChannel);
		b = (int) (b * avgBrightness / maxChannel);
		return (r << 16) | (g << 8) | b;
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
		return ModRecipes.VISOR_DYE.get();
	}
}
