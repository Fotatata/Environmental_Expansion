package com.fotatata.environmental_expansion.entity.client;

import com.fotatata.environmental_expansion.EnvironmentalExpansion;
import com.fotatata.environmental_expansion.entity.custom.BeaverEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BeaverRenderer extends GeoEntityRenderer<BeaverEntity> {
    public BeaverRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BeaverModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    public ResourceLocation getTextureLocation(BeaverEntity animatable) {
        return new ResourceLocation(EnvironmentalExpansion.MOD_ID, "textures/entity/beaver.png");
    }

    @Override
    public RenderType getRenderType(BeaverEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public void render(BeaverEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()){
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}

