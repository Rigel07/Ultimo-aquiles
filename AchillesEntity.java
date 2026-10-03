package com.achilles.entity;

import com.achilles.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Mini Aquiles: un guerrero diminuto con armadura de bronce, capa roja y casco con cresta
 * que intenta ser el héroe de la leyenda... pero es torpe y se tropieza.
 */
public class AchillesEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_TRIPPED =
            SynchedEntityData.defineId(AchillesEntity.class, EntityDataSerializers.BOOLEAN);

    private int trippedTicks;

    public AchillesEntity(EntityType<? extends AchillesEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    public static boolean checkSpawnRules(EntityType<AchillesEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRIPPED, false);
    }

    public boolean isTripped() {
        return this.entityData.get(DATA_TRIPPED);
    }

    // ------------------------------------------------------------ IA

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(3, new PracticeSwingGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Se cree el héroe: ataca monstruos (menos creepers, que no es tan tonto)
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                target -> !(target instanceof Creeper)));
    }

    /** Practica con la espada: se queda quieto, la agita y a veces se cae. */
    static class PracticeSwingGoal extends Goal {
        private final AchillesEntity mob;
        private int ticks;

        PracticeSwingGoal(AchillesEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() == null && !mob.isTripped() && mob.getRandom().nextInt(200) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return ticks > 0 && !mob.isTripped() && mob.getTarget() == null;
        }

        @Override
        public void start() {
            ticks = 40;
            mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            ticks--;
            if (ticks % 10 == 0) {
                mob.swing(InteractionHand.MAIN_HAND);
                mob.playSound(ModSounds.ACHILLES_CLANG.get(), 0.6F, 0.9F + mob.getRandom().nextFloat() * 0.4F);
            }
        }

        @Override
        public void stop() {
            if (mob.getRandom().nextFloat() < 0.3F) {
                mob.trip(50);
            }
        }
    }

    // ------------------------------------------------------------ torpeza

    public void trip(int duration) {
        if (this.level().isClientSide || isTripped()) return;
        this.entityData.set(DATA_TRIPPED, true);
        this.trippedTicks = duration;
        this.getNavigation().stop();
        this.playSound(ModSounds.ACHILLES_TRIP.get(), 1.0F, 0.95F + this.random.nextFloat() * 0.2F);
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isTripped();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        if (isTripped()) {
            if (--trippedTicks <= 0) {
                this.entityData.set(DATA_TRIPPED, false);
                this.playSound(ModSounds.ACHILLES_GET_UP.get(), 1.0F, 1.0F);
            }
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 0.004D && this.random.nextInt(500) == 0) {
            trip(40 + this.random.nextInt(30));
        }
    }

    // ------------------------------------------------------------ sonidos

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ACHILLES_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ACHILLES_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ACHILLES_DEATH.get();
    }
}
