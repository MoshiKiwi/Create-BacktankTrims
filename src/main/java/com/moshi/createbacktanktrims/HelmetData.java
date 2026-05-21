package com.moshi.createbacktanktrims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Stores this mod's per-helmet state in item NBT, under a single {@code createbacktanktrims}
 * compound.
 *
 * <p>Minecraft 1.20.1 has no data-component system, so the 1.21 {@code minecraft:dyed_color}
 * component and the mod's own {@code amethyst_pulse} component are both replaced by NBT keys
 * here. Item NBT is network-synchronised automatically, so the client renderer sees these
 * values with no extra plumbing.
 */
public final class HelmetData {

	private static final String ROOT = "createbacktanktrims";
	private static final String VISOR_COLOR = "visor_color";
	private static final String PULSE = "pulse";

	private HelmetData() {}

	/** @return {@code true} if the helmet carries a visor colour. */
	public static boolean hasVisorColor(ItemStack stack) {
		CompoundTag tag = stack.getTagElement(ROOT);
		return tag != null && tag.contains(VISOR_COLOR, Tag.TAG_INT);
	}

	/** @return the RGB visor colour, or {@code -1} if none is set. */
	public static int getVisorColor(ItemStack stack) {
		CompoundTag tag = stack.getTagElement(ROOT);
		return tag != null && tag.contains(VISOR_COLOR, Tag.TAG_INT) ? tag.getInt(VISOR_COLOR) : -1;
	}

	public static void setVisorColor(ItemStack stack, int rgb) {
		stack.getOrCreateTagElement(ROOT).putInt(VISOR_COLOR, rgb & 0xFFFFFF);
	}

	/** @return {@code true} if the helmet's visor should slowly pulse. */
	public static boolean isPulsing(ItemStack stack) {
		CompoundTag tag = stack.getTagElement(ROOT);
		return tag != null && tag.getBoolean(PULSE);
	}

	public static void setPulsing(ItemStack stack, boolean pulsing) {
		stack.getOrCreateTagElement(ROOT).putBoolean(PULSE, pulsing);
	}

	/** Strips the visor colour and the pulse flag (the armor trim is stored separately). */
	public static void clear(ItemStack stack) {
		stack.removeTagKey(ROOT);
	}
}
