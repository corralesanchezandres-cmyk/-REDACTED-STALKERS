package com.terrormod.stalker.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StalkerEntity extends Monster implements GeoEntity {
    // Estados sincronizados con el cliente (para elegir la animacion)
    public static final int STATE_IDLE = 0;
    public static final int STATE_STALK = 1;
    public static final int STATE_RUN = 2;
    public static final int STATE_STARE = 3;
    public static final int STATE_ATTACK = 4;

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.stalker.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.stalker.walk");
    private static final RawAnimation ANIM_RUN = RawAnimation.begin().thenLoop("animation.stalker.run");
    private static final RawAnimation ANIM_STARE = RawAnimation.begin().thenLoop("animation.stalker.stare");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.stalker.attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StalkerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StalkGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, STATE_IDLE);
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setState(int state) {
        if (getState() != state) this.entityData.set(STATE, state);
    }

    /** Desaparece entre humo y un sonido grave. */
    public void vanish() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.5D, getZ(), 40, 0.3D, 1.0D, 0.3D, 0.02D);
            server.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.3F);
        }
        discard();
    }

    /** No se le puede matar a golpes: si lo atacas, se esfuma (y vuelve mas tarde). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide
                && source.getEntity() instanceof Player
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            vanish();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    // ---------------- GeckoLib ----------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 6, this::predicate));
    }

    private PlayState predicate(AnimationState<StalkerEntity> state) {
        switch (getState()) {
            case STATE_RUN:
                return state.setAndContinue(ANIM_RUN);
            case STATE_STARE:
                return state.setAndContinue(ANIM_STARE);
            case STATE_ATTACK:
                return state.setAndContinue(ANIM_ATTACK);
            case STATE_STALK:
                return state.setAndContinue(state.isMoving() ? ANIM_WALK : ANIM_IDLE);
            default:
                return state.setAndContinue(ANIM_IDLE);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
