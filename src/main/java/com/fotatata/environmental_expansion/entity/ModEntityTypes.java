package com.fotatata.environmental_expansion.entity;

import com.fotatata.environmental_expansion.EnvironmentalExpansion;
import com.fotatata.environmental_expansion.entity.custom.BeaverEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EnvironmentalExpansion.MOD_ID);

    public static final RegistryObject<EntityType<BeaverEntity>> BEAVER = ENTITY_TYPES.register("beaver",
            ()->EntityType.Builder.of(BeaverEntity::new, MobCategory.CREATURE).sized(0.6f,0.5f)
                    .build(new ResourceLocation(EnvironmentalExpansion.MOD_ID, "beaver").toString()));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
