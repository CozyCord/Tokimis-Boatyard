package net.cozystudios.tokimiboatyard.registry;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatPart;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<UpgradedBoatEntity> UPGRADED_BOAT = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(TokimiBoatyard.MOD_ID, "upgraded_boat"),
        FabricEntityTypeBuilder.<UpgradedBoatEntity>create(SpawnGroup.MISC, UpgradedBoatEntity::new)
            .dimensions(EntityDimensions.fixed(1.375F, 0.5625F))
            .trackRangeChunks(10)
            .trackedUpdateRate(3)
            .forceTrackedVelocityUpdates(true)
            .build()
    );

    public static final EntityType<UpgradedBoatPart> UPGRADED_BOAT_PART = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(TokimiBoatyard.MOD_ID, "upgraded_boat_part"),
        FabricEntityTypeBuilder.<UpgradedBoatPart>create(SpawnGroup.MISC, UpgradedBoatPart::new)
            .dimensions(EntityDimensions.fixed(1.0F, 0.5625F))
            .trackRangeChunks(10)
            .trackedUpdateRate(3)
            .forceTrackedVelocityUpdates(true)
            .build()
    );

    private ModEntities() {}
    public static void register() {}
}
