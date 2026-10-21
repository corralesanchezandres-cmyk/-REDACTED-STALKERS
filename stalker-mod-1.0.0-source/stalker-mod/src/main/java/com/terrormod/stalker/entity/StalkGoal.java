package com.terrormod.stalker.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Comportamiento del Stalker:
 *  1) FASE DE OBSERVACION: te sigue a distancia (14-30 bloques). Si te acercas, desaparece.
 *  2) Pasado un tiempo (RIPEN_TICKS) se vuelve agresivo y se acerca cuando NO lo miras.
 *  3) Si lo miras, se congela (animacion "stare"). Si lo miras mucho tiempo, desaparece.
 *  4) Si llega hasta ti sin que lo mires, te da un susto: dano + ceguera + oscuridad.
 */
public class StalkGoal extends Goal {
    // ---- Ajustes ----
    private static final double DETECT_RANGE = 64.0D;
    private static final int RIPEN_TICKS = 1200;        // 60 s antes de volverse agresivo
    private static final int MAX_STARE_TICKS = 140;     // 7 s mirandolo y se esfuma
    private static final double LOOK_THRESHOLD = 0.93D; // cono de "lo estoy mirando"
    private static final float SCARE_DAMAGE = 6.0F;     // 3 corazones
    // -----------------

    private final StalkerEntity mob;
    private Player target;
    private int age;
    private int stareTicks;
    private int scareTimer = -1;

    public StalkGoal(StalkerEntity mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = mob.level().getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(),
                DETECT_RANGE, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !target.isCreative() && !target.isSpectator()
                && mob.distanceToSqr(target) < (DETECT_RANGE + 16) * (DETECT_RANGE + 16);
    }

    @Override
    public void start() {
        age = 0;
        stareTicks = 0;
        scareTimer = -1;
        mob.setState(StalkerEntity.STATE_STALK);
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.setState(StalkerEntity.STATE_IDLE);
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        age++;

        // Despues del susto, se esfuma tras una breve pausa
        if (scareTimer >= 0) {
            mob.getNavigation().stop();
            mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
            if (--scareTimer <= 0) mob.vanish();
            return;
        }

        double dist = mob.distanceTo(target);
        boolean watched = isLookedAt(target);
        boolean aggressive = age > RIPEN_TICKS;

        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);

        if (watched) {
            // Congelado mientras lo miras
            mob.getNavigation().stop();
            mob.setState(StalkerEntity.STATE_STARE);
            stareTicks++;
            if (stareTicks % 40 == 1 && dist < 30) {
                mob.level().playSound(null, target.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
                        SoundSource.HOSTILE, 1.5F, 0.8F);
            }
            if (stareTicks > MAX_STARE_TICKS) mob.vanish();
            return;
        }

        stareTicks = Math.max(0, stareTicks - 2);

        if (!aggressive) {
            // Fase de observacion: mantiene distancia
            if (dist < 12.0D) {
                mob.vanish();
                return;
            }
            if (dist > 30.0D) {
                moveToward(1.0D, StalkerEntity.STATE_STALK);
            } else {
                mob.getNavigation().stop();
                mob.setState(StalkerEntity.STATE_IDLE);
            }
            if (age % 400 == 0) {
                mob.level().playSound(null, target.blockPosition(), SoundEvents.AMBIENT_CAVE.value(),
                        SoundSource.AMBIENT, 1.0F, 0.7F);
            }
            return;
        }

        // Fase agresiva: se acerca cuando no lo miras
        if (dist <= 2.4D) {
            scare();
        } else if (dist > 9.0D) {
            moveToward(1.9D, StalkerEntity.STATE_RUN);
        } else {
            moveToward(1.2D, StalkerEntity.STATE_STALK);
        }
    }

    private void moveToward(double speed, int state) {
        mob.setState(state);
        if (mob.tickCount % 8 == 0) {
            mob.getNavigation().moveTo(target, speed);
        }
    }

    private void scare() {
        mob.getNavigation().stop();
        mob.setState(StalkerEntity.STATE_ATTACK);
        target.hurt(mob.damageSources().mobAttack(mob), SCARE_DAMAGE);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
        mob.level().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_SCREAM,
                SoundSource.HOSTILE, 2.0F, 0.5F);
        scareTimer = 24;
    }

    /** True si el jugador tiene al Stalker dentro de su cono de vision y con linea de vista. */
    private boolean isLookedAt(Player player) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 toMob = mob.getEyePosition().subtract(player.getEyePosition()).normalize();
        return look.dot(toMob) > LOOK_THRESHOLD && player.hasLineOfSight(mob);
    }
}
