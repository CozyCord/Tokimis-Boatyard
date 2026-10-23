package net.cozystudios.tokimiboatyard.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.util.ModIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21.1 {
/*import net.minecraft.server.packs.PackLocationInfo;
*///? }
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
//? if >=1.21.1 {
/*import java.util.Optional;
*///? }
import java.util.Set;

public class DynamicResourcePack implements PackResources {
    public static final String NAME = "tokimiboatyard_dynamic";
    //? if >=1.21.1 {
    /*private static final int PACK_FORMAT = 34;
    *///? } else {
    private static final int PACK_FORMAT = 15;
    //? }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String ITEM_MODEL_PREFIX = "models/item/";
    private static final String LANG_PATH = "lang/en_us.json";

    private volatile @Nullable Map<ResourceLocation, byte[]> cache;

    private Map<ResourceLocation, byte[]> cache() {
        Map<ResourceLocation, byte[]> local = cache;
        if (local != null) return local;
        synchronized (this) {
            if (cache != null) return cache;
            cache = buildAll();
            return cache;
        }
    }

    public void invalidate() {
        cache = null;
    }

    private Map<ResourceLocation, byte[]> buildAll() {
        Map<ResourceLocation, byte[]> files = new HashMap<>();
        Map<String, String> langEntries = new LinkedHashMap<>();

        for (BoatTier tier : BoatTier.values()) {
            for (BoatWood wood : BoatWoodRegistry.all()) {
                String tokimiPath = wood.tokimiPath();
                String itemPath = tier.id() + "_" + tokimiPath + "_chest_boat";

                langEntries.put(
                    "item." + TokimiBoatyard.MOD_ID + "." + itemPath,
                    tier.displayName() + " " + wood.displayName() + (wood.isRaft() ? " Chest Raft" : " Chest Boat")
                );

                if (!wood.isRaft()) {
                    ResourceLocation modelId = ModIds.of(TokimiBoatyard.MOD_ID, ITEM_MODEL_PREFIX + itemPath + ".json");
                    byte[] modelBytes = buildItemModel(wood.itemIconLayer(), tier, wood.overlayShape()).getBytes(StandardCharsets.UTF_8);
                    files.put(modelId, modelBytes);
                }
            }
        }

        byte[] langBytes = GSON.toJson(langEntries).getBytes(StandardCharsets.UTF_8);
        files.put(ModIds.of(TokimiBoatyard.MOD_ID, LANG_PATH), langBytes);
        return files;
    }

    private String buildItemModel(ResourceLocation layer0, BoatTier tier, String shape) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("parent", "item/generated");
        Map<String, String> textures = new LinkedHashMap<>();
        textures.put("layer0", layer0.toString());
        String overlayPath = shape.equals("boat")
            ? "item/overlay/" + tier.id()
            : "item/overlay/" + shape + "/" + tier.id();
        textures.put("layer1", TokimiBoatyard.MOD_ID + ":" + overlayPath);
        model.put("textures", textures);
        return GSON.toJson(model);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... segments) {
        if (segments.length == 1 && segments[0].equals("pack.mcmeta")) {
            String json = "{\"pack\":{\"pack_format\":" + PACK_FORMAT + ",\"description\":\"Tokimi's Boatyard dynamic resources\"}}";
            return () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        byte[] data = cache().get(id);
        if (data == null) return null;
        return () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput consumer) {
        if (type != PackType.CLIENT_RESOURCES) return;
        if (!TokimiBoatyard.MOD_ID.equals(namespace)) return;
        for (Map.Entry<ResourceLocation, byte[]> entry : cache().entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!TokimiBoatyard.MOD_ID.equals(id.getNamespace())) continue;
            if (!id.getPath().startsWith(prefix)) continue;
            byte[] data = entry.getValue();
            consumer.accept(id, () -> new ByteArrayInputStream(data));
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(TokimiBoatyard.MOD_ID) : Set.of();
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if (metaReader.getMetadataSectionName().equals("pack")) {
            @SuppressWarnings("unchecked")
            //? if >=1.21.1 {
            /*T meta = (T) new PackMetadataSection(Component.literal("Tokimi's Boatyard dynamic"), PACK_FORMAT, Optional.empty());
            *///? } else {
            T meta = (T) new PackMetadataSection(Component.literal("Tokimi's Boatyard dynamic"), PACK_FORMAT);
            //? }
            return meta;
        }
        return null;
    }

    @Override
    public String packId() {
        return NAME;
    }

    //? if >=1.21.1 {
    /*@Override
    public PackLocationInfo location() {
        return new PackLocationInfo(NAME, Component.literal("Tokimi's Boatyard Dynamic"), PackSource.BUILT_IN, Optional.empty());
    }
    *///? }

    @Override
    public void close() {
        cache = null;
    }
}
