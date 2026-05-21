package com.moshi.createbacktanktrims.client;

import java.util.Locale;

import com.moshi.createbacktanktrims.CreateBacktankTrims;
import com.moshi.createbacktanktrims.HelmetData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds tooltip lines for this mod's per-helmet state (see {@link HelmetData}).
 *
 * <p>The visor-colour line re-creates vanilla's {@code minecraft:dyed_color} tooltip: on 1.21+
 * the colour rides a data component and vanilla draws this line itself, but 1.20.1 keeps the
 * colour in the mod's own NBT, so the same line is added by hand here — the hex code with
 * advanced tooltips (F3+H), a plain "Dyed" line otherwise.
 */
@Mod.EventBusSubscriber(modid = CreateBacktankTrims.MOD_ID, value = Dist.CLIENT)
public final class ModTooltips {

	private ModTooltips() {}

	@SubscribeEvent
	static void onItemTooltip(ItemTooltipEvent event) {
		if (HelmetData.hasVisorColor(event.getItemStack()))
			event.getToolTip().add(visorColorLine(event));
		if (HelmetData.isPulsing(event.getItemStack()))
			event.getToolTip().add(Component.literal("Pulsating")
				.withStyle(ChatFormatting.LIGHT_PURPLE));
	}

	/** Mirrors {@code DyedItemColor#addToTooltip}: "Color: #RRGGBB" when advanced, else "Dyed". */
	private static Component visorColorLine(ItemTooltipEvent event) {
		if (event.getFlags().isAdvanced()) {
			int rgb = HelmetData.getVisorColor(event.getItemStack());
			return Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", rgb))
				.withStyle(ChatFormatting.GRAY);
		}
		return Component.translatable("item.dyed")
			.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
	}
}
