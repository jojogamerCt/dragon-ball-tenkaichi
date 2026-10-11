package com.dbtenkaichi.client.fx;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.config.ClientConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.FighterNpc;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.player.RuntimeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class CombatFxHandler {
    private CombatFxHandler() {
    }

    private static float budget() {
        return ConfigManager.client().quality().particleBudget;
    }

    private static int count(int base) {
        return Math.max(1, Math.round(base * budget()));
    }

    public static void handle(Packets.CombatFx packet) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        CombatFxKind kind = CombatFxKind.byOrdinal(packet.kind());
        Vec3 pos = new Vec3(packet.x(), packet.y(), packet.z());
        RandomSource random = level.getRandom();
        float strength = packet.strength();
        int localId = mc.player.getId();
        boolean involved = packet.attackerId() == localId || packet.targetId() == localId;
        double distance = mc.player.position().distanceTo(pos);
        float proximity = (float) Math.max(0.0, 1.0 - distance / 32.0);
        switch (kind) {
            case HIT_LIGHT, HIT_HEAVY -> {
                boolean heavy = kind == CombatFxKind.HIT_HEAVY;
                burst(level, ParticleTypes.CRIT, pos, count(heavy ? 14 : 7), 0.5 + strength * 0.2);
                level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, heavy ? 0xFFF2C0 : 0xFFFFFF), pos.x, pos.y, pos.z, 0, 0, 0);
                if (heavy) {
                    burst(level, ParticleTypes.CLOUD, pos, count(6), 0.25);
                    level.addParticle(ParticleTypes.SMALL_GUST, pos.x, pos.y, pos.z, 0, 0, 0);
                }
                hitstop(packet, heavy ? 4.0f : 2.0f);
                if (involved) {
                    CameraEffects.addShake(heavy ? 0.9f + strength * 0.2f : 0.35f);
                } else {
                    CameraEffects.addShake((heavy ? 0.3f : 0.1f) * proximity);
                }
            }
            case GUARD -> {
                burst(level, ParticleTypes.ELECTRIC_SPARK, pos, count(8), 0.4);
                level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, 0x9FD8FF), pos.x, pos.y, pos.z, 0, 0, 0);
                hitstop(packet, 1.5f);
                if (involved) {
                    CameraEffects.addShake(0.25f);
                }
            }
            case PERFECT_GUARD -> {
                ring(level, ParticleTypes.END_ROD, pos, count(16), 0.35, 0.0);
                level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFF), pos.x, pos.y, pos.z, 0, 0, 0);
                hitstop(packet, 5.0f);
                if (involved) {
                    CameraEffects.addShake(0.5f);
                }
            }
            case GUARD_BREAK -> {
                burst(level, ParticleTypes.CRIT, pos, count(20), 0.8);
                level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 0, 0, 0);
                hitstop(packet, 4.0f);
                if (involved) {
                    CameraEffects.addShake(1.0f);
                }
            }
            case MELEE_CLASH -> {
                level.addParticle(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 0, 0, 0);
                burst(level, ParticleTypes.ELECTRIC_SPARK, pos, count(16), 0.7);
                hitstop(packet, 5.0f);
                CameraEffects.addShake(involved ? 1.1f : 0.4f * proximity);
            }
            case DODGE, DASH -> {
                burst(level, ParticleTypes.CLOUD, pos, count(kind == CombatFxKind.DASH ? 10 : 6), 0.15);
                if (kind == CombatFxKind.DASH) {
                    level.addParticle(ParticleTypes.SMALL_GUST, pos.x, pos.y, pos.z, 0, 0, 0);
                }
            }
            case VANISH -> {
                burst(level, ParticleTypes.END_ROD, pos, count(12), 0.3);
                level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, 0xDDF4FF), pos.x, pos.y, pos.z, 0, 0, 0);
            }
            case WHIFF -> burst(level, ParticleTypes.POOF, pos, count(3), 0.05);
            case KI_IMPACT -> {
                level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, 0xBFE6FF), pos.x, pos.y, pos.z, 0, 0, 0);
                burst(level, ParticleTypes.END_ROD, pos, count(8), 0.3);
            }
            case EXPLOSION -> explosion(level, pos, strength, proximity, involved);
            case SHOCKWAVE -> {
                ring(level, ParticleTypes.CLOUD, pos.add(0, 0.2, 0), count(28), 0.45 + strength * 0.2, 0.02);
                level.addParticle(ParticleTypes.GUST, pos.x, pos.y + 0.5, pos.z, 0, 0, 0);
                debris(level, pos, strength, count(10));
                CameraEffects.addShake(strength * 0.5f * Math.max(proximity, involved ? 1.0f : 0.0f));
            }
            case AURA_BURST -> {
                Entity entity = level.getEntity(packet.attackerId());
                int color = entity == null ? 0xFFFFFF : ClientState.forEntity(entity).auraColor();
                ring(level, new DustParticleOptions(color, 2.0f), pos.add(0, 0.8, 0), count(36), 0.5 + strength * 0.15, 0.05);
                level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, color), pos.x, pos.y + 1.0, pos.z, 0, 0, 0);
                CameraEffects.addShake(strength * 0.4f * Math.max(proximity, involved ? 1.0f : 0.0f));
            }
            case LAND -> {
                ring(level, ParticleTypes.DUST_PLUME, pos.add(0, 0.1, 0), count(18), 0.3 + strength * 0.15, 0.03);
                debris(level, pos, strength, count(12));
                if (involved) {
                    CameraEffects.addShake(strength * 0.5f);
                }
            }
            case SONIC_BOOM -> {
                level.addParticle(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 0, 0, 0);
                ring(level, ParticleTypes.CLOUD, pos, count(20), 0.6, 0.0);
                if (involved) {
                    CameraEffects.addShake(0.6f);
                }
            }
            case ZENKAI, LEVEL_UP -> {
                int color = kind == CombatFxKind.ZENKAI ? 0xFFD54A : 0x9FFFE0;
                for (int i = 0; i < count(kind == CombatFxKind.ZENKAI ? 40 : 14); i++) {
                    double a = random.nextDouble() * Math.PI * 2;
                    double r = 0.6 + random.nextDouble() * 0.4;
                    level.addParticle(new DustParticleOptions(color, 1.4f), pos.x + Math.cos(a) * r, pos.y + random.nextDouble() * 2.0, pos.z + Math.sin(a) * r, 0, 0.12, 0);
                }
            }
        }
    }

    private static void hitstop(Packets.CombatFx packet, float ticks) {
        if (!ConfigManager.client().hitstop) {
            return;
        }
        float now = ClientState.clientTicks;
        EntityVisual attacker = ClientState.existing(packet.attackerId());
        EntityVisual target = ClientState.existing(packet.targetId());
        if (attacker != null) {
            attacker.anim.freeze(now, ticks);
        }
        if (target != null && target != attacker) {
            target.anim.freeze(now, ticks);
        }
    }

    private static void explosion(ClientLevel level, Vec3 pos, float radius, float proximity, boolean involved) {
        if (radius >= 3.0f) {
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 0, 0, 0);
        }
        int n = count((int) (4 + radius * 3));
        RandomSource random = level.getRandom();
        for (int i = 0; i < n; i++) {
            level.addParticle(ParticleTypes.EXPLOSION, pos.x + (random.nextDouble() - 0.5) * radius, pos.y + (random.nextDouble() - 0.5) * radius, pos.z + (random.nextDouble() - 0.5) * radius, 0, 0, 0);
        }
        level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFF6D8), pos.x, pos.y, pos.z, 0, 0, 0);
        ring(level, ParticleTypes.LARGE_SMOKE, pos, count((int) (10 + radius * 4)), 0.25 + radius * 0.08, 0.02);
        debris(level, pos, radius, count((int) (8 + radius * 5)));
        CameraEffects.addShake(Math.min(2.5f, radius * 0.45f) * Math.max(proximity, involved ? 1.0f : 0.0f));
    }

    private static void debris(ClientLevel level, Vec3 pos, float strength, int n) {
        BlockPos below = BlockPos.containing(pos.x, pos.y - 0.5, pos.z);
        BlockState state = level.getBlockState(below);
        if (state.isAir()) {
            below = below.below();
            state = level.getBlockState(below);
        }
        if (state.isAir()) {
            return;
        }
        RandomSource random = level.getRandom();
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);
        for (int i = 0; i < n; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double speed = 0.15 + random.nextDouble() * 0.25 * strength;
            level.addParticle(particle, pos.x, pos.y + 0.2, pos.z, Math.cos(a) * speed, 0.2 + random.nextDouble() * 0.4 * strength, Math.sin(a) * speed);
        }
    }

    private static void burst(ClientLevel level, ParticleOptions particle, Vec3 pos, int n, double speed) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < n; i++) {
            level.addParticle(particle, pos.x, pos.y, pos.z, (random.nextDouble() - 0.5) * speed, (random.nextDouble() - 0.5) * speed, (random.nextDouble() - 0.5) * speed);
        }
    }

    private static void ring(ClientLevel level, ParticleOptions particle, Vec3 pos, int n, double speed, double lift) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            level.addParticle(particle, pos.x, pos.y, pos.z, Math.cos(a) * speed, lift, Math.sin(a) * speed);
        }
    }

    public static void clashTick(Packets.Clash packet) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Vec3 pos = new Vec3(packet.x(), packet.y(), packet.z());
        burst(level, ParticleTypes.ELECTRIC_SPARK, pos, count(10), 1.2);
        level.addParticle(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFF), pos.x, pos.y, pos.z, 0, 0, 0);
        if (packet.pulseTick() == 0) {
            level.addParticle(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    public static void cinematic(Packets.Cinematic packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(packet.entityId());
        if (entity != null && packet.full()) {
            ring(mc.level, ParticleTypes.CLOUD, entity.position().add(0, 0.1, 0), count(24), 0.4, 0.0);
        }
    }

    public static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        ClientConfig.Quality quality = ConfigManager.client().quality();
        RandomSource random = level.getRandom();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof Player) && !(entity instanceof FighterNpc)) {
                continue;
            }
            if (entity.distanceToSqr(mc.player) > 64 * 64) {
                continue;
            }
            EntityVisual visual = ClientState.forEntity(entity);
            if (visual.aura > 0.3f) {
                int color = visual.auraColor();
                int n = Math.max(1, Math.round(quality.particleBudget * 3 * visual.aura));
                for (int i = 0; i < n; i++) {
                    double a = random.nextDouble() * Math.PI * 2;
                    double r = entity.getBbWidth() * (0.6 + random.nextDouble() * 0.5);
                    level.addParticle(new DustParticleOptions(color, 0.9f + random.nextFloat() * 0.8f), entity.getX() + Math.cos(a) * r, entity.getY() + random.nextDouble() * 0.4, entity.getZ() + Math.sin(a) * r, 0, 0.15 + random.nextDouble() * 0.15, 0);
                }
                if (visual.auraLightning() && random.nextFloat() < 0.3f * quality.particleBudget) {
                    level.addParticle(ParticleTypes.ELECTRIC_SPARK, entity.getX() + (random.nextDouble() - 0.5) * 1.2, entity.getY() + random.nextDouble() * entity.getBbHeight(), entity.getZ() + (random.nextDouble() - 0.5) * 1.2, 0, 0, 0);
                }
                if ((visual.flags & RuntimeState.FLAG_CHARGING) != 0 && entity.onGround() && random.nextFloat() < 0.5f * quality.particleBudget) {
                    debris(level, entity.position(), 0.6f, 1);
                }
            }
            boolean cinematic = ClientState.clientTicks - visual.cinematicStart < visual.cinematicTicks;
            if (cinematic && visual.cinematicFull && random.nextFloat() < quality.particleBudget) {
                debris(level, entity.position().add((random.nextDouble() - 0.5) * 4, 0, (random.nextDouble() - 0.5) * 4), 1.2f, 2);
                double a = random.nextDouble() * Math.PI * 2;
                level.addParticle(ParticleTypes.CLOUD, entity.getX() + Math.cos(a) * 3, entity.getY() + 0.2, entity.getZ() + Math.sin(a) * 3, -Math.cos(a) * 0.2, 0.05, -Math.sin(a) * 0.2);
            }
        }
    }
}
