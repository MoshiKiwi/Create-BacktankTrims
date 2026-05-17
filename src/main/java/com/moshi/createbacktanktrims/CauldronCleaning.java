package com.moshi.createbacktanktrims;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Adds a water-cauldron interaction for Create's diving helmets: right-clicking a water cauldron
 * with a dyed or pulsing helmet strips the {@code dyed_color} and {@link ModComponents#AMETHYST_PULSE}
 * components, emits green particles, and lowers the water — while leaving the armor trim intact.
 */
public final class CauldronCleaning {

	private CauldronCleaning() {}

	/** Registers the interaction. Must run on the main thread once Create's items exist. */
	public static void register() {
		registerFor(DivingHelmets.COPPER);
		registerFor(DivingHelmets.NETHERITE);
	}

	private static void registerFor(net.minecraft.resources.ResourceLocation id) {
		Item item = BuiltInRegistries.ITEM.get(id);
		if (item != Items.AIR)
			CauldronInteraction.WATER.map().put(item, CauldronCleaning::clean);
	}

	private static ItemInteractionResult clean(BlockState state, Level level, BlockPos pos,
											   Player player, InteractionHand hand, ItemStack stack) {
		boolean hasDye = stack.has(DataComponents.DYED_COLOR);
		boolean hasPulse = stack.has(ModComponents.AMETHYST_PULSE.get());
		if (!hasDye && !hasPulse)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // nothing to clean

		if (!level.isClientSide) {
			stack.remove(DataComponents.DYED_COLOR);
			stack.remove(ModComponents.AMETHYST_PULSE.get());
			// The armor trim component is deliberately left untouched.

			if (level instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
					12, 0.22, 0.18, 0.22, 0.0);
			}
			level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.5F);
			LayeredCauldronBlock.lowerFillLevel(state, level, pos);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}
}
