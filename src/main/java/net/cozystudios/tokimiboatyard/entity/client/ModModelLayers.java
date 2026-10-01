package net.cozystudios.tokimiboatyard.entity.client;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class ModModelLayers {
    public static final EntityModelLayer UPGRADED_BOAT_CHUNK =
        new EntityModelLayer(new Identifier(TokimiBoatyard.MOD_ID, "upgraded_boat_chunk"), "main");

    public static final EntityModelLayer UPGRADED_BOAT_BAND =
        new EntityModelLayer(new Identifier(TokimiBoatyard.MOD_ID, "upgraded_boat_band"), "main");

    public static final EntityModelLayer UPGRADED_RAFT_CHUNK =
        new EntityModelLayer(new Identifier(TokimiBoatyard.MOD_ID, "upgraded_raft_chunk"), "main");

    public static final EntityModelLayer UPGRADED_RAFT_BAND =
        new EntityModelLayer(new Identifier(TokimiBoatyard.MOD_ID, "upgraded_raft_band"), "main");

    private ModModelLayers() {}
}
