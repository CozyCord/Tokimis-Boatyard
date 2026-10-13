package net.cozystudios.tokimiboatyard.resource;

import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class TokimiPackProvider implements ResourcePackProvider {
    @Override
    public void register(Consumer<ResourcePackProfile> adder) {
        ResourcePackProfile profile = ResourcePackProfile.create(
            DynamicResourcePack.NAME,
            Text.literal("Tokimi's Boatyard Dynamic"),
            true,
            name -> new DynamicResourcePack(),
            ResourceType.CLIENT_RESOURCES,
            ResourcePackProfile.InsertionPosition.BOTTOM,
            ResourcePackSource.BUILTIN
        );
        if (profile != null) adder.accept(profile);
    }
}
