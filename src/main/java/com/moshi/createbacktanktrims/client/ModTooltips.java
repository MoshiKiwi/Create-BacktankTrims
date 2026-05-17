package com.moshi.createbacktanktrims.client;

import com.moshi.createbacktanktrims.CreateBacktankTrims;
import com.moshi.createbacktanktrims.ModComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Adds a tooltip line to helmets carrying the {@link ModComponents#AMETHYST_PULSE} component. */
@EventBusSubscriber(modid = CreateBacktankTrims.MOD_ID, value = Dist.CLIENT)
public final class ModTooltips {

	private ModTooltips() {}

	@SubscribeEvent
	static void onItemTooltip(ItemTooltipEvent event) {
		if (event.getItemStack().has(ModComponents.AMETHYST_PULSE.get()))
			event.getToolTip().add(Component.literal("Pulsating")
				.withStyle(ChatFormatting.LIGHT_PURPLE));
	}
}
