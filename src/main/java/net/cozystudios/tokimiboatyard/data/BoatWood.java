package net.cozystudios.tokimiboatyard.data;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record BoatWood(
    Identifier id,
    Identifier entityTexture,
    Identifier itemIconLayer,
    @Nullable BoatEntity.Type vanillaType,
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
