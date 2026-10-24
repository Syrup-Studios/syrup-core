package net.syrupstudios.syrupcore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SyrupCore {
	public static final String MOD_ID = "syrup_core";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void initialize() {
		LOGGER.info("Syrup Core initialized!");
	}
}
