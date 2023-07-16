package com.fotatata.environmental_expansion;

import com.fotatata.environmental_expansion.entity.ModEntityTypes;
import com.fotatata.environmental_expansion.entity.client.BeaverRenderer;
import com.fotatata.environmental_expansion.entity.client.CraneRenderer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(EnvironmentalExpansion.MOD_ID)
public class EnvironmentalExpansion {

    public static final String MOD_ID = "environmental_expansion";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EnvironmentalExpansion() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        GeckoLib.initialize();
        ModEntityTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntityTypes.BEAVER.get(), BeaverRenderer::new);
            EntityRenderers.register(ModEntityTypes.CRANE.get(), CraneRenderer::new);
        }
    }
}
