package com.moshi.createbacktanktrims;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

/**
 * Standalone fix for Create issue #6213: Create's diving gear and backtanks cannot be trimmed,
 * and when forced to carry a trim the visuals never update.
 *
 * <p>This mod does two things:
 * <ul>
 *   <li>Ships a {@code minecraft:trimmable_armor} item tag that re-adds the six Create armor
 *       pieces Create removes in {@code CreateRegistrateTags}. Because this mod declares an
 *       {@code AFTER} dependency on Create, our datapack is processed last and the additions win.
 *       That alone re-enables trimming in the smithing table and makes the five vanilla-rendered
 *       pieces show their trims.</li>
 *   <li>Adds a client render layer ({@link com.moshi.createbacktanktrims.client.BacktankTrimLayer})
 *       that draws the trim for the netherite backtank, whose diving suit is drawn through Create's
 *       custom {@code LayeredArmorItem} renderer and therefore never gets the vanilla trim pass.</li>
 * </ul>
 */
@Mod(CreateBacktankTrims.MOD_ID)
public class CreateBacktankTrims {
	public static final String MOD_ID = "createbacktanktrims";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreateBacktankTrims() {
		LOGGER.info("Create: Backtank Trims loaded - Create armor can now be trimmed.");
	}
}
