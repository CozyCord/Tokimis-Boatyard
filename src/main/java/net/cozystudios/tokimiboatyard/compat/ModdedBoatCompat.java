package net.cozystudios.tokimiboatyard.compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class ModdedBoatCompat {

    private static final Set<String> ALLOWED_NAMESPACES = Set.of(
        "natures_spirit",
        "hexalia",
        "biomeswevegone",
        "regions_unexplored",
        "deeperdarker",
        "beachparty",
        "meadow",
        "vinery"
    );

    public static void registerAll() {
        registerVanillaWoods();
        scanEnumExtensions();
        scanModdedItems();
        TokimiBoatyard.LOGGER.info("Registered {} boat wood variants", BoatWoodRegistry.all().size());
        for (BoatWood w : BoatWoodRegistry.all()) {
            TokimiBoatyard.LOGGER.info("  wood={} iconLayer={} entity={} baseItem={}",
                w.id(), w.itemIconLayer(), w.entityTexture(),
                w.baseChestBoatItem() == null ? "null" : Registries.ITEM.getId(w.baseChestBoatItem()));
        }
    }

    private static Identifier resolveLayer0(String modid, String wood, Identifier fallback) {
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modid);
        if (modOpt.isEmpty()) return fallback;
        Optional<Path> pathOpt = modOpt.get().findPath("assets/" + modid + "/models/item/" + wood + "_chest_boat.json");
        if (pathOpt.isEmpty()) return fallback;
        try {
            String content = Files.readString(pathOpt.get());
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            if (json.has("textures")) {
                JsonObject textures = json.getAsJsonObject("textures");
                if (textures.has("layer0")) {
                    String layer0 = textures.get("layer0").getAsString();
                    Identifier parsed = Identifier.tryParse(layer0);
                    if (parsed != null) return parsed;
                }
            }
        } catch (IOException | RuntimeException ignore) {}
        return fallback;
    }

    private static void registerVanillaWoods() {
        registerVanilla(BoatEntity.Type.OAK,       Items.OAK_CHEST_BOAT,       "Oak");
        registerVanilla(BoatEntity.Type.SPRUCE,    Items.SPRUCE_CHEST_BOAT,    "Spruce");
        registerVanilla(BoatEntity.Type.BIRCH,     Items.BIRCH_CHEST_BOAT,     "Birch");
        registerVanilla(BoatEntity.Type.JUNGLE,    Items.JUNGLE_CHEST_BOAT,    "Jungle");
        registerVanilla(BoatEntity.Type.ACACIA,    Items.ACACIA_CHEST_BOAT,    "Acacia");
        registerVanilla(BoatEntity.Type.DARK_OAK,  Items.DARK_OAK_CHEST_BOAT,  "Dark Oak");
        registerVanilla(BoatEntity.Type.MANGROVE,  Items.MANGROVE_CHEST_BOAT,  "Mangrove");
        registerVanilla(BoatEntity.Type.CHERRY,    Items.CHERRY_CHEST_BOAT,    "Cherry");
        registerVanilla(BoatEntity.Type.BAMBOO,    Items.BAMBOO_CHEST_RAFT,    "Bamboo");
    }

    private static void registerVanilla(BoatEntity.Type type, Item baseItem, String displayName) {
        String name = type.getName();
        boolean isRaft = type == BoatEntity.Type.BAMBOO;
        String iconSuffix = isRaft ? "_chest_raft" : "_chest_boat";
        BoatWoodRegistry.register(new BoatWood(
            new Identifier("minecraft", name),
            new Identifier("minecraft", "textures/entity/chest_boat/" + name + ".png"),
            new Identifier("minecraft", "item/" + name + iconSuffix),
            type,
            displayName,
            isRaft,
            baseItem
        ));
    }

    private static void scanEnumExtensions() {
        for (BoatEntity.Type type : BoatEntity.Type.values()) {
            if (BoatWoodRegistry.getByVanillaType(type) != null) continue;

            String rawName = type.getName();
            String namespace = "minecraft";
            String path = rawName;
            String nsCandidate = findAllowedNamespacePrefix(rawName);
            if (nsCandidate != null) {
                namespace = nsCandidate;
                path = rawName.substring(nsCandidate.length() + 1);
            }

            Identifier id = new Identifier(namespace, path);
            Item baseItem = Registries.ITEM.get(new Identifier(namespace, path + "_chest_boat"));
            if (baseItem == Items.AIR) baseItem = null;

            Identifier fallbackIcon = baseItem != null
                ? new Identifier(namespace, "item/" + path + "_chest_boat")
                : new Identifier("minecraft", "item/oak_chest_boat");
            Identifier itemIcon = baseItem != null
                ? resolveLayer0(namespace, path, fallbackIcon)
                : fallbackIcon;

            BoatWoodRegistry.register(new BoatWood(
                id,
                new Identifier("minecraft", "textures/entity/chest_boat/" + rawName + ".png"),
                itemIcon,
                type,
                prettify(path),
                false,
                baseItem
            ));
        }
    }

    private static String findAllowedNamespacePrefix(String rawName) {
        for (String ns : ALLOWED_NAMESPACES) {
            String prefix = ns + "_";
            if (rawName.startsWith(prefix) && rawName.length() > prefix.length()) {
                return ns;
            }
        }
        return null;
    }

    private static void scanModdedItems() {
        for (Identifier itemId : Registries.ITEM.getIds()) {
            if (!ALLOWED_NAMESPACES.contains(itemId.getNamespace())) continue;
            String path = itemId.getPath();
            if (!path.endsWith("_chest_boat")) continue;

            String wood = path.substring(0, path.length() - "_chest_boat".length());
            Identifier woodId = new Identifier(itemId.getNamespace(), wood);
            if (BoatWoodRegistry.has(woodId)) continue;

            Item baseItem = Registries.ITEM.get(itemId);
            if (baseItem == Items.AIR) continue;

            BoatEntity.Type matchedType = findEnumTypeByName(itemId.getNamespace() + "_" + wood);

            Identifier fallbackIcon = new Identifier(itemId.getNamespace(), "item/" + wood + "_chest_boat");
            Identifier itemIcon = resolveLayer0(itemId.getNamespace(), wood, fallbackIcon);

            BoatWoodRegistry.register(new BoatWood(
                woodId,
                new Identifier(itemId.getNamespace(), "textures/entity/chest_boat/" + wood + ".png"),
                itemIcon,
                matchedType,
                prettify(wood),
                false,
                baseItem
            ));
        }
    }

    private static BoatEntity.Type findEnumTypeByName(String name) {
        for (BoatEntity.Type t : BoatEntity.Type.values()) {
            if (t.getName().equals(name)) return t;
        }
        return null;
    }

    static String prettify(String snake) {
        String[] parts = snake.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) sb.append(parts[i].substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    private ModdedBoatCompat() {}
}
