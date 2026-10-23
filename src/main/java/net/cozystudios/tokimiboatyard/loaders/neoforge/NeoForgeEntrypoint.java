package net.cozystudios.tokimiboatyard.loaders.neoforge;

//? if neoforge {
/*import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.compat.ModdedBoatCompat;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.registry.ModEntities;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.cozystudios.tokimiboatyard.registry.ModScreens;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.cozystudios.tokimiboatyard.resource.TokimiPackProvider;
import net.minecraft.server.packs.PackType;

@Mod(TokimiBoatyard.MOD_ID)
public final class NeoForgeEntrypoint {
    public NeoForgeEntrypoint(IEventBus modBus) {
        modBus.addListener(EventPriority.LOWEST, this::registerContent);
        modBus.addListener(this::addBoatItems);
        modBus.addListener(this::addPackFinder);
    }

    private void registerContent(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.ENTITY_TYPE.key())) {
            ModEntities.register();
        } else if (event.getRegistryKey().equals(BuiltInRegistries.ITEM.key())) {
            ModdedBoatCompat.registerAll();
            ModItems.register();
        } else if (event.getRegistryKey().equals(BuiltInRegistries.MENU.key())) {
            ModScreens.register();
        } else if (event.getRegistryKey().equals(BuiltInRegistries.RECIPE_SERIALIZER.key())) {
            ModRecipes.register();
            TokimiBoatyard.LOGGER.info("Tokimi's Boatyard initialized");
        }
    }

    private void addBoatItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.TOOLS_AND_UTILITIES) return;
        for (BoatTier tier : BoatTier.values()) {
            for (Item item : ModItems.getAll(tier)) event.accept(new ItemStack(item));
        }
    }

    private void addPackFinder(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(new TokimiPackProvider());
        }
    }
}
*///?}
