package com.dbtenkaichi.config;

public final class ClientConfig {
    public int schemaVersion = ConfigManager.CLIENT_SCHEMA;
    public String graphicsQuality = "high";
    public float cameraShake = 1.0f;
    public boolean hitstop = true;
    public String cinematics = "full";
    public float hudScale = 1.0f;
    public boolean showBattlePower = true;
    public float lockOnCameraStrength = 0.35f;
    public boolean auraInFirstPerson = true;
    public boolean speedLines = true;

    public enum Quality {
        LOW(0.25f, 4, 1),
        MEDIUM(0.5f, 8, 2),
        HIGH(0.8f, 12, 3),
        ULTRA(1.0f, 18, 4);

        public final float particleBudget;
        public final int auraFlames;
        public final int auraLayers;

        Quality(float particleBudget, int auraFlames, int auraLayers) {
            this.particleBudget = particleBudget;
            this.auraFlames = auraFlames;
            this.auraLayers = auraLayers;
        }
    }

    public Quality quality() {
        return switch (graphicsQuality == null ? "" : graphicsQuality.toLowerCase(java.util.Locale.ROOT)) {
            case "low" -> Quality.LOW;
            case "medium" -> Quality.MEDIUM;
            case "ultra" -> Quality.ULTRA;
            default -> Quality.HIGH;
        };
    }

    public boolean fullCinematics() {
        return "full".equalsIgnoreCase(cinematics);
    }

    public boolean shortCinematics() {
        return "short".equalsIgnoreCase(cinematics);
    }
}
