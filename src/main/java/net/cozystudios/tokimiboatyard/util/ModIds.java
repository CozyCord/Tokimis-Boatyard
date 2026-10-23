package net.cozystudios.tokimiboatyard.util;

import net.minecraft.resources.ResourceLocation;

public final class ModIds {
    public static ResourceLocation of(String namespace, String path) {
        //? if >=1.21.1 {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///? } else {
        return new ResourceLocation(namespace, path);
        //? }
    }

    public static ResourceLocation parse(String id) {
        //? if >=1.21.1 {
        /*return ResourceLocation.parse(id);
        *///? } else {
        return new ResourceLocation(id);
        //? }
    }

    private ModIds() {}
}
