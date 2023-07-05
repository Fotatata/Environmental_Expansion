package com.fotatata.environmental_expansion.entity.client;

import com.fotatata.environmental_expansion.EnvironmentalExpansion;
import com.fotatata.environmental_expansion.entity.custom.BeaverEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class BeaverModel extends DefaultedEntityGeoModel<BeaverEntity> {
    public BeaverModel() {
        super(new ResourceLocation(EnvironmentalExpansion.MOD_ID, "beaver"));
    }
}