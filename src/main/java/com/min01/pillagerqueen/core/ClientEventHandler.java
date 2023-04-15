package com.min01.pillagerqueen.core;

import com.min01.pillagerqueen.client.render.PillagerQueenRenderer;
import com.min01.pillagerqueen.client.render.model.ModModelLayers;
import com.min01.pillagerqueen.client.render.model.PillagerQueenModel;
import com.min01.pillagerqueen.core.registry.ModEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler 
{
    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
    	event.registerEntityRenderer(ModEntities.PILLAGER_QUEEN.get(), PillagerQueenRenderer::new);
    }
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
    	event.registerLayerDefinition(ModModelLayers.PILLAGER_QUEEN, PillagerQueenModel::createBodyLayer);
    }
}
