package net.cozystudios.tokimiboatyard.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public record BoatWood(
    ResourceLocation id,
    ResourceLocation entityTexture,
    ResourceLocation itemIconLayer,
    @Nullable Boat.Type vanillaType,
    String displayName,
    boolean isRaft,
    @Nullable Item baseChestBoatItem
) {
    public String tokimiPath() {
        return id.getNamespace().equals("minecraft")
            ? id.getPath()
            : id.getNamespace() + "_" + id.getPath();
    }

    public String overlayShape() {
        if (isRaft) return "raft";
        if (id.getNamespace().equals("beachparty") && id.getPath().equals("floaty")) return "floaty";
        return "boat";
    }
}
