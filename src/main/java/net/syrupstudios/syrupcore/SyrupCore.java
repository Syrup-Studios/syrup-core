package net.syrupstudios.syrupcore;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyrupCore implements ModInitializer {
	public static final String MOD_ID = "syrup-core";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Syrup Core initialized!");
	}
}