package com.min01.pillagerqueen.core.registry;

import com.min01.pillagerqueen.core.ModMain;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems 
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);
	
	public static final RegistryObject<Item> PILLAGER_QUEEN_SPAWN_EGG = ITEMS.register("pillager_queen_spawn_egg", () -> new ForgeSpawnEggItem(() -> ModEntities.PILLAGER_QUEEN.get(), 10977222, 7558869, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
