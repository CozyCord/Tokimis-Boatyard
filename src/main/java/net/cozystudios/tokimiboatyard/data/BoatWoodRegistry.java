package net.cozystudios.tokimiboatyard.data;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class BoatWoodRegistry {
    private static final Map<Identifier, BoatWood> BY_ID = new LinkedHashMap<>();
    private static final Map<Item, BoatWood> BY_BASE_ITEM = new java.util.HashMap<>();
    private static final Map<String, BoatWood> BY_TOKIMI_PATH = new java.util.HashMap<>();

    public static void register(BoatWood wood) {
        Objects.requireNonNull(wood, "wood");
        BY_ID.put(wood.id(), wood);
        BY_TOKIMI_PATH.put(wood.tokimiPath(), wood);
        if (wood.baseChestBoatItem() != null) {
            BY_BASE_ITEM.put(wood.baseChestBoatItem(), wood);
        }
    }

    public static BoatWood get(Identifier id) {
        return BY_ID.get(id);
    }

    public static BoatWood getByTokimiPath(String path) {
        return BY_TOKIMI_PATH.get(path);
    }

    public static BoatWood getByBaseItem(Item item) {
        return BY_BASE_ITEM.get(item);
    }

    public static BoatWood getByVanillaType(BoatEntity.Type type) {
        for (BoatWood w : BY_ID.values()) {
            if (w.vanillaType() == type) return w;
        }
        return null;
    }

    public static Collection<BoatWood> all() {
        return BY_ID.values();
    }

    public static boolean has(Identifier id) {
        return BY_ID.containsKey(id);
    }

    private BoatWoodRegistry() {}
}
