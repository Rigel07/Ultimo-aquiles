package com.achilles.client;

import com.achilles.Achilles;
import com.achilles.entity.AchillesEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AchillesRenderer extends MobRenderer<AchillesEntity, AchillesModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Achilles.MODID, "textures/entity/achilles.png");

    public AchillesRenderer(EntityRendererProvider.Context context) {
        super(context, new AchillesModel(context.bakeLayer(AchillesModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(AchillesEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AchillesEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.6F, 0.6F, 0.6F);
    }

    /** Cuando se tropieza se queda tumbado de lado, como un muñeco. */
    @Override
    protected void setupRotations(AchillesEntity entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (entity.isTripped()) {
            poseStack.translate(0.0D, 0.12D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.translate(0.0D, -0.12D, 0.0D);
        }
    }
}
