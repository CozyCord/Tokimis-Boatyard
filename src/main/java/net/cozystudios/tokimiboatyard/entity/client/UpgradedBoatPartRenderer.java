package net.cozystudios.tokimiboatyard.entity.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatPart;
import net.cozystudios.tokimiboatyard.util.ModIds;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import java.util.Map;

public class UpgradedBoatPartRenderer extends EntityRenderer<UpgradedBoatPart> {
    private static final ResourceLocation FALLBACK_TEXTURE =
        ModIds.of("minecraft", "textures/entity/chest_boat/oak.png");

    private final Map<BoatTier, ResourceLocation> tierBlockTextures;
    private final UpgradedBoatModel boatModel;
    private final UpgradedBoatBandModel boatBandModel;
    private final UpgradedRaftModel raftModel;
    private final UpgradedRaftBandModel raftBandModel;

    public UpgradedBoatPartRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8F;
        this.boatModel = new UpgradedBoatModel(ctx.bakeLayer(ModModelLayers.UPGRADED_BOAT_CHUNK));
        this.boatBandModel = new UpgradedBoatBandModel(ctx.bakeLayer(ModModelLayers.UPGRADED_BOAT_BAND));
        this.raftModel = new UpgradedRaftModel(ctx.bakeLayer(ModModelLayers.UPGRADED_RAFT_CHUNK));
        this.raftBandModel = new UpgradedRaftBandModel(ctx.bakeLayer(ModModelLayers.UPGRADED_RAFT_BAND));
        ImmutableMap.Builder<BoatTier, ResourceLocation> tierBuilder = ImmutableMap.builder();
        for (BoatTier tier : BoatTier.values()) {
            tierBuilder.put(tier, ModIds.parse(tier.blockTexturePath()));
        }
        this.tierBlockTextures = tierBuilder.build();
    }

    @Override
    public ResourceLocation getTextureLocation(UpgradedBoatPart part) {
        UpgradedBoatEntity parent = part.getParentBoat();
        if (parent == null) return FALLBACK_TEXTURE;
        BoatWood wood = BoatWoodRegistry.get(parent.getWoodId());
        return wood != null ? wood.entityTexture() : FALLBACK_TEXTURE;
    }

    private static boolean isRaft(UpgradedBoatEntity boat) {
        BoatWood wood = BoatWoodRegistry.get(boat.getWoodId());
        return wood != null && wood.isRaft();
    }

    @Override
    public void render(UpgradedBoatPart part, float entityYaw, float tickDelta,
                       PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        UpgradedBoatEntity boat = part.getParentBoat();
        if (boat == null) return;

        int expectedParts = boat.getExtraParts();
        if (boat.tickCount < 20) {
            long partCount = boat.getPassengers().stream()
                .filter(e -> e instanceof UpgradedBoatPart).count();
            if (partCount < expectedParts) return;
        }

        matrices.pushPose();
        matrices.translate(0.0, 0.375, 0.0);

        float prevYaw = boat.yRotO;
        float yawDiff = boat.getYRot() - prevYaw;
        while (yawDiff > 180.0F) yawDiff -= 360.0F;
        while (yawDiff < -180.0F) yawDiff += 360.0F;
        float interpYaw = prevYaw + yawDiff * tickDelta;
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F - interpYaw));

        int index = part.getIndex();
        int totalParts = boat.getExtraParts();
        boolean isLast = index >= totalParts;

        float pivotShift = -index * net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity.SEGMENT_SPACING;
        matrices.translate(0.0, 0.0, pivotShift);

        float damageWobble = boat.getHurtTime() - tickDelta;
        float damageWobbleStrength = boat.getDamage() - tickDelta;
        if (damageWobbleStrength < 0.0F) damageWobbleStrength = 0.0F;
        if (damageWobble > 0.0F) {
            matrices.mulPose(Axis.XP.rotationDegrees(
                Mth.sin(damageWobble) * damageWobble * damageWobbleStrength / 10.0F
                    * (float) boat.getHurtDir()));
        }

        float bubbleWobble = boat.getBubbleAngle(tickDelta);
        if (!Mth.equal(bubbleWobble, 0.0F)) {
            matrices.mulPose(new Quaternionf().setAngleAxis(
                bubbleWobble * ((float) Math.PI / 180F), 1.0F, 0.0F, 1.0F));
        }

        matrices.translate(0.0, 0.0, -pivotShift);

        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.mulPose(Axis.YP.rotationDegrees(90.0F));

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
                RenderType.entityCutoutNoCull(getTextureLocation(part)));
            //? if >=1.21 {
            /*this.raftModel.renderToBuffer(matrices, buffer, light, OverlayTexture.NO_OVERLAY, -1);
            *///?} else {
            this.raftModel.renderToBuffer(matrices, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            //?}

            ResourceLocation bandTex = this.tierBlockTextures.get(boat.getTier());
            VertexConsumer bandBuffer = vertexConsumers.getBuffer(
                RenderType.entityCutoutNoCull(bandTex));
            //? if >=1.21 {
            /*this.raftBandModel.render(matrices, bandBuffer, light, OverlayTexture.NO_OVERLAY, -1);
            *///?} else {
            this.raftBandModel.render(matrices, bandBuffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            //?}
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
                RenderType.entityCutoutNoCull(getTextureLocation(part)));
            //? if >=1.21 {
            /*this.boatModel.renderToBuffer(matrices, buffer, light, OverlayTexture.NO_OVERLAY, -1);
            *///?} else {
            this.boatModel.renderToBuffer(matrices, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            //?}

            ResourceLocation bandTex = this.tierBlockTextures.get(boat.getTier());
            VertexConsumer bandBuffer = vertexConsumers.getBuffer(
                RenderType.entityCutoutNoCull(bandTex));
            //? if >=1.21 {
            /*this.boatBandModel.render(matrices, bandBuffer, light, OverlayTexture.NO_OVERLAY, -1);
            *///?} else {
            this.boatBandModel.render(matrices, bandBuffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            //?}

            if (!boat.isUnderWater()) {
                VertexConsumer waterBuffer = vertexConsumers.getBuffer(RenderType.waterMask());
                this.boatModel.waterPatch().render(matrices, waterBuffer, light, OverlayTexture.NO_OVERLAY);
            }
        }

        matrices.popPose();
        super.render(part, entityYaw, tickDelta, matrices, vertexConsumers, light);
    }
}
