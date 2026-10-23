package net.cozystudios.tokimiboatyard.loaders.fabric;

//? if fabric {
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.fabricmc.api.ModInitializer;

public final class FabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        TokimiBoatyard.initialize();
    }
}
//?}
