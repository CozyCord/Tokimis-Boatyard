package net.cozystudios.tokimiboatyard.registry;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ModItems {
    private static final Map<BoatTier, Map<Identifier, UpgradedBoatItem>> BOATS = new EnumMap<>(BoatTier.class);

    public static void register() {
        for (BoatTier tier : BoatTier.values()) {
            Map<Identifier, UpgradedBoatItem> byWood = new HashMap<>();
            for (BoatWood wood : BoatWoodRegistry.all()) {
                Item.Settings settings = new Item.Settings().maxCount(1);
                if (tier.fireproof()) settings.fireproof();
                UpgradedBoatItem item = new UpgradedBoatItem(tier, wood.id(), settings);
                Identifier id = new Identifier(TokimiBoatyard.MOD_ID, itemId(tier, wood));
                Registry.register(Registries.ITEM, id, item);
                byWood.put(wood.id(), item);
            }
            BOATS.put(tier, byWood);
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            for (BoatTier tier : BoatTier.values()) {
                Map<Identifier, UpgradedBoatItem> byWood = BOATS.get(tier);
                if (byWood == null) continue;
                for (UpgradedBoatItem item : byWood.values()) {
                    entries.add(item);
                }
            }
        });
    }

    public static UpgradedBoatItem get(BoatTier tier, Identifier woodId) {
        Map<Identifier, UpgradedBoatItem> byWood = BOATS.get(tier);
        return byWood == null ? null : byWood.get(woodId);
    }

    public static String itemId(BoatTier tier, BoatWood wood) {
        return tier.id() + "_" + wood.tokimiPath() + "_chest_boat";
    }

    private ModItems() {}
}
