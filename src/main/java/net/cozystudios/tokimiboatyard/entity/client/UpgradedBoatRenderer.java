package net.cozystudios.tokimiboatyard.entity.client;

import com.google.common.collect.ImmutableMap;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

import java.util.Map;

public class UpgradedBoatRenderer extends EntityRenderer<UpgradedBoatEntity> {
    private final Map<BoatEntity.Type, Identifier> textures;
    private final Map<BoatTier, Identifier> tierBlockTextures;
    private final UpgradedBoatModel boatModel;
    private final UpgradedBoatBandModel boatBandModel;
    private final UpgradedRaftModel raftModel;
    private final UpgradedRaftBandModel raftBandModel;

    public UpgradedBoatRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8F;
        this.boatModel = new UpgradedBoatModel(ctx.getPart(ModModelLayers.UPGRADED_BOAT_CHUNK));
        this.boatBandModel = new UpgradedBoatBandModel(ctx.getPart(ModModelLayers.UPGRADED_BOAT_BAND));
        this.raftModel = new UpgradedRaftModel(ctx.getPart(ModModelLayers.UPGRADED_RAFT_CHUNK));
        this.raftBandModel = new UpgradedRaftBandModel(ctx.getPart(ModModelLayers.UPGRADED_RAFT_BAND));
        ImmutableMap.Builder<BoatEntity.Type, Identifier> builder = ImmutableMap.builder();
        for (BoatEntity.Type type : BoatEntity.Type.values()) {
            builder.put(type, new Identifier("textures/entity/chest_boat/" + type.getName() + ".png"));
        }
        this.textures = builder.build();
        ImmutableMap.Builder<BoatTier, Identifier> tierBuilder = ImmutableMap.builder();
        for (BoatTier tier : BoatTier.values()) {
            tierBuilder.put(tier, new Identifier(tier.blockTexturePath()));
        }
        this.tierBlockTextures = tierBuilder.build();
    }

    @Override
    public Identifier getTexture(UpgradedBoatEntity entity) {
        return textures.get(entity.getVariant());
    }

    private static boolean isRaft(UpgradedBoatEntity entity) {
        return entity.getVariant() == BoatEntity.Type.BAMBOO;
    }

    @Override
    public void render(UpgradedBoatEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        int expectedParts = entity.getExtraParts();
        if (expectedParts > 0 && entity.age < 20) {
            long partCount = entity.getPassengerList().stream()
                .filter(e -> e instanceof UpgradedBoatPart).count();
            if (partCount < expectedParts) return;
        }
        matrices.push();
        matrices.translate(0.0, 0.375, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));

        float damageWobble = entity.getDamageWobbleTicks() - tickDelta;
        float damageWobbleStrength = entity.getDamageWobbleStrength() - tickDelta;
        if (damageWobbleStrength < 0.0F) damageWobbleStrength = 0.0F;
        if (damageWobble > 0.0F) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
                MathHelper.sin(damageWobble) * damageWobble * damageWobbleStrength / 10.0F
                    * (float) entity.getDamageWobbleSide()));
        }

        float bubbleWobble = entity.interpolateBubbleWobble(tickDelta);
        if (!MathHelper.approximatelyEquals(bubbleWobble, 0.0F)) {
            matrices.multiply(new Quaternionf().setAngleAxis(
                bubbleWobble * ((float) Math.PI / 180F), 1.0F, 0.0F, 1.0F));
        }

        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));

        boolean singleSegment = entity.getExtraParts() == 0;

        boolean headOccupied = entity.hasPassengerInSegment(0);

        if (isRaft(entity)) {
            this.raftModel.setPaddlesVisible(true, true);
            this.raftModel.setChestVisible(singleSegment);
            if (headOccupied) {
                this.raftModel.animatePaddles(entity, tickDelta);
            } else {
                this.raftModel.setPaddlesAtRest();
            }

            VertexConsumer buffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(getTexture(entity)));
            this.raftModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

            Identifier bandTex = this.tierBlockTextures.get(entity.getTier());
            VertexConsumer bandBuffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(bandTex));
            this.raftBandModel.render(matrices, bandBuffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            this.boatModel.setVisible(true, singleSegment, true, true);
            this.boatModel.setPaddlesVisible(true, true);
            this.boatModel.setChestVisible(singleSegment);
            if (headOccupied) {
                this.boatModel.animatePaddles(entity, tickDelta);
            } else {
                this.boatModel.setPaddlesAtRest();
            }

            VertexConsumer buffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(getTexture(entity)));
            this.boatModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

            Identifier bandTex = this.tierBlockTextures.get(entity.getTier());
            VertexConsumer bandBuffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(bandTex));
            this.boatBandModel.render(matrices, bandBuffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

            if (!entity.isSubmergedInWater()) {
                VertexConsumer waterBuffer = vertexConsumers.getBuffer(RenderLayer.getWaterMask());
                this.boatModel.getWaterPatch().render(matrices, waterBuffer, light, OverlayTexture.DEFAULT_UV);
            }
        }

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
