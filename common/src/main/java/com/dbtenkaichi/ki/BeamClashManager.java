package com.dbtenkaichi.ki;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.StatType;
import com.dbtenkaichi.training.TrainingSystem;
import com.dbtenkaichi.world.DestructionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BeamClashManager {
    public static final int PULSE_PERIOD = 16;
    public static final int PULSE_WINDOW = 3;
    public static final int MAX_TICKS = 260;

    private static final Map<Integer, Clash> CLASHES = new LinkedHashMap<>();
    private static int nextId = 1;

    private BeamClashManager() {
    }

    public static void detect(ServerLevel level, KiBeamEntity beam) {
        if (beam.state() != KiBeamEntity.STATE_FIRING || find(beam) != null) {
            return;
        }
        Vec3 head = beam.head();
        double search = beam.radius() + 6.0;
        List<KiBeamEntity> others = level.getEntitiesOfClass(KiBeamEntity.class, new AABB(head, head).inflate(search + 80), b -> b != beam && b.isAlive());
        for (KiBeamEntity other : others) {
            if (other.state() != KiBeamEntity.STATE_FIRING || other.ownerId() == beam.ownerId() || !other.technique().clashable() || find(other) != null) {
                continue;
            }
            if (beam.direction().dot(other.direction()) > -0.3) {
                continue;
            }
            double reach = beam.radius() + other.radius() + 1.5;
            if (closestDistance(beam.position(), beam.head(), other.position(), other.head()) <= reach) {
                start(level, beam, other);
                return;
            }
        }
    }

    private static double closestDistance(Vec3 a0, Vec3 a1, Vec3 b0, Vec3 b1) {
        Vec3 u = a1.subtract(a0);
        Vec3 v = b1.subtract(b0);
        Vec3 w = a0.subtract(b0);
        double a = u.dot(u), b = u.dot(v), c = v.dot(v), d = u.dot(w), e = v.dot(w);
        double denom = a * c - b * b;
        double s = denom < 1.0e-6 ? 0.0 : Math.max(0.0, Math.min(1.0, (b * e - c * d) / denom));
        double t = c < 1.0e-6 ? 0.0 : Math.max(0.0, Math.min(1.0, (b * s + e) / c));
        Vec3 pa = a0.add(u.scale(s));
        Vec3 pb = b0.add(v.scale(t));
        return pa.distanceTo(pb);
    }

    private static void start(ServerLevel level, KiBeamEntity a, KiBeamEntity b) {
        Clash clash = new Clash(nextId++, level, a, b, level.getGameTime());
        CLASHES.put(clash.id, clash);
        a.setState(KiBeamEntity.STATE_CLASHING);
        b.setState(KiBeamEntity.STATE_CLASHING);
        bind(a.owner(), clash.id, 1);
        bind(b.owner(), clash.id, -1);
        clash.updatePoint();
        broadcast(clash, 0);
        level.playSound(null, clash.point.x, clash.point.y, clash.point.z, ModSounds.CLASH_LOOP.get(), SoundSource.PLAYERS, 2.0f, 1.0f);
    }

    private static void bind(@Nullable LivingEntity owner, int id, int side) {
        if (owner == null) {
            return;
        }
        RuntimeState rt = CombatSystem.runtime(owner);
        if (rt != null) {
            rt.clashId = id;
            rt.clashSide = side;
            rt.clashHolding = true;
            rt.stateDirty = true;
        }
    }

    private static void unbind(@Nullable LivingEntity owner) {
        if (owner == null) {
            return;
        }
        RuntimeState rt = CombatSystem.runtime(owner);
        if (rt != null) {
            rt.clashId = -1;
            rt.clashSide = 0;
            rt.stateDirty = true;
        }
    }

    private static @Nullable Clash find(KiBeamEntity beam) {
        for (Clash clash : CLASHES.values()) {
            if (clash.a == beam || clash.b == beam) {
                return clash;
            }
        }
        return null;
    }

    public static void tick(MinecraftServer server) {
        if (CLASHES.isEmpty()) {
            return;
        }
        List<Clash> finished = new ArrayList<>();
        for (Clash clash : CLASHES.values()) {
            try {
                if (clash.tick()) {
                    finished.add(clash);
                }
            } catch (RuntimeException e) {
                com.dbtenkaichi.Tenkaichi.LOG.error("Beam clash {} failed and was resolved as a draw", clash.id, e);
                clash.resolve(0);
                finished.add(clash);
            }
        }
        for (Clash clash : finished) {
            CLASHES.remove(clash.id);
        }
    }

    public static void pulse(ServerPlayer player, long now) {
        RuntimeState rt = PlayerManager.data(player).runtime();
        Clash clash = CLASHES.get(rt.clashId);
        if (clash == null) {
            return;
        }
        applyPulse(rt, clash, now);
    }

    static void applyPulse(RuntimeState rt, Clash clash, long now) {
        if (now - rt.lastClashPulse < 6) {
            rt.clashStaggerUntil = now + 6;
            rt.lastClashPulse = now;
            return;
        }
        rt.lastClashPulse = now;
        long phase = (now - clash.start) % PULSE_PERIOD;
        if (phase <= PULSE_WINDOW || phase >= PULSE_PERIOD - PULSE_WINDOW) {
            rt.clashSurgeUntil = now + 8;
        } else {
            rt.clashStaggerUntil = now + 6;
        }
    }

    public static void setHolding(LivingEntity owner, boolean holding) {
        RuntimeState rt = CombatSystem.runtime(owner);
        if (rt != null) {
            rt.clashHolding = holding;
        }
    }

    public static void onBeamRemoved(KiBeamEntity beam) {
        Iterator<Clash> it = CLASHES.values().iterator();
        while (it.hasNext()) {
            Clash clash = it.next();
            if (clash.a == beam || clash.b == beam) {
                clash.resolve(clash.a == beam ? -1 : 1);
                it.remove();
            }
        }
    }

    public static void onLeave(ServerPlayer player) {
        RuntimeState rt = PlayerManager.data(player).runtime();
        Clash clash = CLASHES.remove(rt.clashId);
        if (clash != null) {
            clash.resolve(rt.clashSide > 0 ? -1 : 1);
        }
    }

    public static void onAttacked(LivingEntity target, long now) {
        RuntimeState rt = CombatSystem.runtime(target);
        if (rt != null && rt.clashId >= 0) {
            rt.clashStaggerUntil = Math.max(rt.clashStaggerUntil, now + 20);
        }
    }

    public static void stopAll() {
        CLASHES.clear();
    }

    private static void broadcast(Clash clash, int phase) {
        Vec3 p = clash.point;
        DestructionManager.sendNear(clash.level, p, 160, new Packets.Clash(clash.id, clash.a.ownerId(), clash.b.ownerId(), (float) clash.balance, phase,
                (int) ((clash.level.getGameTime() - clash.start) % PULSE_PERIOD), p.x, p.y, p.z));
    }

    static final class Clash {
        final int id;
        final ServerLevel level;
        final KiBeamEntity a;
        final KiBeamEntity b;
        final long start;
        double balance;
        Vec3 point = Vec3.ZERO;

        Clash(int id, ServerLevel level, KiBeamEntity a, KiBeamEntity b, long start) {
            this.id = id;
            this.level = level;
            this.a = a;
            this.b = b;
            this.start = start;
        }

        void updatePoint() {
            double t = 0.5 + 0.45 * balance;
            Vec3 oa = a.position();
            Vec3 ob = b.position();
            point = oa.add(ob.subtract(oa).scale(t));
            a.setLength((float) oa.distanceTo(point));
            b.setLength((float) ob.distanceTo(point));
            a.setDirection(point.subtract(oa));
            b.setDirection(point.subtract(ob));
        }

        boolean tick() {
            long now = level.getGameTime();
            LivingEntity ownerA = a.owner();
            LivingEntity ownerB = b.owner();
            if (!a.isAlive() || !b.isAlive() || ownerA == null || ownerB == null || !ownerA.isAlive() || !ownerB.isAlive()) {
                resolve(ownerA == null || !ownerA.isAlive() || !a.isAlive() ? -1 : 1);
                return true;
            }
            double pa = power(ownerA, a, now);
            double pb = power(ownerB, b, now);
            double delta = (pa - pb) / Math.max(1.0e-3, pa + pb);
            balance += delta * 0.035 + (level.getRandom().nextDouble() - 0.5) * 0.004;
            updatePoint();
            long elapsed = now - start;
            if (elapsed % 4 == 0) {
                broadcast(this, 1);
            }
            if (elapsed % 8 == 0) {
                CombatSystem.fx(a, CombatFxKind.SHOCKWAVE, ownerA, ownerB, point, 1.5f);
            }
            if (elapsed % 20 == 0) {
                level.playSound(null, point.x, point.y, point.z, ModSounds.CLASH_LOOP.get(), SoundSource.PLAYERS, 2.0f, 0.9f + (float) Math.abs(balance) * 0.3f);
                CombatSystem.anim(ownerA, AnimClip.CLASH_PUSH, 20, 0);
                CombatSystem.anim(ownerB, AnimClip.CLASH_PUSH, 20, 0);
            }
            if (balance >= 1.0) {
                resolve(1);
                return true;
            }
            if (balance <= -1.0) {
                resolve(-1);
                return true;
            }
            if (elapsed > MAX_TICKS) {
                resolve(0);
                return true;
            }
            return false;
        }

        double power(LivingEntity owner, KiBeamEntity beam, long now) {
            Fighter fighter = Fighter.of(owner);
            RuntimeState rt = CombatSystem.runtime(owner);
            Technique technique = beam.technique();
            double base = technique.baseDamage() * (0.5 + beam.chargeRatio());
            double stat = 1.0 + fighter.stat(StatType.KI_OUTPUT) * 0.05 + fighter.stat(StatType.SPIRIT) * 0.015;
            double form = fighter.kiMultiplier(ConfigManager.balance());
            double invest = 1.0;
            double stamina = 1.0;
            if (owner instanceof Player player) {
                PlayerData data = PlayerManager.data(player);
                double maxKi = Math.max(1.0, data.runtime().cachedMaxKi);
                if (rt != null && rt.clashHolding && data.ki() > 0) {
                    data.setKi(Math.max(0.0, data.ki() - maxKi * 0.012));
                    rt.vitalsDirty = true;
                    invest = 1.0;
                } else {
                    invest = data.ki() > 0 ? 0.55 : 0.3;
                }
                stamina = 0.6 + 0.4 * Math.max(0.0, Math.min(1.0, data.stamina() / Math.max(1.0, data.runtime().cachedMaxStamina)));
            } else if (rt != null) {
                long phase = (now - start) % PULSE_PERIOD;
                double skill = Math.min(0.9, 0.25 + fighter.stat(StatType.MARTIAL_ARTS) * 0.01);
                if (phase == 1 && owner.getRandom().nextDouble() < skill) {
                    rt.clashSurgeUntil = now + 8;
                }
                invest = 0.85;
            }
            double surge = rt != null && rt.clashSurgeUntil > now ? 1.45 : 1.0;
            double stagger = rt != null && rt.clashStaggerUntil > now ? 0.65 : 1.0;
            double health = 0.7 + 0.3 * owner.getHealth() / Math.max(1.0f, owner.getMaxHealth());
            return base * stat * form * invest * stamina * surge * stagger * health * Math.sqrt(Math.max(1.0, fighter.battlePower()) / 1000.0);
        }

        void resolve(int winner) {
            LivingEntity ownerA = a.owner();
            LivingEntity ownerB = b.owner();
            unbind(ownerA);
            unbind(ownerB);
            double strength = Math.max(a.radius(), b.radius()) * 3.0 + 1.5;
            if (winner == 0) {
                DestructionManager.explode(level, point, strength + 1.0, null, a.technique().baseDamage() + b.technique().baseDamage(), 1.0);
                a.fade();
                b.fade();
            } else {
                KiBeamEntity win = winner > 0 ? a : b;
                KiBeamEntity lose = winner > 0 ? b : a;
                lose.onClashLost();
                win.onClashWon(0.4 + Math.abs(balance) * 0.4);
                DestructionManager.explode(level, point, strength, win.owner(), win.technique().baseDamage() * 0.5, 0.6);
                LivingEntity winnerOwner = win.owner();
                if (winnerOwner instanceof ServerPlayer player) {
                    TrainingSystem.passiveGain(player, PlayerManager.data(player), "beam_clash", StatType.SPIRIT, 6.0);
                }
                LivingEntity loserOwner = lose.owner();
                if (loserOwner instanceof ServerPlayer player) {
                    TrainingSystem.passiveGain(player, PlayerManager.data(player), "beam_clash", StatType.SPIRIT, 2.5);
                }
            }
            broadcast(this, 2);
        }
    }
}
