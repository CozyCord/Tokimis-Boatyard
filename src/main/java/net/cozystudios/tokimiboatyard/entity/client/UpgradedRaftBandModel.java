package net.cozystudios.tokimiboatyard.entity.client;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class UpgradedRaftBandModel {
    private final ModelPart leftBand;
    private final ModelPart rightBand;

    public UpgradedRaftBandModel(ModelPart root) {
        this.leftBand = root.getChild("band_left");
        this.rightBand = root.getChild("band_right");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        root.addChild("band_left",
            ModelPartBuilder.create().uv(0, 0).cuboid(-14.0F, -3.0F, 8.0F, 28.0F, 1.0F, 2.0F),
            ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        root.addChild("band_right",
            ModelPartBuilder.create().uv(0, 0).cuboid(-14.0F, -3.0F, -10.0F, 28.0F, 1.0F, 2.0F),
            ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        return TexturedModelData.of(data, 16, 16);
    }

    public void render(MatrixStack matrices, VertexConsumer vc, int light, int overlay,
                       float red, float green, float blue, float alpha) {
        this.leftBand.render(matrices, vc, light, overlay, red, green, blue, alpha);
        this.rightBand.render(matrices, vc, light, overlay, red, green, blue, alpha);
    }
}
