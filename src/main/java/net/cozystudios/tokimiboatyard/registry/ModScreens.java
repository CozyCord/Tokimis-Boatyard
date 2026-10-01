package net.cozystudios.tokimiboatyard.registry;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.screen.UpgradedBoatScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreens {
    public static final ScreenHandlerType<UpgradedBoatScreenHandler> UPGRADED_BOAT =
        new ExtendedScreenHandlerType<>(UpgradedBoatScreenHandler::new);

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER,
            new Identifier(TokimiBoatyard.MOD_ID, "upgraded_boat"),
            UPGRADED_BOAT);
    }

    private ModScreens() {}
}
