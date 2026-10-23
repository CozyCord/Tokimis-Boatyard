package net.cozystudios.tokimiboatyard.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;

public class UpgradedBoatModel extends EntityModel<Entity> implements WaterPatchModel {
    private final ModelPart bottom;
    private final ModelPart back;
    private final ModelPart front;
    private final ModelPart right;
    private final ModelPart left;
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ModelPart waterPatch;
    private final ModelPart chestBottom;
    private final ModelPart chestLid;
    private final ModelPart chestLock;

    public UpgradedBoatModel(ModelPart root) {
        this.bottom = root.getChild("bottom");
        this.back = root.getChild("back");
        this.front = root.getChild("front");
        this.right = root.getChild("right");
        this.left = root.getChild("left");
        this.leftPaddle = root.getChild("left_paddle");
        this.rightPaddle = root.getChild("right_paddle");
        this.waterPatch = root.getChild("water_patch");
        this.chestBottom = root.getChild("chest_bottom");
        this.chestLid = root.getChild("chest_lid");
        this.chestLock = root.getChild("chest_lock");
    }

    public void setVisible(boolean front, boolean back, boolean left, boolean right) {
        this.front.visible = front;
        this.back.visible = back;
        this.left.visible = left;
        this.right.visible = right;
        this.bottom.visible = true;
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
        paddle.xRot = Mth.clampedLerp(-1.0471976F, -0.2617994F,
            (Mth.sin(-phase) + 1.0F) / 2.0F);
        paddle.yRot = Mth.clampedLerp(-0.7853982F, 0.7853982F,
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
        this.back.render(matrices, vertices, light, overlay, color);
        this.front.render(matrices, vertices, light, overlay, color);
        this.left.render(matrices, vertices, light, overlay, color);
        this.right.render(matrices, vertices, light, overlay, color);
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
        this.back.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.front.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.left.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.right.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.leftPaddle.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.rightPaddle.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.chestBottom.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.chestLid.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.chestLock.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
    //?}

    @Override
    public ModelPart waterPatch() {
        return this.waterPatch;
    }
}
