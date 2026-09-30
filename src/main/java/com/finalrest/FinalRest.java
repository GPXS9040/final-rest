package com.finalrest;

import com.finalrest.block.ModBlocks;
import com.finalrest.block.entity.ModBlockEntities;
import com.finalrest.event.PlayerDeathHandler;
import com.finalrest.event.TombstoneBreakHandler;
import com.finalrest.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinalRest implements ModInitializer {
	public static final String MOD_ID = "final-rest";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		PlayerDeathHandler.register();
		TombstoneBreakHandler.register();

		LOGGER.info("FinalRest inicializado");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}