package com.min01.pillagerqueen.core.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.pillagerqueen.common.entity.PillagerQueenEntity;
import com.min01.pillagerqueen.core.registry.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(Raid.class)
public class RaidMixin 
{
	@Inject(method = {"spawnGroup"}, at = {@At("TAIL")})
	void spawnGroup(BlockPos blockPos, CallbackInfo ci) 
	{
		Raid raid = Raid.class.cast(this);
		if (raid.getGroupsSpawned() == raid.getNumGroups(raid.getLevel().getDifficulty()))
		{
			PillagerQueenEntity queen = new PillagerQueenEntity(ModEntities.PILLAGER_QUEEN.get(), raid.getLevel());
			joinRaid(raid.getGroupsSpawned(), queen, blockPos, false);
		} 
	}
	  
	public void joinRaid(int i, Raider raider, @Nullable BlockPos blockPos, boolean bl) 
	{
		Raid raid = Raid.class.cast(this);
		boolean bl2 = raid.addWaveMob(i, raider, true);
		if (bl2) 
		{
			raider.setCurrentRaid(raid);
			raider.setWave(i);
			raider.setCanJoinRaid(true);
			raider.setTicksOutsideRaid(0);
			if (!bl && blockPos != null) 
			{
				raider.setPos(blockPos.getX() + 0.5D, blockPos.getY() + 1.0D, blockPos.getZ() + 0.5D);
				raider.finalizeSpawn((ServerLevelAccessor) raid.getLevel(), raid.getLevel().getCurrentDifficultyAt(blockPos), MobSpawnType.EVENT, null, null);
				raider.applyRaidBuffs(i, false);
				raider.setOnGround(true);
				((ServerLevel)raid.getLevel()).addFreshEntityWithPassengers(raider);
			} 
		} 
	}
}
