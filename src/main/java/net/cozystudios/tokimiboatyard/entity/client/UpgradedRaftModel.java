package net.cozystudios.tokimiboatyard.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;

public class UpgradedRaftModel extends EntityModel<Entity> {
    private final ModelPart bottom;
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ModelPart chestBottom;
    private final ModelPart chestLid;
    private final ModelPart chestLock;

    public UpgradedRaftModel(ModelPart root) {
        this.bottom = root.getChild("bottom");
        this.leftPaddle = root.getChild("left_paddle");
        this.rightPaddle = root.getChild("right_paddle");
        this.chestBottom = root.getChild("chest_bottom");
        this.chestLid = root.getChild("chest_lid");
        this.chestLock = root.getChild("chest_lock");
    }

    public void setPaddlesVisible(boolean left, boolean right) {
        this.leftPaddle.visible = left;
        this.rightPaddle.visible = right;
    }

    public void setChestVisible(boolean visible) {
        this.chestBottom.visible = visible;
        this.chestLid.visible = visible;
        this.chestLock.visible = visible;
    }

    public void animatePaddles(Boat boat, float paddleAnglePhase) {
        setPaddleAngle(boat, 0, this.leftPaddle, paddleAnglePhase);
        setPaddleAngle(boat, 1, this.rightPaddle, paddleAnglePhase);
    }

    public void setPaddlesAtRest() {
        setPaddleAngleAtPhase(this.leftPaddle, 0, 0.0F);
        setPaddleAngleAtPhase(this.rightPaddle, 1, 0.0F);
    }

    private static void setPaddleAngle(Boat entity, int side, ModelPart paddle, float angle) {
        setPaddleAngleAtPhase(paddle, side, entity.getRowingTime(side, angle));
    }

    private static void setPaddleAngleAtPhase(ModelPart paddle, int side, float phase) {
        paddle.xRot = Mth.clampedLerp(
            -(float) Math.PI / 3F,
            -(float) Math.PI / 12F,
            (Mth.sin(-phase) + 1.0F) / 2.0F);
        paddle.yRot = Mth.clampedLerp(
            -(float) Math.PI / 4F,
            (float) Math.PI / 4F,
            (Mth.sin(-phase + 1.0F) + 1.0F) / 2.0F);
        if (side == 1) {
            paddle.yRot = (float) Math.PI - paddle.yRot;
        }
    }

    @Override
    public void setupAnim(Entity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    //? if >=1.21 {
    /*public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.bottom.render(matrices, vertices, light, overlay, color);
        this.leftPaddle.render(matrices, vertices, light, overlay, color);
        this.rightPaddle.render(matrices, vertices, light, overlay, color);
        this.chestBottom.render(matrices, vertices, light, overlay, color);
        this.chestLid.render(matrices, vertices, light, overlay, color);
        this.chestLock.render(matrices, vertices, light, overlay, color);
    }
    *///?} else {

    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices,
                       int light, int overlay, float red, float green, float blue, float alpha) {
        this.bottom.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.leftPaddle.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.rightPaddle.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.chestBottom.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.chestLid.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.chestLock.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
    //?}
}
