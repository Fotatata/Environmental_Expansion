package com.fotatata.environmental_expansion.entity.events;

import com.fotatata.environmental_expansion.EnvironmentalExpansion;
import com.fotatata.environmental_expansion.entity.ModEntityTypes;
import com.fotatata.environmental_expansion.entity.custom.BeaverEntity;
import com.fotatata.environmental_expansion.entity.custom.CraneEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = EnvironmentalExpansion.MOD_ID)
    public static class ForgeEvents{

        @Mod.EventBusSubscriber(modid = EnvironmentalExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
        public static class ModEventBusEvents{
            @SubscribeEvent
            public static void entityAttributeEvent(EntityAttributeCreationEvent event){
                event.put(ModEntityTypes.BEAVER.get(), BeaverEntity.setAttributes());
                event.put(ModEntityTypes.CRANE.get(), CraneEntity.setAttributes());
            }
        }
    }
}
