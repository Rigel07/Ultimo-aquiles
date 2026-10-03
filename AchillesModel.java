package com.achilles.client;

import com.achilles.Achilles;
import com.achilles.entity.AchillesEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Mini Aquiles: cabezón, cuerpo chiquito, casco con cresta roja, peto de bronce,
 * grebas, escudo en un brazo, espadita en el otro y capa. Las coordenadas UV (64x64)
 * coinciden con tools/generate_assets.py.
 */
public class AchillesModel extends EntityModel<AchillesEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Achilles.MODID, "achilles"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart cape;

    public AchillesModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.cape = root.getChild("cape");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(8, 0).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        body.addOrReplaceChild("breastplate", CubeListBuilder.create()
                        .texOffs(28, 0).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.4F)),
                PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        head.addOrReplaceChild("helmet", CubeListBuilder.create()
                        .texOffs(32, 10).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        head.addOrReplaceChild("crest", CubeListBuilder.create()
                        .texOffs(32, 23).addBox(-1.0F, -12.5F, -4.0F, 2.0F, 4.0F, 8.0F),
                PartPose.ZERO);

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-4.0F, 15.0F, 0.0F));
        rightArm.addOrReplaceChild("sword", CubeListBuilder.create()
                        .texOffs(16, 26).addBox(-0.5F, 3.0F, -8.0F, 1.0F, 1.0F, 8.0F)
                        .texOffs(52, 23).addBox(-1.5F, 3.0F, -2.0F, 3.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(4.0F, 15.0F, 0.0F));
        leftArm.addOrReplaceChild("shield", CubeListBuilder.create()
                        .texOffs(0, 33).addBox(1.0F, -2.0F, -3.5F, 1.0F, 7.0F, 7.0F),
                PartPose.ZERO);

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(-1.5F, 20.0F, 0.0F));
        rightLeg.addOrReplaceChild("greave", CubeListBuilder.create()
                        .texOffs(16, 34).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.3F)),
                PartPose.ZERO);
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(1.5F, 20.0F, 0.0F));
        leftLeg.addOrReplaceChild("greave", CubeListBuilder.create()
                        .texOffs(16, 34).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.3F)),
                PartPose.ZERO);

        root.addOrReplaceChild("cape", CubeListBuilder.create()
                        .texOffs(32, 36).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 9.0F, 1.0F),
                PartPose.offset(0.0F, 14.0F, 2.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AchillesEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = Mth.clamp(netHeadYaw, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
        this.head.xRot = Mth.clamp(headPitch, -30.0F, 30.0F) * Mth.DEG_TO_RAD;

        float swing = limbSwing * 0.6662F;
        this.rightLeg.xRot = Mth.cos(swing) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(swing + Mth.PI) * 1.4F * limbSwingAmount;

        // Espada en alto, como un héroe de verdad
        this.rightArm.xRot = -0.6F + Mth.cos(swing + Mth.PI) * 1.0F * limbSwingAmount;
        this.rightArm.yRot = 0.0F;
        this.leftArm.xRot = Mth.cos(swing) * 1.0F * limbSwingAmount;

        if (this.attackTime > 0.0F) {
            float s = Mth.sin(this.attackTime * Mth.PI);
            this.rightArm.xRot = -1.0F - s * 1.4F;
            this.rightArm.yRot = -s * 0.5F;
        }

        this.rightArm.zRot = 0.05F + Mth.cos(ageInTicks * 0.09F) * 0.04F;
        this.leftArm.zRot = -0.05F - Mth.cos(ageInTicks * 0.09F) * 0.04F;
        this.cape.xRot = 0.15F + limbSwingAmount * 0.8F + Mth.cos(ageInTicks * 0.1F) * 0.03F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
