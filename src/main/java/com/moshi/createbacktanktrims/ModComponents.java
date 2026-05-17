package com.moshi.createbacktanktrims;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom data components registered by this mod. */
public final class ModComponents {

	private static final DeferredRegister<DataComponentType<?>> REGISTRY =
		DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CreateBacktankTrims.MOD_ID);

	/**
	 * Marks a diving helmet whose visor should slowly pulse. Applied by crafting the helmet with
	 * an amethyst shard; the component is network-synchronised so the client renderer can see it.
	 */
	public static final Supplier<DataComponentType<Boolean>> AMETHYST_PULSE =
		REGISTRY.register("amethyst_pulse", () -> DataComponentType.<Boolean>builder()
			.persistent(Codec.BOOL)
			.networkSynchronized(ByteBufCodecs.BOOL)
			.build());

	private ModComponents() {}

	public static void register(IEventBus modEventBus) {
		REGISTRY.register(modEventBus);
	}
}
