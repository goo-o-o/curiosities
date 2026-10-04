package com.goo.curiosities.client.render.layer.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CuriosityGeoEntityRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {

    public CuriosityGeoEntityRenderer(EntityRendererProvider.Context context, EntityType<? extends T> entityType) {
        super(context, entityType);
    }

    public CuriosityGeoEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        poseStack.translate(0, animatable.getBbHeight() / 2, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot()) + 90.0F));


        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
    }
}
