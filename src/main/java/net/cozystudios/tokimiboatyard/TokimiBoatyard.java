package net.cozystudios.tokimiboatyard;

import net.cozystudios.tokimiboatyard.registry.ModEntities;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.cozystudios.tokimiboatyard.registry.ModScreens;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokimiBoatyard implements ModInitializer {
    public static final String MOD_ID = "tokimiboatyard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEntities.register();
        ModItems.register();
        ModScreens.register();
        ModRecipes.register();
        LOGGER.info("Tokimi's Boatyard initialized");
    }
}
