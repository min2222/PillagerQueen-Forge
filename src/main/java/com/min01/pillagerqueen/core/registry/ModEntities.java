package com.min01.pillagerqueen.core.registry;

import com.min01.pillagerqueen.common.entity.PillagerQueenEntity;
import com.min01.pillagerqueen.core.ModMain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities 
{

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModMain.MOD_ID);

	public static final RegistryObject<EntityType<PillagerQueenEntity>> PILLAGER_QUEEN = ENTITY_TYPES.register("pillager_queen",
            () -> EntityType.Builder.of(PillagerQueenEntity::new, MobCategory.MISC).setTrackingRange(100).sized(0.9F, 1.9F).build(new ResourceLocation(ModMain.MOD_ID, "pillager_queen").toString()));
}
