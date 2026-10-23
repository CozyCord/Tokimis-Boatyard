package net.cozystudios.tokimiboatyard.registry;

import net.cozystudios.tokimiboatyard.util.ModIds;
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
//? if fabric
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ModItems {
    private static final Map<BoatTier, Map<ResourceLocation, UpgradedBoatItem>> BOATS = new EnumMap<>(BoatTier.class);

    public static void register() {
        for (BoatTier tier : BoatTier.values()) {
            Map<ResourceLocation, UpgradedBoatItem> byWood = new HashMap<>();
            for (BoatWood wood : BoatWoodRegistry.all()) {
                Item.Properties settings = new Item.Properties().stacksTo(1);
                if (tier.fireproof()) settings.fireResistant();
                UpgradedBoatItem item = new UpgradedBoatItem(tier, wood.id(), settings);
                ResourceLocation id = ModIds.of(TokimiBoatyard.MOD_ID, itemId(tier, wood));
                Registry.register(BuiltInRegistries.ITEM, id, item);
                byWood.put(wood.id(), item);
            }
            BOATS.put(tier, byWood);
        }
        //? if fabric {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            for (BoatTier tier : BoatTier.values()) {
                Map<ResourceLocation, UpgradedBoatItem> byWood = BOATS.get(tier);
                if (byWood == null) continue;
                for (UpgradedBoatItem item : byWood.values()) {
                    entries.accept(item);
                }
            }
        });
        //?}
    }

    public static UpgradedBoatItem get(BoatTier tier, ResourceLocation woodId) {
        Map<ResourceLocation, UpgradedBoatItem> byWood = BOATS.get(tier);
        return byWood == null ? null : byWood.get(woodId);
    }

    public static java.util.Collection<UpgradedBoatItem> getAll(BoatTier tier) {
        Map<ResourceLocation, UpgradedBoatItem> byWood = BOATS.get(tier);
        return byWood == null ? java.util.List.of() : byWood.values();
    }

    public static String itemId(BoatTier tier, BoatWood wood) {
        return tier.id() + "_" + wood.tokimiPath() + "_chest_boat";
    }

    private ModItems() {}
}
