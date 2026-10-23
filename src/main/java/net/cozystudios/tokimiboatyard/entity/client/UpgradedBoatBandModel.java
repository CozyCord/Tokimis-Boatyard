package net.cozystudios.tokimiboatyard.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class UpgradedBoatBandModel {
    private final ModelPart leftBand;
    private final ModelPart rightBand;

    public UpgradedBoatBandModel(ModelPart root) {
        this.leftBand = root.getChild("band_left");
        this.rightBand = root.getChild("band_right");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();

        root.addOrReplaceChild("band_left",
            CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -8.0F, -1.0F, 28.0F, 1.0F, 2.0F),
            PartPose.offset(0.0F, 4.0F, 9.0F));

        root.addOrReplaceChild("band_right",
            CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -8.0F, -1.0F, 28.0F, 1.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, (float) Math.PI, 0.0F));

        return LayerDefinition.create(data, 16, 16);
    }

    //? if >=1.21 {
    /*public void render(PoseStack matrices, VertexConsumer vc, int light, int overlay, int color) {
        this.leftBand.render(matrices, vc, light, overlay, color);
        this.rightBand.render(matrices, vc, light, overlay, color);
    }
    *///?} else {

    public void render(PoseStack matrices, VertexConsumer vc, int light, int overlay,
                       float red, float green, float blue, float alpha) {
        this.leftBand.render(matrices, vc, light, overlay, red, green, blue, alpha);
        this.rightBand.render(matrices, vc, light, overlay, red, green, blue, alpha);
    }
    //?}
}
