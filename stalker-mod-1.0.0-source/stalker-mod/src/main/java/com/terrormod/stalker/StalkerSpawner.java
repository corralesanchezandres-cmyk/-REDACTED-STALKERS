package com.terrormod.stalker;

import com.terrormod.stalker.entity.StalkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Hace aparecer al Stalker detras del jugador, de noche o en zonas oscuras.
 * Ajusta estos valores a tu gusto.
 */
public class StalkerSpawner {
    /** Cada cuantos ticks se intenta (200 = 10 s). */
    private static final int CHECK_INTERVAL = 200;
    /** Probabilidad por intento. 0.08 = aprox. una vez cada 2 minutos. */
    private static final float SPAWN_CHANCE = 0.08F;
    /** Distancia a la que aparece del jugador. */
    private static final double SPAWN_DISTANCE = 26.0D;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL != 0) return;
        if (player.isCreative() || player.isSpectator()) return;

        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return;

        RandomSource random = level.getRandom();
        if (random.nextFloat() > SPAWN_CHANCE) return;

        boolean dark = level.isNight() || level.getMaxLocalRawBrightness(player.blockPosition()) < 7;
        if (!dark) return;

        // Solo un Stalker cerca de cada jugador
        if (!level.getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(100.0D)).isEmpty()) return;

        // Detras del jugador, con algo de variacion de angulo
        float yaw = player.getYRot() + 180.0F + (random.nextFloat() - 0.5F) * 80.0F;
        Vec3 dir = Vec3.directionFromRotation(0.0F, yaw);
        double x = player.getX() + dir.x * SPAWN_DISTANCE;
        double z = player.getZ() + dir.z * SPAWN_DISTANCE;

        BlockPos probe = BlockPos.containing(x, player.getY(), z);
        if (!level.hasChunkAt(probe)) return;
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, probe);
        if (Math.abs(ground.getY() - player.getY()) > 12) return;

        StalkerEntity stalker = StalkerMod.STALKER.get().create(level);
        if (stalker == null) return;
        stalker.moveTo(ground.getX() + 0.5D, ground.getY(), ground.getZ() + 0.5D, yaw + 180.0F, 0.0F);
        if (!level.noCollision(stalker)) return;
        level.addFreshEntity(stalker);
    }
}
