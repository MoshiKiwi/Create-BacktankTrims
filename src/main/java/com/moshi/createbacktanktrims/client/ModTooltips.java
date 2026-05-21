package com.moshi.createbacktanktrims.client;

import com.moshi.createbacktanktrims.CreateBacktankTrims;
import com.moshi.createbacktanktrims.HelmetData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds a tooltip line to helmets whose visor is pulsing (see {@link HelmetData}). */
@Mod.EventBusSubscriber(modid = CreateBacktankTrims.MOD_ID, value = Dist.CLIENT)
public final class ModTooltips {

	private ModTooltips() {}

	@SubscribeEvent
	static void onItemTooltip(ItemTooltipEvent event) {
		if (HelmetData.isPulsing(event.getItemStack()))
			event.getToolTip().add(Component.literal("Pulsating")
				.withStyle(ChatFormatting.LIGHT_PURPLE));
	}
}
