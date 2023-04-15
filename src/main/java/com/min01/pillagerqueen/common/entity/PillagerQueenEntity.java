package com.min01.pillagerqueen.common.entity;

import java.util.EnumSet;
import java.util.Random;

import com.min01.pillagerqueen.core.registry.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PillagerQueenEntity extends Raider 
{
	public final AnimationState walkAnimationState = new AnimationState();

	public final AnimationState meleeAttackAnimationState = new AnimationState();

	public final AnimationState floatingAnimationState = new AnimationState();

	public final AnimationState fallingAnimationState = new AnimationState();

	private static final EntityDataAccessor<Boolean> HAS_SPAWNED_PATROL = SynchedEntityData.defineId(PillagerQueenEntity.class, EntityDataSerializers.BOOLEAN);

	public PillagerQueenEntity(EntityType<? extends Raider> entityType, Level level) 	
	{
		super(entityType, level);
	}

	@Override
	protected void registerGoals() 
	{
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
		this.goalSelector.addGoal(4, new QueenMeleeAttackGoal(this, this, 1.0D, false));
		this.goalSelector.addGoal(3, new QueenFlyingGoal(this));
		this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[] { Raider.class })).setAlertOthers(new Class[0]));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}
	
    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
        		.add(Attributes.MOVEMENT_SPEED, 0.45D)
				.add(Attributes.FOLLOW_RANGE, 64.0D)
				.add(Attributes.MAX_HEALTH, 64.0D)
				.add(Attributes.FOLLOW_RANGE, 35.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.800000011920929D)
				.add(Attributes.FLYING_SPEED, 0.44999998807907104D)
				.add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(HAS_SPAWNED_PATROL, Boolean.valueOf(false));
	}

	@Override
	public void applyRaidBuffs(int p_37844_, boolean p_37845_) 
	{
		
	}

	@Override
	public void tick() 
	{
		super.tick();
		handleAnimations();
	}

	@Override
	public int getExperienceReward() 
	{
		this.xpReward = 800 + this.random.nextInt(750);
		return super.getExperienceReward();
	}

	public void handleAnimations()
	{
		if (getNavigation().isInProgress() || isAggressive() || this.getDeltaMovement().x > 0.0D || this.getDeltaMovement().z > 0.0D) 
		{
			if (isOnGround()) 
			{
				this.walkAnimationState.startIfStopped(this.tickCount);
				spawnSprintParticle();
			}
		} 
		else 
		{
			this.walkAnimationState.stop();
		}
		if (!isOnGround() && !isNoGravity() && !isAggressive()) 
		{
			this.fallingAnimationState.startIfStopped(this.tickCount);
		} 
		else 
		{
			this.fallingAnimationState.stop();
		}
	}

	@Override
	public void handleEntityEvent(byte b)
	{
		if (b == 97) 
		{
			for (int i = 0; i < 10; i++)
				this.level.addParticle(ParticleTypes.CRIT, getX(4.0D), getRandomY(), getZ(4.0D), 2.0D, 2.0D, 2.0D);
		} 
		else if (b == 98) 
		{
			this.meleeAttackAnimationState.start(this.tickCount);
		} 
		else if (b == 99) 
		{
			this.floatingAnimationState.startIfStopped(this.tickCount);
		} 
		else if (b == 100) 
		{
			this.floatingAnimationState.stop();
		}
		else if (b == 101) 
		{
			for (int i = 0; i < 360; i++) 
			{
				if (i % 2 == 0)
					getLevel().addParticle(ParticleTypes.LARGE_SMOKE, getX() + Math.cos(i), getY(1.0D), getZ() + Math.sin(i), Math.cos(i), -0.25D, Math.sin(i));
			}
			for (AbstractIllager p : this.level.getNearbyEntities(AbstractIllager.class, TargetingConditions.DEFAULT, (LivingEntity) this, getBoundingBox().inflate(25.0D))) 
			{
				for (int j = 0; j < 32; j++)
					getLevel().addParticle(ParticleTypes.POOF, p.getX(3.0D), p.getY(), p.getZ(3.0D), 0.25D, 0.25D, 0.25D);
			}
		}
		else 
		{
			super.handleEntityEvent(b);
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) 
	{
		if (!this.level.isClientSide && !hasSpawnedPatrol() && !damageSource.isFall() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && this.level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING))
		{
			ServerLevel serverLevel = (ServerLevel) this.level;
			int targetY = getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, getBlockX(), getBlockZ());
			BlockPos blockPos = new BlockPos(getX(10.0D), targetY, getZ(10.0D));
			setHasSpawnedPatrol(true);
			int amountOfAmbush = this.random.nextInt(8) + 1;
			for (int i = 0; i < amountOfAmbush; i++) 
			{
				blockPos = new BlockPos(getX(10.0D), targetY, getZ(10.0D));
				PatrollingMonster patrollingMonster = (PatrollingMonster) EntityType.PILLAGER.create(serverLevel);
				if (this.random.nextInt(9) == 1)
					patrollingMonster = (PatrollingMonster) EntityType.VINDICATOR.create(serverLevel);
				patrollingMonster.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
				patrollingMonster.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), MobSpawnType.PATROL, null, null);
				serverLevel.addFreshEntityWithPassengers(patrollingMonster);
				getLevel().playSound(null, patrollingMonster.getX(), patrollingMonster.getY(), patrollingMonster.getZ(), SoundEvents.PILLAGER_CELEBRATE, SoundSource.HOSTILE, 1.0F, 1.0F);
			}
			getLevel().playSound(null, getX(), getY(), getZ(), ModSounds.PILLAGER_QUEEN_AMBUSH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
			getLevel().broadcastEntityEvent(this, (byte) 101);
		}
		if (!damageSource.isFall())
			return super.hurt(damageSource, f);
		return false;
	}

	@Override
	public SoundEvent getCelebrateSound() 
	{
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() 
	{
		return ModSounds.PILLAGER_QUEEN_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) 
	{
		return ModSounds.PILLAGER_QUEEN_HURT.get();
	}

	public void setHasSpawnedPatrol(boolean b) 
	{
		getEntityData().set(HAS_SPAWNED_PATROL, Boolean.valueOf(b));
	}

	public boolean hasSpawnedPatrol() 
	{
		return (getEntityData().get(HAS_SPAWNED_PATROL)).booleanValue();
	}

	class QueenMeleeAttackGoal extends Goal 
	{
		private final PillagerQueenEntity pillagerQueen;

		protected final PathfinderMob mob;

		private final double speedModifier;

		private final boolean followingTargetEvenIfNotSeen;

		private Path path;

		private double pathedTargetX;

		private double pathedTargetY;

		private double pathedTargetZ;

		private int ticksUntilNextPathRecalculation;

		private int ticksUntilNextAttack;

		//private final int attackInterval = 20;

		private long lastCanUseCheck;

		//private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;

		private int damageTimer = -1;

		public QueenMeleeAttackGoal(PillagerQueenEntity pillagerQueen, PathfinderMob pathfinderMob, double d, boolean bl) 
		{
			this.pillagerQueen = pillagerQueen;
			this.mob = pathfinderMob;
			this.speedModifier = d;
			this.followingTargetEvenIfNotSeen = bl;
			setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse()
		{
			long l = this.mob.level.getGameTime();
			if (this.mob.getHealth() > this.mob.getMaxHealth() / 2.0F)
				return false;
			if (l - this.lastCanUseCheck < 20L)
				return false;
			this.lastCanUseCheck = l;
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity == null)
				return false;
			if (!livingEntity.isAlive())
				return false;
			this.path = this.mob.getNavigation().createPath(livingEntity, 0);
			if (this.path != null)
				return true;
			return (getAttackReachSqr(livingEntity) >= this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ()));
		}

		@Override
		public boolean canContinueToUse() 
		{
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity == null)
				return false;
			if (!livingEntity.isAlive())
				return false;
			if (!this.followingTargetEvenIfNotSeen)
				return !this.mob.getNavigation().isDone();
			if (!this.mob.isWithinRestriction(livingEntity.blockPosition()))
				return false;
			return (!(livingEntity instanceof Player) || (!livingEntity.isSpectator() && !((Player) livingEntity).isCreative()));
		}

		@Override
		public void start() 
		{
			this.mob.getNavigation().moveTo(this.path, this.speedModifier);
			this.mob.setAggressive(true);
			this.ticksUntilNextPathRecalculation = 0;
			this.ticksUntilNextAttack = 0;
		}

		@Override
		public void stop()
		{
			LivingEntity livingEntity = this.mob.getTarget();
			if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity)) this.mob.setTarget(null);
			this.mob.setAggressive(false);
			this.mob.getNavigation().stop();
		}
		
		@Override
		public boolean requiresUpdateEveryTick() 
		{
			return true;
		}

		@Override
		public void tick() 
		{
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity != null) 
			{
				this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
				double d = this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
				this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
				if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingEntity)) && this.ticksUntilNextPathRecalculation <= 0 && ((this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D) || livingEntity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.mob.getRandom().nextFloat() < 0.05F)) 
				{
					this.pathedTargetX = livingEntity.getX();
					this.pathedTargetY = livingEntity.getY();
					this.pathedTargetZ = livingEntity.getZ();
					this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
					if (d > 1024.0D) 
					{
						this.ticksUntilNextPathRecalculation += 10;
					} 
					else if (d > 256.0D) 
					{
						this.ticksUntilNextPathRecalculation += 5;
					}
					if (!this.mob.getNavigation().moveTo(livingEntity, this.speedModifier))
						this.ticksUntilNextPathRecalculation += 15;
					this.ticksUntilNextPathRecalculation = adjustedTickDelay(this.ticksUntilNextPathRecalculation);
				}
				this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
				checkAndPerformAttack(livingEntity, d);
			}
		}

		protected void checkAndPerformAttack(LivingEntity livingEntity, double d) 
		{
			this.damageTimer--;
			double e = getAttackReachSqr(livingEntity);
			if (d <= e)
				if (this.damageTimer < 0 && getTicksUntilNextAttack() <= 0)
				{
					this.mob.swing(InteractionHand.MAIN_HAND);
					this.mob.getLevel().broadcastEntityEvent(this.mob, (byte) 98);
					this.damageTimer = 3;
					this.ticksUntilNextAttack = 40;
				} 
				else if (this.damageTimer == 0) 
				{
					this.mob.doHurtTarget(livingEntity);
					livingEntity.knockback(5.0D, this.pillagerQueen.getX() - livingEntity.getX(), this.pillagerQueen.getZ() - livingEntity.getZ());
					PillagerQueenEntity.this.getLevel().playSound(null, PillagerQueenEntity.this.getX(), PillagerQueenEntity.this.getY(), PillagerQueenEntity.this.getZ(), ModSounds.PILLAGER_QUEEN_MELEE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
					this.mob.getLevel().broadcastEntityEvent(this.mob, (byte) 97);
					livingEntity.knockback(this.pillagerQueen.getX() - livingEntity.getX(), 3.5D, this.pillagerQueen.getZ() - livingEntity.getZ());
					resetAttackCooldown();
				}
		}

		protected void resetAttackCooldown()
		{
			this.ticksUntilNextAttack = adjustedTickDelay(20);
		}

		protected boolean isTimeToAttack()
		{
			return (this.ticksUntilNextAttack <= 0);
		}

		protected int getTicksUntilNextAttack() 
		{
			return this.ticksUntilNextAttack;
		}

		protected int getAttackInterval() 
		{
			return adjustedTickDelay(20);
		}

		protected double getAttackReachSqr(LivingEntity livingEntity)
		{
			return (this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + livingEntity.getBbWidth());
		}
	}

	class QueenFlyingGoal extends Goal 
	{
		private final PillagerQueenEntity pillagerQueen;

		Random r = new Random();

		public QueenFlyingGoal(PillagerQueenEntity pillagerQueen) 
		{
			this.pillagerQueen = pillagerQueen;
			setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse()
		{
			if (this.pillagerQueen.getHealth() > this.pillagerQueen.getMaxHealth() / 2.0F)
				return true;
			return false;
		}

		@Override
		public void start() 
		{
			super.start();
			this.pillagerQueen.moveControl = new FlyingMoveControl(this.pillagerQueen, 50, true);
			this.pillagerQueen.getNavigation().setCanFloat(true);
			this.pillagerQueen.getLevel().broadcastEntityEvent(this.pillagerQueen, (byte) 99);
		}

		public void stop() {
			super.stop();
			this.pillagerQueen.moveControl = new MoveControl(this.pillagerQueen);
			this.pillagerQueen.setNoGravity(false);
			this.pillagerQueen.getNavigation().setCanFloat(false);
			this.pillagerQueen.getLevel().broadcastEntityEvent(this.pillagerQueen, (byte) 100);
		}

		@Override
		public void tick()
		{
			super.tick();
			this.pillagerQueen.setNoGravity(true);
			if (this.pillagerQueen.getTarget() != null)
				this.pillagerQueen.getLookControl().setLookAt(this.pillagerQueen.getTarget());
			int targetY = this.pillagerQueen.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, this.pillagerQueen.getBlockX(), this.pillagerQueen.getBlockZ());
			float maxFloatHeight = this.pillagerQueen.getHealth() / 4.0F;
			float goalHeight = targetY + maxFloatHeight;
			//BlockPos blockPos = new BlockPos(this.pillagerQueen.getX(), this.pillagerQueen.getY(), this.pillagerQueen.getZ());
			if (Math.round(this.pillagerQueen.getY()) != Math.round(goalHeight))
			{
				this.pillagerQueen.moveControl.setWantedPosition(Math.round(this.pillagerQueen.getX()), goalHeight, Math.round(this.pillagerQueen.getZ()), 8.0D);
				this.pillagerQueen.setYRot(0.0F);
				this.pillagerQueen.setYBodyRot(0.0F);
			}
			LivingEntity livingEntity = this.pillagerQueen.getTarget();
			if (livingEntity != null)
			{
				this.pillagerQueen.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
				Vec3 vec3 = this.pillagerQueen.getTarget().getEyePosition();
				AABB aabb = this.pillagerQueen.getBoundingBox().inflate(15.0D);
				if (!aabb.contains(vec3))
					this.pillagerQueen.moveControl.setWantedPosition(livingEntity.getX(), goalHeight, livingEntity.getZ(), 8.0D);
			}
		}
	}
}
