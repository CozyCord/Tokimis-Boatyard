package net.cozystudios.tokimiboatyard.resource;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
//? if >=1.21.1 {
/*import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import java.util.Optional;
*///? }
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

public class TokimiPackProvider implements RepositorySource {
    @Override
    public void loadPacks(Consumer<Pack> adder) {
        //? if >=1.21.1 {
        /*Pack profile = Pack.readMetaAndCreate(
            new PackLocationInfo(DynamicResourcePack.NAME, Component.literal("Tokimi's Boatyard Dynamic"), PackSource.BUILT_IN, Optional.empty()),
            new Pack.ResourcesSupplier() {
                @Override
                public PackResources openPrimary(PackLocationInfo info) { return new DynamicResourcePack(); }

                @Override
                public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) { return new DynamicResourcePack(); }
            },
            PackType.CLIENT_RESOURCES,
            new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
        );
        *///? } else {
        Pack profile = Pack.readMetaAndCreate(
            DynamicResourcePack.NAME,
            Component.literal("Tokimi's Boatyard Dynamic"),
            true,
            name -> new DynamicResourcePack(),
            PackType.CLIENT_RESOURCES,
            Pack.Position.BOTTOM,
            PackSource.BUILT_IN
        );
        //? }
        if (profile != null) adder.accept(profile);
    }
}
