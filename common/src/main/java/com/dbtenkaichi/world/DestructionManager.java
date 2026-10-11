package com.dbtenkaichi.world;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.combat.TenkaichiDamage;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.config.GameplayConfig;
import com.dbtenkaichi.config.TenkaichiConfig;
import com.dbtenkaichi.combat.DamageMath;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class DestructionManager {
    private static final int TICK_BUDGET = 900;
    private static final Deque<Restore> RESTORE = new ArrayDeque<>();
    private static int budgetLeft = TICK_BUDGET;

    private DestructionManager() {
    }

    public static void tick(MinecraftServer server) {
        budgetLeft = TICK_BUDGET;
        long now = System.currentTimeMillis();
        int restored = 0;
        while (!RESTORE.isEmpty() && RESTORE.peekFirst().at <= now && restored < 200) {
            Restore r = RESTORE.pollFirst();
            restore(server, r);
            restored++;
        }
    }

    public static void restoreAll(MinecraftServer server) {
        int count = 0;
        while (!RESTORE.isEmpty()) {
            restore(server, RESTORE.pollFirst());
            count++;
        }
        if (count > 0) {
            Tenkaichi.LOG.info("Restored {} blocks from temporary battle damage", count);
        }
    }

    private static void restore(MinecraftServer server, Restore r) {
        ServerLevel level = server.getLevel(r.dimension);
        if (level != null && level.isLoaded(r.pos) && level.getBlockState(r.pos).isAir()) {
            level.setBlock(r.pos, r.state, 3);
        }
    }

    public static void explode(ServerLevel level, Vec3 center, double radius, @Nullable LivingEntity source, double baseDamage, double chargeRatio) {
        TenkaichiConfig cfg = ConfigManager.get();
        GameplayConfig gp = cfg.gameplay;
        BalanceConfig balance = cfg.balance;
        double r = Math.max(0.5, Math.min(gp.maxExplosionRadius, radius));
        damageArea(level, center, r * 1.25, source, baseDamage, chargeRatio, balance);
        sendNear(level, center, 128, new Packets.CombatFx(CombatFxKind.EXPLOSION.ordinal(), source == null ? -1 : source.getId(), -1, center.x, center.y, center.z, (float) r));
        level.playSound(null, center.x, center.y, center.z, ModSounds.EXPLOSION.get(), SoundSource.PLAYERS, (float) Math.min(4.0, 1.0 + r * 0.5), 0.8f + level.getRandom().nextFloat() * 0.2f);
        TenkaichiConfig.DestructionMode mode = cfg.destruction();
        if (mode == TenkaichiConfig.DestructionMode.OFF || mode == TenkaichiConfig.DestructionMode.VISUAL) {
            return;
        }
        if (gp.destructionDimensionBlacklist.contains(level.dimension().identifier().toString())) {
            return;
        }
        if (!(source instanceof Player) && !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        carve(level, center, r, source instanceof ServerPlayer sp ? sp : null, mode == TenkaichiConfig.DestructionMode.REGENERATING, gp);
    }

    private static void damageArea(ServerLevel level, Vec3 center, double radius, @Nullable LivingEntity source, double baseDamage, double chargeRatio, BalanceConfig balance) {
        if (baseDamage <= 0) {
            return;
        }
        Fighter attacker = source == null ? null : Fighter.of(source);
        AABB box = new AABB(center, center).inflate(radius);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && !e.isSpectator());
        for (LivingEntity victim : victims) {
            if (source != null && (victim == source || !CombatSystem.canHarm(source, victim))) {
                continue;
            }
            double dist = victim.getBoundingBox().getCenter().distanceTo(center);
            if (dist > radius) {
                continue;
            }
            double falloff = 1.0 - (dist / radius) * 0.6;
            Fighter defender = Fighter.of(victim);
            double raw = attacker == null ? baseDamage : DamageMath.ki(baseDamage, attacker.stat(StatType.KI_OUTPUT), chargeRatio, attacker.kiMultiplier(balance), balance);
            raw *= falloff;
            double guardFactor = 1.0;
            var rt = CombatSystem.runtime(victim);
            DamageMath.GuardState guard = rt != null && rt.guarding ? DamageMath.GuardState.GUARD : DamageMath.GuardState.NONE;
            if (rt != null && rt.invulnerableUntil > level.getGameTime()) {
                continue;
            }
            double damage = DamageMath.finalDamage(raw * guardFactor, attacker == null ? defender.battlePower() : attacker.battlePower(), defender.battlePower(),
                    defender.stat(StatType.DEFENSE), defender.defenseMultiplier(balance), guard, defender.fatigue(), balance);
            damage *= 1.0 - Math.min(0.5, defender.stat(StatType.SPIRIT) * 0.002);
            TenkaichiDamage.hurt(victim, level, TenkaichiDamage.source(level, TenkaichiDamage.KI_EXPLOSION, source, source), (float) damage);
            Vec3 push = victim.position().subtract(center);
            if (push.lengthSqr() > 1.0e-4) {
                CombatSystem.knockback(victim, push.normalize().scale(0.4 + falloff * 0.8).add(0, 0.3 * falloff, 0));
            }
            if (rt != null) {
                rt.lastCombat = level.getGameTime();
                rt.charging = false;
                rt.stunnedUntil = Math.max(rt.stunnedUntil, level.getGameTime() + 8);
            }
        }
    }

    private static void carve(ServerLevel level, Vec3 center, double radius, @Nullable ServerPlayer player, boolean regenerate, GameplayConfig gp) {
        int limit = Math.min(gp.maxBlocksPerExplosion, budgetLeft);
        if (limit <= 0) {
            return;
        }
        RandomSource random = level.getRandom();
        BlockPos origin = BlockPos.containing(center);
        int ri = (int) Math.ceil(radius);
        List<BlockPos> targets = new ArrayList<>();
        for (int dy = -ri; dy <= ri; dy++) {
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    double noise = 0.75 + random.nextDouble() * 0.35;
                    double d2 = dx * dx + dy * dy * 1.3 + dz * dz;
                    if (d2 > radius * radius * noise) {
                        continue;
                    }
                    targets.add(origin.offset(dx, dy, dz));
                }
            }
        }
        targets.sort((a, b) -> Double.compare(a.distSqr(origin), b.distSqr(origin)));
        long expire = System.currentTimeMillis() + Math.max(1, gp.terrainRegenSeconds) * 1000L;
        int removed = 0;
        for (BlockPos pos : targets) {
            if (removed >= limit) {
                break;
            }
            if (!level.isLoaded(pos) || level.isOutsideBuildHeight(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness < 0 || hardness > gp.maxBlockHardness || state.hasBlockEntity()) {
                continue;
            }
            if (player != null && !Services.PLATFORM.canBreakBlock(player, pos, state)) {
                continue;
            }
            if (gp.protectSpawnArea && player != null && level.getServer().isUnderSpawnProtection(level, pos, player)) {
                continue;
            }
            if (regenerate) {
                RESTORE.addLast(new Restore(level.dimension(), pos.immutable(), state, expire + removed * 15L));
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            removed++;
        }
        budgetLeft -= removed;
    }

    public static void sendNear(ServerLevel level, Vec3 pos, double range, CustomPacketPayload payload) {
        double r2 = range * range;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos) <= r2) {
                Services.NETWORK.sendToPlayer(player, payload);
            }
        }
    }

    private record Restore(ResourceKey<Level> dimension, BlockPos pos, BlockState state, long at) {
    }
}
