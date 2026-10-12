package com.dbtenkaichi.client.fx;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.config.ClientConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.FighterNpc;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public final class CombatFxHandler {
    private static final int WHITE = 0xFFFFFF;
    private static final int HIT_GOLD = 0xFFE7A0;
    private static final int GUARD_BLUE = 0x8FD4FF;
    private static final int DUST = 0xC8BCA8;
    private static final int FIRE = 0xFF9A3C;

    private CombatFxHandler() {
    }

    private static float budget() {
        return ConfigManager.client().quality().particleBudget;
    }

    private static int count(int base) {
        return Math.max(1, Math.round(base * budget()));
    }

    private static void spawn(Supplier<ParticleType<ColorParticleOption>> type, int color, double x, double y, double z, double dx, double dy, double dz, float scale) {
        Particle particle = Minecraft.getInstance().particleEngine.createParticle(ModParticles.of(type, color), x, y, z, dx, dy, dz);
        if (particle != null && scale != 1.0f) {
            particle.scale(scale);
        }
    }

    private static void spawn(Supplier<ParticleType<ColorParticleOption>> type, int color, Vec3 pos, float scale) {
        spawn(type, color, pos.x, pos.y, pos.z, 0, 0, 0, scale);
    }

    private static void burst(Supplier<ParticleType<ColorParticleOption>> type, int color, Vec3 pos, int n, double speed, float scale) {
        RandomSource random = Minecraft.getInstance().particleEngine.getRandom();
        for (int i = 0; i < n; i++) {
            double dx = random.nextGaussian();
            double dy = random.nextGaussian();
            double dz = random.nextGaussian();
            double len = Math.max(1.0e-3, Math.sqrt(dx * dx + dy * dy + dz * dz));
            double s = speed * (0.5 + random.nextDouble() * 0.5) / len;
            spawn(type, color, pos.x, pos.y, pos.z, dx * s, dy * s, dz * s, scale);
        }
    }

    private static void ring(Supplier<ParticleType<ColorParticleOption>> type, int color, Vec3 pos, int n, double speed, double lift, float scale) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            spawn(type, color, pos.x, pos.y, pos.z, Math.cos(a) * speed, lift, Math.sin(a) * speed, scale);
        }
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
                spawn(ModParticles.IMPACT_STAR, heavy ? HIT_GOLD : WHITE, pos, heavy ? 1.3f + strength * 0.2f : 0.8f);
                burst(ModParticles.KI_SPARK, HIT_GOLD, pos, count(heavy ? 14 : 6), 0.35 + strength * 0.12, 1.0f);
                if (heavy) {
                    spawn(ModParticles.SHOCK_RING, WHITE, pos, 0.9f + strength * 0.2f);
                    burst(ModParticles.SPEED_LINE, WHITE, pos, count(6), 0.6, 1.0f);
                    burst(ModParticles.DUST_PUFF, DUST, pos, count(4), 0.08, 0.8f);
                }
                hitstop(packet, heavy ? 4.0f : 2.0f);
                if (involved) {
                    CameraEffects.addShake(heavy ? 0.9f + strength * 0.2f : 0.35f);
                } else {
                    CameraEffects.addShake((heavy ? 0.3f : 0.1f) * proximity);
                }
            }
            case GUARD -> {
                spawn(ModParticles.SHOCK_RING, GUARD_BLUE, pos, 0.6f);
                burst(ModParticles.KI_SPARK, GUARD_BLUE, pos, count(8), 0.3, 1.0f);
                hitstop(packet, 1.5f);
                if (involved) {
                    CameraEffects.addShake(0.25f);
                }
            }
            case PERFECT_GUARD -> {
                spawn(ModParticles.SHOCK_RING, WHITE, pos, 1.2f);
                spawn(ModParticles.IMPACT_STAR, 0xBFF0FF, pos, 1.1f);
                ring(ModParticles.KI_SPARK, 0xDDF6FF, pos, count(16), 0.35, 0.0, 1.2f);
                hitstop(packet, 5.0f);
                if (involved) {
                    CameraEffects.addShake(0.5f);
                }
            }
            case GUARD_BREAK -> {
                spawn(ModParticles.IMPACT_STAR, 0xFFB050, pos, 1.6f);
                spawn(ModParticles.SHOCK_RING, 0xFFB050, pos, 1.2f);
                burst(ModParticles.KI_SPARK, 0xFFC070, pos, count(20), 0.7, 1.2f);
                hitstop(packet, 4.0f);
                if (involved) {
                    CameraEffects.addShake(1.0f);
                }
            }
            case MELEE_CLASH -> {
                spawn(ModParticles.SHOCK_RING, WHITE, pos, 2.0f);
                spawn(ModParticles.IMPACT_STAR, HIT_GOLD, pos, 1.8f);
                for (int i = 0; i < count(3); i++) {
                    spawn(ModParticles.LIGHTNING, 0xCFE8FF, pos.x + (random.nextDouble() - 0.5), pos.y + (random.nextDouble() - 0.5), pos.z + (random.nextDouble() - 0.5), 0, 0, 0, 1.2f);
                }
                burst(ModParticles.KI_SPARK, WHITE, pos, count(16), 0.7, 1.0f);
                hitstop(packet, 5.0f);
                CameraEffects.addShake(involved ? 1.1f : 0.4f * proximity);
            }
            case DODGE, DASH -> {
                boolean dash = kind == CombatFxKind.DASH;
                burst(ModParticles.SPEED_LINE, WHITE, pos, count(dash ? 10 : 6), 0.5, 1.2f);
                ring(ModParticles.DUST_PUFF, DUST, pos.add(0, -0.6, 0), count(dash ? 10 : 6), 0.15, 0.01, 0.8f);
            }
            case VANISH -> {
                burst(ModParticles.ENERGY_ORB, 0xDDF4FF, pos, count(12), 0.25, 1.0f);
                spawn(ModParticles.SHOCK_RING, 0xDDF4FF, pos, 1.0f);
                burst(ModParticles.SPEED_LINE, WHITE, pos, count(8), 0.8, 1.0f);
            }
            case WHIFF -> burst(ModParticles.SPEED_LINE, 0xE0E0E0, pos, count(3), 0.3, 0.7f);
            case KI_IMPACT -> {
                spawn(ModParticles.IMPACT_STAR, 0xBFE6FF, pos, 1.2f);
                burst(ModParticles.ENERGY_ORB, 0x9FD8FF, pos, count(8), 0.25, 1.0f);
                burst(ModParticles.KI_SPARK, WHITE, pos, count(8), 0.4, 1.0f);
            }
            case EXPLOSION -> explosion(level, pos, strength, proximity, involved);
            case SHOCKWAVE -> {
                spawn(ModParticles.SHOCK_RING, 0xFFF0D0, pos.add(0, 0.4, 0), 2.0f + strength * 0.5f);
                ring(ModParticles.DUST_PUFF, DUST, pos.add(0, 0.2, 0), count(28), 0.4 + strength * 0.2, 0.02, 1.4f);
                debris(level, pos, strength, count(10));
                CameraEffects.addShake(strength * 0.5f * Math.max(proximity, involved ? 1.0f : 0.0f));
            }
            case AURA_BURST -> {
                Entity entity = level.getEntity(packet.attackerId());
                int color = entity == null ? WHITE : ClientState.forEntity(entity).auraColor();
                Vec3 center = pos.add(0, 0.9, 0);
                spawn(ModParticles.SHOCK_RING, color, center, 2.2f + strength * 0.3f);
                spawn(ModParticles.IMPACT_STAR, color, center, 1.6f);
                ring(ModParticles.AURA_FLAME, color, pos.add(0, 0.1, 0), count(24), 0.35 + strength * 0.1, 0.12, 1.4f);
                burst(ModParticles.KI_SPARK, color, center, count(16), 0.6, 1.2f);
                ring(ModParticles.DUST_PUFF, DUST, pos.add(0, 0.1, 0), count(14), 0.3, 0.02, 1.2f);
                CameraEffects.addShake(strength * 0.4f * Math.max(proximity, involved ? 1.0f : 0.0f));
            }
            case LAND -> {
                spawn(ModParticles.SHOCK_RING, 0xFFF0D0, pos.add(0, 0.3, 0), 1.0f + strength * 0.4f);
                ring(ModParticles.DUST_PUFF, DUST, pos.add(0, 0.1, 0), count(18), 0.25 + strength * 0.15, 0.03, 1.1f);
                debris(level, pos, strength, count(12));
                if (involved) {
                    CameraEffects.addShake(strength * 0.5f);
                }
            }
            case SONIC_BOOM -> {
                spawn(ModParticles.SHOCK_RING, WHITE, pos, 3.0f);
                ring(ModParticles.SPEED_LINE, WHITE, pos, count(20), 0.9, 0.0, 1.4f);
                if (involved) {
                    CameraEffects.addShake(0.6f);
                }
            }
            case ZENKAI, LEVEL_UP -> {
                int color = kind == CombatFxKind.ZENKAI ? 0xFFD54A : 0x9FFFE0;
                for (int i = 0; i < count(kind == CombatFxKind.ZENKAI ? 40 : 14); i++) {
                    double a = random.nextDouble() * Math.PI * 2;
                    double r = 0.6 + random.nextDouble() * 0.4;
                    spawn(ModParticles.ENERGY_ORB, color, pos.x + Math.cos(a) * r, pos.y + random.nextDouble() * 2.0, pos.z + Math.sin(a) * r, 0, 0.08, 0, 1.0f);
                }
                spawn(ModParticles.SHOCK_RING, color, pos.add(0, 1.0, 0), 2.0f);
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
        RandomSource random = level.getRandom();
        spawn(ModParticles.IMPACT_STAR, 0xFFF6D8, pos, 2.0f + radius * 0.6f);
        spawn(ModParticles.SHOCK_RING, 0xFFE0B0, pos, 2.0f + radius);
        int n = count((int) (6 + radius * 4));
        for (int i = 0; i < n; i++) {
            double ox = (random.nextDouble() - 0.5) * radius;
            double oy = (random.nextDouble() - 0.5) * radius;
            double oz = (random.nextDouble() - 0.5) * radius;
            spawn(ModParticles.ENERGY_ORB, random.nextBoolean() ? FIRE : 0xFFD070, pos.x + ox, pos.y + oy, pos.z + oz, ox * 0.05, oy * 0.05 + 0.02, oz * 0.05, 2.5f + radius * 0.3f);
        }
        burst(ModParticles.KI_SPARK, 0xFFE0A0, pos, count((int) (10 + radius * 4)), 0.5 + radius * 0.1, 1.4f);
        ring(ModParticles.DUST_PUFF, 0x9A9088, pos, count((int) (10 + radius * 4)), 0.2 + radius * 0.08, 0.03, 2.0f);
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

    public static void clashTick(Packets.Clash packet) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Vec3 pos = new Vec3(packet.x(), packet.y(), packet.z());
        RandomSource random = level.getRandom();
        burst(ModParticles.KI_SPARK, WHITE, pos, count(10), 1.0, 1.4f);
        spawn(ModParticles.IMPACT_STAR, WHITE, pos, 2.0f + random.nextFloat());
        spawn(ModParticles.LIGHTNING, 0xCFE8FF, pos.x + (random.nextDouble() - 0.5) * 2, pos.y + (random.nextDouble() - 0.5) * 2, pos.z + (random.nextDouble() - 0.5) * 2, 0, 0, 0, 2.0f);
        if (packet.pulseTick() == 0) {
            spawn(ModParticles.SHOCK_RING, WHITE, pos, 4.0f);
        }
    }

    public static void cinematic(Packets.Cinematic packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(packet.entityId());
        if (entity != null && packet.full()) {
            int color = ClientState.forEntity(entity).auraColor();
            ring(ModParticles.DUST_PUFF, DUST, entity.position().add(0, 0.1, 0), count(24), 0.4, 0.0, 1.5f);
            spawn(ModParticles.SHOCK_RING, color, entity.position().add(0, 1.0, 0), 3.0f);
        }
    }

    private static void chargeParticles(Entity entity, RandomSource random, ClientConfig.Quality quality, int color) {
        Vec3 look = entity.getViewVector(1.0f).multiply(1, 0, 1);
        if (look.lengthSqr() < 1.0e-4) {
            look = new Vec3(0, 0, 1);
        }
        look = look.normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        Vec3 hands = entity.position().add(0, entity.getBbHeight() * 0.45, 0).add(look.scale(-0.25)).add(side.scale(0.5));
        int n = Math.max(1, Math.round(3 * quality.particleBudget));
        for (int i = 0; i < n; i++) {
            Vec3 from = hands.add((random.nextDouble() - 0.5) * 2.4, (random.nextDouble() - 0.5) * 2.4, (random.nextDouble() - 0.5) * 2.4);
            Vec3 vel = hands.subtract(from).scale(0.18);
            spawn(ModParticles.KI_SPARK, random.nextBoolean() ? color : 0xE8F8FF, from.x, from.y, from.z, vel.x, vel.y, vel.z, 1.2f);
        }
        spawn(ModParticles.ENERGY_ORB, color, hands.x, hands.y, hands.z, 0, 0, 0, 1.6f + random.nextFloat() * 0.6f);
        if (random.nextFloat() < 0.25f * quality.particleBudget) {
            spawn(ModParticles.LIGHTNING, 0xCFE8FF, hands.x + (random.nextDouble() - 0.5) * 0.6, hands.y + (random.nextDouble() - 0.5) * 0.6, hands.z + (random.nextDouble() - 0.5) * 0.6, 0, 0, 0, 0.6f);
        }
    }

    private static void beamParticles(com.dbtenkaichi.entity.KiBeamEntity beam, RandomSource random, ClientConfig.Quality quality) {
        Vec3 dir = beam.direction();
        if (dir.lengthSqr() < 1.0e-4) {
            return;
        }
        Vec3 start = beam.position();
        float length = beam.length();
        float radius = beam.radius();
        int glow = beam.glowColor();
        int n = Math.max(1, Math.round(4 * quality.particleBudget));
        for (int i = 0; i < n; i++) {
            Vec3 at = start.add(dir.scale(random.nextDouble() * length));
            Vec3 jitter = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(radius * 0.6);
            Vec3 p = at.add(jitter);
            spawn(ModParticles.KI_SPARK, random.nextBoolean() ? glow : 0xFFFFFF, p.x, p.y, p.z, jitter.x * 0.08, jitter.y * 0.08, jitter.z * 0.08, 1.4f);
        }
        Vec3 head = beam.head();
        spawn(ModParticles.ENERGY_ORB, glow, head.x, head.y, head.z, 0, 0, 0, 1.2f + radius);
        burst(ModParticles.KI_SPARK, glow, head, Math.max(1, Math.round(3 * quality.particleBudget)), 0.5, 1.4f);
        spawn(ModParticles.ENERGY_ORB, 0xFFFFFF, start.x, start.y, start.z, 0, 0, 0, 1.0f + radius * 0.8f);
    }

    public static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        ClientConfig.Quality quality = ConfigManager.client().quality();
        RandomSource random = level.getRandom();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof com.dbtenkaichi.entity.KiBeamEntity beam) {
                beamParticles(beam, random, quality);
                continue;
            }
            if (entity instanceof com.dbtenkaichi.entity.KiBlastEntity blast && random.nextFloat() < quality.particleBudget) {
                spawn(ModParticles.ENERGY_ORB, blast.color(), blast.getX(), blast.getY() + blast.getBbHeight() * 0.5, blast.getZ(), 0, 0, 0, 0.8f + blast.size());
                continue;
            }
            if (!(entity instanceof Player) && !(entity instanceof FighterNpc)) {
                continue;
            }
            EntityVisual chargeVisual = ClientState.forEntity(entity);
            if ((chargeVisual.flags & RuntimeState.FLAG_SKILL_CHARGING) != 0 && entity.distanceToSqr(mc.player) < 48 * 48) {
                chargeParticles(entity, random, quality, entity instanceof com.dbtenkaichi.entity.RaiderEntity ? 0xB04BFF : 0x3FA8FF);
            }
            if (entity.distanceToSqr(mc.player) > 64 * 64) {
                continue;
            }
            EntityVisual visual = ClientState.forEntity(entity);
            if (visual.aura > 0.3f) {
                int color = visual.auraColor();
                int secondary = visual.auraSecondary();
                int n = Math.max(1, Math.round(quality.particleBudget * 3 * visual.aura));
                for (int i = 0; i < n; i++) {
                    double a = random.nextDouble() * Math.PI * 2;
                    double r = entity.getBbWidth() * (0.5 + random.nextDouble() * 0.6);
                    spawn(ModParticles.AURA_FLAME, random.nextFloat() < 0.3f ? secondary : color, entity.getX() + Math.cos(a) * r, entity.getY() + random.nextDouble() * 0.6,
                            entity.getZ() + Math.sin(a) * r, 0, 0.1 + random.nextDouble() * 0.12, 0, 1.0f + visual.aura * 0.3f);
                }
                if (random.nextFloat() < 0.5f * quality.particleBudget) {
                    double a = random.nextDouble() * Math.PI * 2;
                    spawn(ModParticles.KI_SPARK, color, entity.getX() + Math.cos(a) * 0.5, entity.getY() + random.nextDouble() * entity.getBbHeight(), entity.getZ() + Math.sin(a) * 0.5,
                            0, 0.12, 0, 1.0f);
                }
                if (visual.auraLightning() && random.nextFloat() < 0.35f * quality.particleBudget) {
                    spawn(ModParticles.LIGHTNING, 0xCFE8FF, entity.getX() + (random.nextDouble() - 0.5) * 1.2, entity.getY() + random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + (random.nextDouble() - 0.5) * 1.2, 0, 0, 0, 0.8f + random.nextFloat() * 0.6f);
                }
                if ((visual.flags & RuntimeState.FLAG_CHARGING) != 0 && entity.onGround() && random.nextFloat() < 0.5f * quality.particleBudget) {
                    debris(level, entity.position(), 0.6f, 1);
                    double a = random.nextDouble() * Math.PI * 2;
                    spawn(ModParticles.DUST_PUFF, DUST, entity.getX() + Math.cos(a) * 0.6, entity.getY() + 0.1, entity.getZ() + Math.sin(a) * 0.6, Math.cos(a) * 0.08, 0.01, Math.sin(a) * 0.08, 1.0f);
                }
            }
            boolean cinematic = ClientState.clientTicks - visual.cinematicStart < visual.cinematicTicks;
            if (cinematic && visual.cinematicFull && random.nextFloat() < quality.particleBudget) {
                debris(level, entity.position().add((random.nextDouble() - 0.5) * 4, 0, (random.nextDouble() - 0.5) * 4), 1.2f, 2);
                double a = random.nextDouble() * Math.PI * 2;
                spawn(ModParticles.DUST_PUFF, DUST, entity.getX() + Math.cos(a) * 3, entity.getY() + 0.2, entity.getZ() + Math.sin(a) * 3, -Math.cos(a) * 0.2, 0.05, -Math.sin(a) * 0.2, 1.4f);
                spawn(ModParticles.KI_SPARK, visual.auraColor(), entity.getX() + (random.nextDouble() - 0.5) * 2, entity.getY() + random.nextDouble() * 2, entity.getZ() + (random.nextDouble() - 0.5) * 2,
                        0, 0.2, 0, 1.4f);
            }
        }
    }
}
