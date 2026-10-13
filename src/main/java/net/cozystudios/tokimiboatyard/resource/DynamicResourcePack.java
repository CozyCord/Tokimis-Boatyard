package net.cozystudios.tokimiboatyard.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DynamicResourcePack implements ResourcePack {
    public static final String NAME = "tokimiboatyard_dynamic";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String ITEM_MODEL_PREFIX = "models/item/";
    private static final String LANG_PATH = "lang/en_us.json";

    private volatile @Nullable Map<Identifier, byte[]> cache;

    private Map<Identifier, byte[]> cache() {
        Map<Identifier, byte[]> local = cache;
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

    private Map<Identifier, byte[]> buildAll() {
        Map<Identifier, byte[]> files = new HashMap<>();
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
                    Identifier modelId = new Identifier(TokimiBoatyard.MOD_ID, ITEM_MODEL_PREFIX + itemPath + ".json");
                    byte[] modelBytes = buildItemModel(wood.itemIconLayer(), tier, wood.overlayShape()).getBytes(StandardCharsets.UTF_8);
                    files.put(modelId, modelBytes);
                }
            }
        }

        byte[] langBytes = GSON.toJson(langEntries).getBytes(StandardCharsets.UTF_8);
        files.put(new Identifier(TokimiBoatyard.MOD_ID, LANG_PATH), langBytes);
        return files;
    }

    private String buildItemModel(Identifier layer0, BoatTier tier, String shape) {
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
    public @Nullable InputSupplier<InputStream> openRoot(String... segments) {
        if (segments.length == 1 && segments[0].equals("pack.mcmeta")) {
            String json = "{\"pack\":{\"pack_format\":15,\"description\":\"Tokimi's Boatyard dynamic resources\"}}";
            return () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    @Override
    public @Nullable InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        if (type != ResourceType.CLIENT_RESOURCES) return null;
        byte[] data = cache().get(id);
        if (data == null) return null;
        return () -> new ByteArrayInputStream(data);
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
        if (type != ResourceType.CLIENT_RESOURCES) return;
        if (!TokimiBoatyard.MOD_ID.equals(namespace)) return;
        for (Map.Entry<Identifier, byte[]> entry : cache().entrySet()) {
            Identifier id = entry.getKey();
            if (!TokimiBoatyard.MOD_ID.equals(id.getNamespace())) continue;
            if (!id.getPath().startsWith(prefix)) continue;
            byte[] data = entry.getValue();
            consumer.accept(id, () -> new ByteArrayInputStream(data));
        }
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return type == ResourceType.CLIENT_RESOURCES ? Set.of(TokimiBoatyard.MOD_ID) : Set.of();
    }

    @Override
    public @Nullable <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
        if (metaReader.getKey().equals("pack")) {
            @SuppressWarnings("unchecked")
            T meta = (T) new PackResourceMetadata(Text.literal("Tokimi's Boatyard dynamic"), 15);
            return meta;
        }
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void close() {
        cache = null;
    }
}
