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

public class UpgradedBoatPartRenderer extends EntityRenderer<UpgradedBoatPart> {
    private static final Identifier FALLBACK_TEXTURE =
        new Identifier("textures/entity/chest_boat/oak.png");

    private final Map<BoatEntity.Type, Identifier> textures;
    private final Map<BoatTier, Identifier> tierBlockTextures;
    private final UpgradedBoatModel boatModel;
    private final UpgradedBoatBandModel boatBandModel;
    private final UpgradedRaftModel raftModel;
    private final UpgradedRaftBandModel raftBandModel;

    public UpgradedBoatPartRenderer(EntityRendererFactory.Context ctx) {
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
    public Identifier getTexture(UpgradedBoatPart part) {
        UpgradedBoatEntity parent = part.getParentBoat();
        if (parent == null) return FALLBACK_TEXTURE;
        return textures.get(parent.getVariant());
    }

    private static boolean isRaft(UpgradedBoatEntity boat) {
        return boat.getVariant() == BoatEntity.Type.BAMBOO;
    }

    @Override
    public void render(UpgradedBoatPart part, float entityYaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        UpgradedBoatEntity boat = part.getParentBoat();
        if (boat == null) return;

        int expectedParts = boat.getExtraParts();
        if (boat.age < 20) {
            long partCount = boat.getPassengerList().stream()
                .filter(e -> e instanceof UpgradedBoatPart).count();
            if (partCount < expectedParts) return;
        }

        matrices.push();
        matrices.translate(0.0, 0.375, 0.0);

        float prevYaw = boat.prevYaw;
        float yawDiff = boat.getYaw() - prevYaw;
        while (yawDiff > 180.0F) yawDiff -= 360.0F;
        while (yawDiff < -180.0F) yawDiff += 360.0F;
        float interpYaw = prevYaw + yawDiff * tickDelta;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - interpYaw));

        float damageWobble = boat.getDamageWobbleTicks() - tickDelta;
        float damageWobbleStrength = boat.getDamageWobbleStrength() - tickDelta;
        if (damageWobbleStrength < 0.0F) damageWobbleStrength = 0.0F;
        if (damageWobble > 0.0F) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
                MathHelper.sin(damageWobble) * damageWobble * damageWobbleStrength / 10.0F
                    * (float) boat.getDamageWobbleSide()));
        }

        float bubbleWobble = boat.interpolateBubbleWobble(tickDelta);
        if (!MathHelper.approximatelyEquals(bubbleWobble, 0.0F)) {
            matrices.multiply(new Quaternionf().setAngleAxis(
                bubbleWobble * ((float) Math.PI / 180F), 1.0F, 0.0F, 1.0F));
        }

        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));

        int index = part.getIndex();
        int totalParts = boat.getExtraParts();
        boolean isLast = index >= totalParts;

        boolean segmentOccupied = boat.hasPassengerInSegment(index);

        if (isRaft(boat)) {
            this.raftModel.setPaddlesVisible(true, true);
            this.raftModel.setChestVisible(isLast);
            if (segmentOccupied) {
                this.raftModel.animatePaddles(boat, tickDelta);
            } else {
                this.raftModel.setPaddlesAtRest();
            }

            VertexConsumer buffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(getTexture(part)));
            this.raftModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

            Identifier bandTex = this.tierBlockTextures.get(boat.getTier());
            VertexConsumer bandBuffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(bandTex));
            this.raftBandModel.render(matrices, bandBuffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            this.boatModel.setVisible(false, isLast, true, true);
            this.boatModel.setPaddlesVisible(true, true);
            this.boatModel.setChestVisible(isLast);
            if (segmentOccupied) {
                this.boatModel.animatePaddles(boat, tickDelta);
            } else {
                this.boatModel.setPaddlesAtRest();
            }

            VertexConsumer buffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(getTexture(part)));
            this.boatModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

            Identifier bandTex = this.tierBlockTextures.get(boat.getTier());
            VertexConsumer bandBuffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(bandTex));
            this.boatBandModel.render(matrices, bandBuffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

            if (!boat.isSubmergedInWater()) {
                VertexConsumer waterBuffer = vertexConsumers.getBuffer(RenderLayer.getWaterMask());
                this.boatModel.getWaterPatch().render(matrices, waterBuffer, light, OverlayTexture.DEFAULT_UV);
            }
        }

        matrices.pop();
        super.render(part, entityYaw, tickDelta, matrices, vertexConsumers, light);
    }
}
