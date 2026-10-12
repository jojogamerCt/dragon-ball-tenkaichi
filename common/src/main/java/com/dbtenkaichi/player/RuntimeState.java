package com.dbtenkaichi.player;

import java.util.HashMap;
import java.util.Map;

public final class RuntimeState {
    public static final int FLAG_FLYING = 1;
    public static final int FLAG_FAST_FLIGHT = 1 << 1;
    public static final int FLAG_CHARGING = 1 << 2;
    public static final int FLAG_GUARDING = 1 << 3;
    public static final int FLAG_AURA = 1 << 4;
    public static final int FLAG_TRANSFORMING = 1 << 5;
    public static final int FLAG_STUNNED = 1 << 6;
    public static final int FLAG_COMBAT_STANCE = 1 << 7;
    public static final int FLAG_SKILL_CHARGING = 1 << 8;
    public static final int FLAG_BEAM_FIRING = 1 << 9;
    public static final int FLAG_EXHAUSTED = 1 << 10;
    public static final int FLAG_CLASHING = 1 << 11;
    public static final int FLAG_DASHING = 1 << 12;
    public static final int FLAG_TRAINING = 1 << 13;

    public boolean flying;
    public boolean fastFlight;
    public boolean charging;
    public long chargeStart;
    public boolean guarding;
    public long guardStart;
    public boolean combatStance;
    public int comboIndex;
    public long lastAttack;
    public long nextAttackAllowed;
    public long stunnedUntil;
    public long smashedUntil;
    public long poiseUntil;
    public long lastHitTaken;
    public int hitStreak;
    public long invulnerableUntil;
    public long dodgeStart;
    public long nextDodge;
    public long nextDash;
    public long dashingUntil;
    public long counterUntil;
    public long transformingUntil;
    public String pendingForm;
    public int lockedTarget = -1;
    public int skillSlot = -1;
    public long skillStart;
    public int activeBeam = -1;
    public int clashId = -1;
    public long lastCombat = -100000L;
    public long exhaustedUntil;
    public long nextKiBlast;
    public boolean blastHand;
    public double zenkaiPendingBp;
    public long zenkaiPendingSince;
    public double kiAccumulator;
    public String trainingActivity;
    public long trainingStart;
    public int trainingStreak;
    public int trainingHits;
    public int trainingScore;
    public int trainingMaster = -1;
    public long lastTrainingPress;
    public float bandMin;
    public float bandMax;
    public long secondTicker;
    public net.minecraft.world.phys.Vec3 lastPos;
    public int flightViolations;
    public long lastSonicBoom;
    public long lastLanding;
    public boolean wasFlightControlled;
    public int clashSide;
    public boolean clashHolding;
    public long lastClashPulse;
    public long clashSurgeUntil;
    public long clashStaggerUntil;
    public final Map<Integer, Double> damageDealt = new HashMap<>();
    public int actionsThisSecond;
    public long actionWindow;

    public boolean dataDirty = true;
    public boolean vitalsDirty = true;
    public boolean stateDirty = true;
    public boolean appearanceDirty = true;
    public int lastSentFlags = -1;
    public float lastSentKi = -1.0f;
    public float lastSentStamina = -1.0f;
    public long lastSentBp = -1L;
    public long cachedBp = 1L;
    public double cachedMaxKi = 100.0;
    public double cachedMaxStamina = 100.0;

    public int flags(long now) {
        int f = 0;
        if (flying) {
            f |= FLAG_FLYING;
        }
        if (flying && fastFlight) {
            f |= FLAG_FAST_FLIGHT;
        }
        if (charging) {
            f |= FLAG_CHARGING;
        }
        if (guarding) {
            f |= FLAG_GUARDING;
        }
        if (charging || transformingUntil > now || skillSlot >= 0 || (flying && fastFlight) || clashId >= 0) {
            f |= FLAG_AURA;
        }
        if (transformingUntil > now) {
            f |= FLAG_TRANSFORMING;
        }
        if (stunnedUntil > now) {
            f |= FLAG_STUNNED;
        }
        if (combatStance) {
            f |= FLAG_COMBAT_STANCE;
        }
        if (skillSlot >= 0) {
            f |= FLAG_SKILL_CHARGING;
        }
        if (activeBeam >= 0) {
            f |= FLAG_BEAM_FIRING;
        }
        if (exhaustedUntil > now) {
            f |= FLAG_EXHAUSTED;
        }
        if (clashId >= 0) {
            f |= FLAG_CLASHING;
        }
        if (dashingUntil > now) {
            f |= FLAG_DASHING;
        }
        if (trainingActivity != null) {
            f |= FLAG_TRAINING;
        }
        return f;
    }

    public boolean busy(long now) {
        return stunnedUntil > now || transformingUntil > now;
    }

    public void resetCombat() {
        flying = false;
        fastFlight = false;
        charging = false;
        guarding = false;
        comboIndex = 0;
        stunnedUntil = 0L;
        invulnerableUntil = 0L;
        transformingUntil = 0L;
        pendingForm = null;
        lockedTarget = -1;
        skillSlot = -1;
        activeBeam = -1;
        clashId = -1;
        trainingActivity = null;
        damageDealt.clear();
        stateDirty = true;
        vitalsDirty = true;
    }
}
