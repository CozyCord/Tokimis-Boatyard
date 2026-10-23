package net.cozystudios.tokimiboatyard.compat;

import net.cozystudios.tokimiboatyard.util.ModIds;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
//?} else if neoforge {
/*import net.neoforged.fml.ModList;
*///?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
                w.baseChestBoatItem() == null ? "null" : BuiltInRegistries.ITEM.getKey(w.baseChestBoatItem()));
        }
    }

    private static ResourceLocation resolveLayer0(String modid, String wood, ResourceLocation fallback) {
        //? if fabric {
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modid);
        if (modOpt.isEmpty()) return fallback;
        Optional<Path> pathOpt = modOpt.get().findPath("assets/" + modid + "/models/item/" + wood + "_chest_boat.json");
        if (pathOpt.isEmpty()) return fallback;
        //?} else if neoforge {
        /*Optional<Path> pathOpt = ModList.get().getModContainerById(modid)
            .map(container -> container.getModInfo().getOwningFile().getFile()
                .findResource("assets/" + modid + "/models/item/" + wood + "_chest_boat.json"));
        if (pathOpt.isEmpty()) return fallback;
        *///?}
        try {
            String content = Files.readString(pathOpt.get());
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            if (json.has("textures")) {
                JsonObject textures = json.getAsJsonObject("textures");
                if (textures.has("layer0")) {
                    String layer0 = textures.get("layer0").getAsString();
                    ResourceLocation parsed = ResourceLocation.tryParse(layer0);
                    if (parsed != null) return parsed;
                }
            }
        } catch (IOException | RuntimeException ignore) {}
        return fallback;
    }

    private static void registerVanillaWoods() {
        registerVanilla(Boat.Type.OAK,       Items.OAK_CHEST_BOAT,       "Oak");
        registerVanilla(Boat.Type.SPRUCE,    Items.SPRUCE_CHEST_BOAT,    "Spruce");
        registerVanilla(Boat.Type.BIRCH,     Items.BIRCH_CHEST_BOAT,     "Birch");
        registerVanilla(Boat.Type.JUNGLE,    Items.JUNGLE_CHEST_BOAT,    "Jungle");
        registerVanilla(Boat.Type.ACACIA,    Items.ACACIA_CHEST_BOAT,    "Acacia");
        registerVanilla(Boat.Type.DARK_OAK,  Items.DARK_OAK_CHEST_BOAT,  "Dark Oak");
        registerVanilla(Boat.Type.MANGROVE,  Items.MANGROVE_CHEST_BOAT,  "Mangrove");
        registerVanilla(Boat.Type.CHERRY,    Items.CHERRY_CHEST_BOAT,    "Cherry");
        registerVanilla(Boat.Type.BAMBOO,    Items.BAMBOO_CHEST_RAFT,    "Bamboo");
    }

    private static void registerVanilla(Boat.Type type, Item baseItem, String displayName) {
        String name = type.getName();
        boolean isRaft = type == Boat.Type.BAMBOO;
        String iconSuffix = isRaft ? "_chest_raft" : "_chest_boat";
        BoatWoodRegistry.register(new BoatWood(
            ModIds.of("minecraft", name),
            ModIds.of("minecraft", "textures/entity/chest_boat/" + name + ".png"),
            ModIds.of("minecraft", "item/" + name + iconSuffix),
            type,
            displayName,
            isRaft,
            baseItem
        ));
    }

    private static void scanEnumExtensions() {
        for (Boat.Type type : Boat.Type.values()) {
            if (BoatWoodRegistry.getByVanillaType(type) != null) continue;

            String rawName = type.getName();
            String namespace = "minecraft";
            String path = rawName;
            String nsCandidate = findAllowedNamespacePrefix(rawName);
            if (nsCandidate != null) {
                namespace = nsCandidate;
                path = rawName.substring(nsCandidate.length() + 1);
            }

            ResourceLocation id = ModIds.of(namespace, path);
            Item baseItem = BuiltInRegistries.ITEM.get(ModIds.of(namespace, path + "_chest_boat"));
            if (baseItem == Items.AIR) baseItem = null;

            ResourceLocation fallbackIcon = baseItem != null
                ? ModIds.of(namespace, "item/" + path + "_chest_boat")
                : ModIds.of("minecraft", "item/oak_chest_boat");
            ResourceLocation itemIcon = baseItem != null
                ? resolveLayer0(namespace, path, fallbackIcon)
                : fallbackIcon;

            BoatWoodRegistry.register(new BoatWood(
                id,
                ModIds.of("minecraft", "textures/entity/chest_boat/" + rawName + ".png"),
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
        for (ResourceLocation itemId : BuiltInRegistries.ITEM.keySet()) {
            if (!ALLOWED_NAMESPACES.contains(itemId.getNamespace())) continue;
            String path = itemId.getPath();
            if (!path.endsWith("_chest_boat")) continue;

            String wood = path.substring(0, path.length() - "_chest_boat".length());
            ResourceLocation woodId = ModIds.of(itemId.getNamespace(), wood);
            if (BoatWoodRegistry.has(woodId)) continue;

            Item baseItem = BuiltInRegistries.ITEM.get(itemId);
            if (baseItem == Items.AIR) continue;

            Boat.Type matchedType = findEnumTypeByName(itemId.getNamespace() + "_" + wood);

            ResourceLocation fallbackIcon = ModIds.of(itemId.getNamespace(), "item/" + wood + "_chest_boat");
            ResourceLocation itemIcon = resolveLayer0(itemId.getNamespace(), wood, fallbackIcon);

            BoatWoodRegistry.register(new BoatWood(
                woodId,
                ModIds.of(itemId.getNamespace(), "textures/entity/chest_boat/" + wood + ".png"),
                itemIcon,
                matchedType,
                prettify(wood),
                false,
                baseItem
            ));
        }
    }

    private static Boat.Type findEnumTypeByName(String name) {
        for (Boat.Type t : Boat.Type.values()) {
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
