package net.cozystudios.tokimiboatyard.registry;

import net.cozystudios.tokimiboatyard.util.ModIds;
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.screen.UpgradedBoatScreenHandler;
//? if fabric
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21.1 {
/*import net.minecraft.network.codec.ByteBufCodecs;
*///? }
import net.minecraft.world.inventory.MenuType;
//? if neoforge
/*import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;*/

public final class ModScreens {
    public static final MenuType<UpgradedBoatScreenHandler> UPGRADED_BOAT =
        //? if fabric && >=1.21.1 {
        /*new ExtendedScreenHandlerType<>(UpgradedBoatScreenHandler::new, ByteBufCodecs.VAR_INT);
        *///?} else if fabric {
        new ExtendedScreenHandlerType<>(UpgradedBoatScreenHandler::new);
        //?} else if neoforge {
        /*IMenuTypeExtension.create((syncId, playerInv, buf) ->
            new UpgradedBoatScreenHandler(syncId, playerInv, buf.readVarInt()));
        *///?}

    public static void register() {
        Registry.register(BuiltInRegistries.MENU,
            ModIds.of(TokimiBoatyard.MOD_ID, "upgraded_boat"),
            UPGRADED_BOAT);
    }

    private ModScreens() {}
}
