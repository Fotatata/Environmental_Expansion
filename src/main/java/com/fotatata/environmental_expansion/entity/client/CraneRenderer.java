package com.fotatata.environmental_expansion.entity.client;

import com.fotatata.environmental_expansion.EnvironmentalExpansion;
import com.fotatata.environmental_expansion.entity.custom.CraneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CraneRenderer extends GeoEntityRenderer<CraneEntity> {
    public CraneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CraneModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CraneEntity animatable) {
        return new ResourceLocation(EnvironmentalExpansion.MOD_ID, "textures/entity/crane_common.png");
    }

    @Override
    public RenderType getRenderType(CraneEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public void render(CraneEntity entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()){
            poseStack.scale(0.6f, 0.6f, 0.6f);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
