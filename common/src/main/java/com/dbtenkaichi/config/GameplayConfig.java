package com.dbtenkaichi.config;

import java.util.ArrayList;
import java.util.List;

public final class GameplayConfig {
    public static final List<String> ALL_RACES = List.of("saiyan", "half_saiyan", "human", "namekian", "frieza_race", "majin", "android", "bio_android", "kaioshin", "demon");

    public List<String> enabledRaces = new ArrayList<>(ALL_RACES);
    public boolean allowCharacterReset = false;
    public float minHeight = 0.88f;
    public float maxHeight = 1.12f;
    public long startingTrainingPoints = 150L;

    public boolean flightEnabled = true;
    public double flightKiPerSecond = 0.6;
    public double fastFlightKiPerSecond = 3.0;
    public double hoverKiPerSecond = 0.25;
    public double baseFlightSpeed = 0.45;
    public double fastFlightSpeed = 1.35;
    public double flightSpeedPerLevel = 0.004;
    public double maxFlightSpeed = 3.2;

    public double dashStaminaCost = 18.0;
    public double dodgeStaminaCost = 14.0;
    public double vanishKiCost = 20.0;
    public int dodgeInvulnerabilityTicks = 6;
    public int dodgeCooldownTicks = 14;
    public int dashCooldownTicks = 20;
    public int perfectGuardWindowTicks = 4;
    public double guardStaminaPerHit = 6.0;
    public double guardBreakThreshold = 0.0;
    public int maxLockOnDistance = 48;
    public double meleeRange = 3.6;
    public double meleeRangeLocked = 4.6;

    public boolean pvpEnabled = true;
    public boolean friendlyFire = false;
    public boolean protectSpawnArea = true;

    public String destructionMode = "real";
    public int maxExplosionRadius = 6;
    public int maxBlocksPerExplosion = 220;
    public int terrainRegenSeconds = 0;
    public List<String> destructionDimensionBlacklist = new ArrayList<>();
    public float maxBlockHardness = 50.0f;

    public int maxActionsPerSecond = 40;
    public int cinematicTicks = 70;
    public boolean allowNpcSpawning = true;
    public double npcDifficulty = 1.0;
    public double npcMultiplayerScaling = 0.35;
    public int enemySpawnWeight = 6;
}
