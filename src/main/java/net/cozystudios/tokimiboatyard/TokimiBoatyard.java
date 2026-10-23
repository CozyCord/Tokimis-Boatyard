package net.cozystudios.tokimiboatyard;

import net.cozystudios.tokimiboatyard.compat.ModdedBoatCompat;
import net.cozystudios.tokimiboatyard.registry.ModEntities;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.cozystudios.tokimiboatyard.registry.ModScreens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TokimiBoatyard {
    public static final String MOD_ID = "tokimiboatyard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initialize() {
        ModEntities.register();
        ModdedBoatCompat.registerAll();
        ModItems.register();
        ModScreens.register();
        ModRecipes.register();
        LOGGER.info("Tokimi's Boatyard initialized");
    }

    private TokimiBoatyard() {}
}
