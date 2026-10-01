package net.cozystudios.tokimiboatyard.registry;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    private static final Map<BoatTier, Map<BoatEntity.Type, UpgradedBoatItem>> BOATS = new EnumMap<>(BoatTier.class);

    public static void register() {
        for (BoatTier tier : BoatTier.values()) {
            Map<BoatEntity.Type, UpgradedBoatItem> byWood = new EnumMap<>(BoatEntity.Type.class);
            for (BoatEntity.Type wood : BoatEntity.Type.values()) {
                Item.Settings settings = new Item.Settings().maxCount(1);
                if (tier.fireproof()) settings.fireproof();
                UpgradedBoatItem item = new UpgradedBoatItem(tier, wood, settings);
                Identifier id = new Identifier(TokimiBoatyard.MOD_ID, itemId(tier, wood));
                Registry.register(Registries.ITEM, id, item);
                byWood.put(wood, item);
            }
            BOATS.put(tier, byWood);
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            for (BoatTier tier : BoatTier.values()) {
                for (BoatEntity.Type wood : BoatEntity.Type.values()) {
                    entries.add(BOATS.get(tier).get(wood));
                }
            }
        });
    }

    public static UpgradedBoatItem get(BoatTier tier, BoatEntity.Type wood) {
        return BOATS.get(tier).get(wood);
    }

    public static String itemId(BoatTier tier, BoatEntity.Type wood) {
        return tier.id() + "_" + wood.getName() + "_chest_boat";
    }

    private ModItems() {}
}
