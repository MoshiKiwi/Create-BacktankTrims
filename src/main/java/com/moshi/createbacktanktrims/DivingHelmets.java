package com.moshi.createbacktanktrims;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Registry ids for Create's diving helmets, and a shared helper to recognise them. */
public final class DivingHelmets {

	public static final ResourceLocation COPPER =
		new ResourceLocation("create", "copper_diving_helmet");
	public static final ResourceLocation NETHERITE =
		new ResourceLocation("create", "netherite_diving_helmet");

	private DivingHelmets() {}

	/** @return {@code true} if the stack is one of Create's copper/netherite diving helmets. */
	public static boolean isDivingHelmet(ItemStack stack) {
		if (stack.isEmpty())
			return false;
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return COPPER.equals(id) || NETHERITE.equals(id);
	}
}
