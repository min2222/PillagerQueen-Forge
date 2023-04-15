package com.min01.pillagerqueen.core.registry;

import com.min01.pillagerqueen.core.ModMain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds 
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModMain.MOD_ID);
	
	public static final RegistryObject<SoundEvent> PILLAGER_QUEEN_AMBUSH = register("entity.pillager_queen.ambush");
	
	public static final RegistryObject<SoundEvent> PILLAGER_QUEEN_MELEE = register("entity.pillager_queen.melee");
	  
	public static final RegistryObject<SoundEvent> PILLAGER_QUEEN_DEATH = register("entity.pillager_queen.death");
	  
	public static final RegistryObject<SoundEvent> PILLAGER_QUEEN_HURT = register("entity.pillager_queen.hurt");

	private static RegistryObject<SoundEvent> register(String name) 
	{
		return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(ModMain.MOD_ID, name)));
    }
}
