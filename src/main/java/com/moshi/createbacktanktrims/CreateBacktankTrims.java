package com.moshi.createbacktanktrims;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
 *
 * <p>1.20.1 port: armor trims and the trim component exist here, but data components do not, so
 * per-helmet state is stored in item NBT instead (see {@link HelmetData}).
 */
@Mod(CreateBacktankTrims.MOD_ID)
public class CreateBacktankTrims {
	public static final String MOD_ID = "createbacktanktrims";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreateBacktankTrims() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModRecipes.register(modEventBus);
		modEventBus.addListener(this::commonSetup);
		LOGGER.info("Create: Backtank Trims loaded - Create armor can now be trimmed.");
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		// Cauldron interaction maps are not thread-safe; register on the main thread.
		event.enqueueWork(CauldronCleaning::register);
	}
}
