package com.dbtenkaichi.entity.ai;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.dbtenkaichi.entity.KiBlastEntity;
import com.dbtenkaichi.entity.FighterNpc;
import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RaiderBrain {
    public enum State {
        IDLE,
        APPROACH,
        COMBO,
        REPOSITION,
        GUARD,
        EVADE,
        VOLLEY,
        RECOVER,
        CANNON,
        ENRAGE
    }

    private final FighterNpc npc;
    private final BrainHost host;
    private State state = State.IDLE;
    private long stateStart;
    private long nextDecision;
    private int comboHits;
    private int comboTarget;
    private boolean orbitClockwise;
    private long nextCannon;
    private long nextVolley;
    private long nextEvade;
    private int volleyShots;
    private boolean enraged;

    public <T extends FighterNpc & BrainHost> RaiderBrain(T npc) {
        this.npc = npc;
        this.host = npc;
    }

    public State state() {
        return state;
    }

    public boolean enraged() {
        return enraged;
    }

    private void enter(State next, long now) {
        if (state == State.CANNON && next != State.CANNON) {
            npc.fighterRuntime().skillSlot = -1;
        }
        state = next;
        stateStart = now;
        RuntimeState rt = npc.fighterRuntime();
        rt.guarding = next == State.GUARD;
        rt.charging = next == State.RECOVER;
        if (next == State.GUARD) {
            rt.guardStart = now;
        }
        if (next == State.COMBO) {
            comboHits = 0;
            comboTarget = 2 + npc.getRandom().nextInt(enraged ? 3 : 2);
        }
        if (next == State.VOLLEY) {
            volleyShots = 3 + npc.getRandom().nextInt(enraged ? 4 : 2);
        }
        if (next == State.REPOSITION) {
            orbitClockwise = npc.getRandom().nextBoolean();
        }
    }

    public void tick(ServerLevel level) {
        long now = level.getGameTime();
        RuntimeState rt = npc.fighterRuntime();
        LivingEntity target = npc.getTarget();
        if (npc.isStunned()) {
            rt.guarding = false;
            rt.charging = false;
            return;
        }
        if (target == null || !target.isAlive()) {
            if (state != State.IDLE) {
                enter(State.IDLE, now);
            }
            idle(now);
            return;
        }
        if (!enraged && host.canEnrage() && npc.getHealth() < npc.getMaxHealth() * 0.5f) {
            enraged = true;
            enter(State.ENRAGE, now);
        }
        Vec3 targetPos = target.getBoundingBox().getCenter();
        double distance = npc.distanceTo(target);
        npc.lookAtPoint(targetPos);
        if (state != State.ENRAGE && state != State.CANNON && now >= nextEvade && incomingThreat(level, target)) {
            enter(State.EVADE, now);
            nextEvade = now + 30;
        }
        switch (state) {
            case IDLE -> enter(State.APPROACH, now);
            case APPROACH -> approach(target, targetPos, distance, now);
            case COMBO -> combo(target, targetPos, distance, now);
            case REPOSITION -> reposition(targetPos, distance, now);
            case GUARD -> guard(target, distance, now);
            case EVADE -> evade(target, targetPos, now);
            case VOLLEY -> volley(level, target, targetPos, distance, now);
            case RECOVER -> recover(targetPos, distance, now);
            case CANNON -> cannon(level, target, targetPos, now);
            case ENRAGE -> enrage(level, now);
        }
    }

    private void idle(long now) {
        if (npc.isFighterFlying() && npc.onGround()) {
            npc.setFighterFlying(false);
        }
        if (npc.isFighterFlying()) {
            FighterMovement.hover(npc);
        }
    }

    private boolean incomingThreat(ServerLevel level, LivingEntity target) {
        RuntimeState targetRt = CombatSystem.runtime(target);
        AABB around = npc.getBoundingBox().inflate(24.0);
        List<KiBeamEntity> beams = level.getEntitiesOfClass(KiBeamEntity.class, around, b -> b.ownerId() == target.getId() && b.state() == KiBeamEntity.STATE_FIRING);
        for (KiBeamEntity beam : beams) {
            Vec3 toNpc = npc.getBoundingBox().getCenter().subtract(beam.position());
            if (toNpc.normalize().dot(beam.direction()) > 0.93) {
                if (npc.fighterKiRatio() > 0.5 && npc.getRandom().nextFloat() < 0.4f && now(level) >= nextCannon) {
                    enter(State.CANNON, now(level));
                    return false;
                }
                return true;
            }
        }
        List<KiBlastEntity> blasts = level.getEntitiesOfClass(KiBlastEntity.class, npc.getBoundingBox().inflate(4.0), b -> b.owner() == target);
        if (!blasts.isEmpty() && npc.getRandom().nextFloat() < 0.35f + npc.stat(com.dbtenkaichi.stats.StatType.AGILITY) * 0.003f) {
            return true;
        }
        return targetRt != null && targetRt.skillSlot >= 0 && npc.distanceTo(target) < 26 && npc.getRandom().nextFloat() < 0.05f;
    }

    private static long now(ServerLevel level) {
        return level.getGameTime();
    }

    private void approach(LivingEntity target, Vec3 targetPos, double distance, long now) {
        boolean fly = FighterMovement.needsFlight(npc, targetPos) || !target.onGround();
        if (fly != npc.isFighterFlying()) {
            npc.setFighterFlying(fly);
        }
        double speed = host.moveSpeed(enraged);
        if (fly) {
            FighterMovement.flyToward(npc, targetPos.add(0, -0.5, 0), speed, 0.25);
        } else {
            npc.getNavigation().moveTo(target, 1.2 + (enraged ? 0.3 : 0.0));
        }
        if (distance < 3.2) {
            npc.getNavigation().stop();
            enter(State.COMBO, now);
            return;
        }
        if (now > nextDecision) {
            nextDecision = now + 20;
            float roll = npc.getRandom().nextFloat();
            if (npc.fighterKiRatio() < 0.2) {
                enter(State.RECOVER, now);
            } else if (distance > 10 && distance < 34 && npc.fighterKiRatio() > 0.65 && now >= nextCannon && roll < 0.25 && CombatSystem.hasLineOfSight(npc, target)) {
                enter(State.CANNON, now);
            } else if (distance > 6 && distance < 28 && now >= nextVolley && roll < 0.45 && CombatSystem.hasLineOfSight(npc, target)) {
                enter(State.VOLLEY, now);
            }
        }
    }

    private void combo(LivingEntity target, Vec3 targetPos, double distance, long now) {
        RuntimeState rt = npc.fighterRuntime();
        if (distance > 4.2) {
            enter(State.APPROACH, now);
            return;
        }
        if (npc.isFighterFlying()) {
            FighterMovement.flyToward(npc, targetPos.subtract(npc.getLookAngle().scale(1.6)), host.moveSpeed(enraged), 0.4);
        } else {
            npc.getNavigation().moveTo(target, 1.0);
        }
        RuntimeState targetRt = CombatSystem.runtime(target);
        if (targetRt != null && targetRt.guarding && comboHits >= 1 && npc.getRandom().nextFloat() < 0.5f) {
            if (CombatSystem.heavyAttack(npc, 10, now)) {
                enter(State.REPOSITION, now);
            }
            return;
        }
        if (now >= rt.nextAttackAllowed) {
            boolean hit;
            if (comboHits >= comboTarget) {
                hit = CombatSystem.heavyAttack(npc, 6 + npc.getRandom().nextInt(12), now);
                if (hit) {
                    enter(npc.getRandom().nextFloat() < 0.5f ? State.REPOSITION : State.GUARD, now);
                }
                return;
            }
            hit = CombatSystem.lightAttack(npc, now);
            if (hit) {
                comboHits++;
                rt.nextAttackAllowed += enraged ? 3 : 6;
            }
        }
        if (targetRt != null && targetRt.lastAttack >= now - 1 && comboHits > 0 && npc.getRandom().nextFloat() < 0.18f + (enraged ? 0.1f : 0.0f)) {
            enter(State.GUARD, now);
        }
    }

    private void reposition(Vec3 targetPos, double distance, long now) {
        if (!npc.isFighterFlying()) {
            npc.setFighterFlying(true);
        }
        Vec3 point = FighterMovement.orbitPoint(npc, targetPos, 6.0, 1.5, orbitClockwise);
        FighterMovement.flyToward(npc, point, host.moveSpeed(enraged), 0.3);
        if (now - stateStart > 20 + npc.getRandom().nextInt(20)) {
            enter(npc.getRandom().nextFloat() < 0.4f && distance > 6 ? State.VOLLEY : State.APPROACH, now);
        }
    }

    private void guard(LivingEntity target, double distance, long now) {
        FighterMovement.hover(npc);
        RuntimeState targetRt = CombatSystem.runtime(target);
        boolean pressured = targetRt != null && targetRt.lastAttack >= now - 6;
        if (now - stateStart > (pressured ? 24 : 12)) {
            RuntimeState rt = npc.fighterRuntime();
            if (rt.counterUntil > now) {
                enter(State.COMBO, now);
            } else {
                enter(distance < 3.5 ? State.COMBO : State.APPROACH, now);
            }
        }
    }

    private void evade(LivingEntity target, Vec3 targetPos, long now) {
        if (!npc.isFighterFlying()) {
            npc.setFighterFlying(true);
        }
        RuntimeState rt = npc.fighterRuntime();
        if (now - stateStart == 0) {
            Vec3 away = npc.position().subtract(targetPos).multiply(1, 0, 1);
            Vec3 side = new Vec3(-away.z, 0, away.x).normalize().scale(npc.getRandom().nextBoolean() ? 1.6 : -1.6);
            npc.setDeltaMovement(side.add(0, 0.5, 0));
            rt.invulnerableUntil = now + 6;
            CombatSystem.anim(npc, AnimClip.DODGE_LEFT, 7, 0);
            CombatSystem.fx(npc, CombatFxKind.DODGE, npc, npc, npc.position().add(0, 1, 0), 1.0f);
            CombatSystem.playSound((ServerLevel) npc.level(), npc, ModSounds.DODGE.get(), 0.7f, 1.0f);
        }
        if (now - stateStart > 8) {
            enter(State.APPROACH, now);
        }
    }

    private void volley(ServerLevel level, LivingEntity target, Vec3 targetPos, double distance, long now) {
        if (!npc.isFighterFlying() && npc.getRandom().nextFloat() < 0.5f) {
            npc.setFighterFlying(true);
        }
        FighterMovement.flyToward(npc, FighterMovement.orbitPoint(npc, targetPos, Math.max(8.0, distance), 3.0, orbitClockwise), host.moveSpeed(enraged) * 0.5, 0.2);
        if ((now - stateStart) % (enraged ? 4 : 6) == 0 && volleyShots > 0) {
            Technique blast = Techniques.KI_BLAST;
            if (npc.spendFighterKi(blast.kiCost())) {
                KiBlastEntity entity = KiBlastEntity.create(level, npc, blast, host.blastColor(), target.getId(), 0.8 + (enraged ? 0.3 : 0.0));
                level.addFreshEntity(entity);
                CombatSystem.anim(npc, volleyShots % 2 == 0 ? AnimClip.KI_BLAST_LEFT : AnimClip.KI_BLAST_RIGHT, 6, 0);
                CombatSystem.playSound(level, npc, ModSounds.KI_BLAST.get(), 0.8f, 0.8f);
            }
            volleyShots--;
        }
        if (volleyShots <= 0) {
            nextVolley = now + 60;
            enter(State.APPROACH, now);
        }
    }

    private void recover(Vec3 targetPos, double distance, long now) {
        if (!npc.isFighterFlying()) {
            npc.setFighterFlying(true);
        }
        if (distance < 14) {
            Vec3 away = npc.position().subtract(targetPos).normalize();
            FighterMovement.flyToward(npc, npc.position().add(away.scale(6)).add(0, 2, 0), host.moveSpeed(enraged), 0.3);
            npc.fighterRuntime().charging = false;
        } else {
            FighterMovement.hover(npc);
            npc.fighterRuntime().charging = true;
        }
        if (npc.fighterKiRatio() > 0.7 || now - stateStart > 160 || distance < 4) {
            enter(State.APPROACH, now);
        }
    }

    private void cannon(ServerLevel level, LivingEntity target, Vec3 targetPos, long now) {
        RuntimeState rt = npc.fighterRuntime();
        FighterMovement.hover(npc);
        Technique technique = host.signatureBeam();
        long elapsed = now - stateStart;
        if (elapsed == 0) {
            rt.skillSlot = 0;
            rt.skillStart = now;
            CombatSystem.playSound(level, npc, ModSounds.BEAM_CHARGE.get(), 1.4f, 0.8f);
        }
        if (elapsed >= technique.minCharge() + 18) {
            rt.skillSlot = -1;
            double ratio = Math.min(1.0, (double) elapsed / technique.maxCharge());
            if (npc.spendFighterKi(technique.costAt(ratio) * 0.6)) {
                KiBeamEntity beam = KiBeamEntity.create(level, npc, technique, ratio);
                level.addFreshEntity(beam);
                rt.activeBeam = beam.getId();
                CombatSystem.anim(npc, AnimClip.BEAM_FIRE, technique.duration() + 10, 0);
                CombatSystem.playSound(level, npc, ModSounds.BEAM_FIRE.get(), 1.8f, 0.85f);
            }
            nextCannon = now + 200;
            enter(State.REPOSITION, now);
        }
    }

    private void enrage(ServerLevel level, long now) {
        RuntimeState rt = npc.fighterRuntime();
        FighterMovement.hover(npc);
        long elapsed = now - stateStart;
        if (elapsed == 0) {
            rt.invulnerableUntil = now + 30;
            CombatSystem.anim(npc, AnimClip.TRANSFORM, 30, 0);
            CombatSystem.fx(npc, CombatFxKind.AURA_BURST, npc, npc, npc.position(), 1.5f);
            CombatSystem.playSound(level, npc, ModSounds.TRANSFORM_SCREAM.get(), 1.6f, 0.75f);
        }
        if (elapsed >= 30) {
            host.applyRage();
            CombatSystem.fx(npc, CombatFxKind.SHOCKWAVE, npc, npc, npc.position(), 2.0f);
            for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, npc.getBoundingBox().inflate(5.0), e -> e != npc && e.isAlive())) {
                Vec3 push = other.position().subtract(npc.position()).multiply(1, 0, 1);
                if (push.lengthSqr() > 1.0e-4) {
                    CombatSystem.knockback(other, push.normalize().scale(1.1).add(0, 0.35, 0));
                }
            }
            enter(State.APPROACH, now);
        }
    }
}
