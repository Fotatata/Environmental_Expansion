package com.fotatata.environmental_expansion.entity.client;

import com.fotatata.environmental_expansion.EnvironmentalExpansion;
import com.fotatata.environmental_expansion.entity.custom.CraneEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class CraneModel extends DefaultedEntityGeoModel<CraneEntity> {
    public CraneModel() {
        super(new ResourceLocation(EnvironmentalExpansion.MOD_ID, "crane"));
    }

    @Override
    public void setCustomAnimations(CraneEntity animatable, long instanceId, AnimationState<CraneEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head != null){
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
