package com.dbtenkaichi.client.cinematic;

import com.dbtenkaichi.client.cinematic.CinematicScript.Camera;
import com.dbtenkaichi.client.cinematic.CinematicScript.EventKind;

import java.util.HashMap;
import java.util.Map;

public final class Cinematics {
    public static final String DEFAULT = "power_up";
    private static final Map<String, CinematicScript> SCRIPTS = new HashMap<>();

    static {
        register(superSaiyanAwakening());
        register(darkRage());
        register(powerUp());
    }

    private Cinematics() {
    }

    public static void register(CinematicScript script) {
        SCRIPTS.put(script.id, script);
    }

    public static CinematicScript get(String id) {
        CinematicScript script = id == null ? null : SCRIPTS.get(id);
        return script != null ? script : SCRIPTS.get(DEFAULT);
    }

    private static Camera cam(float yaw, float pitch, float distance, float height) {
        return new Camera(yaw, pitch, distance, height);
    }

    private static CinematicScript superSaiyanAwakening() {
        return CinematicScript.create("super_saiyan_awakening")
                .colors(0xFFFFFF, 0x0E1A10, 0xD8F0FF)
                .cut(0.00f, 0.14f, cam(0, 2, 1.25f, 0.9f), cam(0, 2, 1.0f, 0.92f))
                .cut(0.14f, 0.30f, cam(38, -10, 10.0f, 0.45f), cam(30, -8, 8.5f, 0.45f))
                .cut(0.30f, 0.46f, cam(0, 4, 4.4f, 0.55f), cam(0, 2, 3.6f, 0.55f))
                .cut(0.46f, 0.60f, cam(-28, 12, 1.5f, 0.93f), cam(-20, 8, 1.3f, 0.93f))
                .shot(0.60f, 0.68f, cam(0, 4, 1.1f, 0.92f), cam(0, 6, 3.8f, 0.8f))
                .shot(0.68f, 0.86f, cam(180, -6, 5.0f, 0.5f), cam(175, -4, 6.5f, 0.5f))
                .shot(0.86f, 1.00f, cam(18, 3, 2.3f, 0.9f), cam(6, 2, 1.7f, 0.92f))
                .darken(0.00f, 0.10f, 0.0f, 0.6f).darken(0.10f, 0.68f, 0.6f, 0.78f).darken(0.68f, 0.80f, 0.45f, 0.0f)
                .shake(0.00f, 0.30f, 0.15f, 0.3f).shake(0.30f, 0.60f, 0.35f, 0.7f).shake(0.60f, 0.70f, 1.2f, 1.6f).shake(0.70f, 0.86f, 0.6f, 0.2f)
                .aura(0.00f, 0.30f, 0.0f, 0.25f).aura(0.30f, 0.60f, 0.3f, 0.6f).aura(0.60f, 0.68f, 0.7f, 1.0f).aura(0.68f, 0.86f, 1.6f, 1.3f).aura(0.86f, 1.0f, 0.9f, 0.7f)
                .rocks(0.30f, 0.68f, 0.6f, 1.0f).rocks(0.68f, 0.86f, 0.6f, 0.2f)
                .flash(0.17f, 0.19f, 0.45f, 0.0f).flash(0.22f, 0.24f, 0.55f, 0.0f).flash(0.27f, 0.29f, 0.4f, 0.0f)
                .flash(0.52f, 0.54f, 0.35f, 0.0f).flash(0.62f, 0.68f, 0.0f, 1.0f).flash(0.68f, 0.76f, 1.0f, 0.0f)
                .flicker(0.46f, 0.66f)
                .formFrom(0.68f)
                .revealAt(0.86f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.0f)
                .event(0.17f, EventKind.LIGHTNING, 1.0f).event(0.17f, EventKind.SOUND_THUNDER, 1.0f)
                .event(0.22f, EventKind.LIGHTNING, 1.2f).event(0.22f, EventKind.SOUND_THUNDER, 0.9f)
                .event(0.27f, EventKind.LIGHTNING, 0.9f).event(0.27f, EventKind.SOUND_THUNDER, 1.1f)
                .event(0.31f, EventKind.GROUND_BREAK, 1.0f).event(0.31f, EventKind.SOUND_RUMBLE, 1.2f)
                .event(0.52f, EventKind.LIGHTNING, 0.7f).event(0.52f, EventKind.SOUND_THUNDER, 1.2f)
                .event(0.60f, EventKind.SOUND_SCREAM, 1.0f)
                .event(0.68f, EventKind.BURST, 2.0f).event(0.68f, EventKind.SOUND_BURST, 1.0f).event(0.68f, EventKind.GROUND_BREAK, 2.0f)
                .event(0.86f, EventKind.SOUND_POWER, 0.8f);
    }

    private static CinematicScript darkRage() {
        return CinematicScript.create("dark_rage")
                .colors(0xFF6A6A, 0x1A0610, 0xC890FF)
                .shot(0.00f, 0.35f, cam(-60, -12, 6.0f, 0.5f), cam(60, -12, 6.0f, 0.5f))
                .cut(0.35f, 0.55f, cam(0, 18, 1.4f, 0.9f), cam(0, 10, 1.2f, 0.92f))
                .shot(0.55f, 1.00f, cam(200, -15, 4.0f, 0.4f), cam(150, -6, 7.0f, 0.5f))
                .darken(0.0f, 0.2f, 0.0f, 0.6f).darken(0.2f, 0.85f, 0.6f, 0.6f).darken(0.85f, 1.0f, 0.6f, 0.0f)
                .shake(0.0f, 0.55f, 0.3f, 0.8f).shake(0.55f, 1.0f, 1.0f, 0.2f)
                .aura(0.0f, 0.55f, 0.4f, 1.0f).aura(0.55f, 1.0f, 1.5f, 1.0f)
                .rocks(0.2f, 0.8f, 0.5f, 0.8f)
                .flash(0.55f, 0.62f, 0.8f, 0.0f)
                .formFrom(0.55f)
                .revealAt(0.7f)
                .event(0.0f, EventKind.SOUND_RUMBLE, 1.0f)
                .event(0.25f, EventKind.LIGHTNING, 1.0f).event(0.25f, EventKind.SOUND_THUNDER, 0.8f)
                .event(0.40f, EventKind.SOUND_SCREAM, 0.8f)
                .event(0.55f, EventKind.BURST, 1.6f).event(0.55f, EventKind.SOUND_BURST, 0.9f).event(0.55f, EventKind.GROUND_BREAK, 1.5f);
    }

    private static CinematicScript powerUp() {
        return CinematicScript.create(DEFAULT)
                .shot(0.0f, 0.6f, cam(40, 5, 4.0f, 0.6f), cam(-20, 5, 2.8f, 0.7f))
                .shot(0.6f, 1.0f, cam(0, -5, 3.0f, 0.55f), cam(0, 0, 4.2f, 0.55f))
                .shake(0.0f, 0.6f, 0.2f, 0.6f).shake(0.6f, 1.0f, 0.8f, 0.1f)
                .aura(0.0f, 0.6f, 0.5f, 1.0f).aura(0.6f, 1.0f, 1.4f, 0.8f)
                .rocks(0.2f, 0.7f, 0.3f, 0.6f)
                .flash(0.6f, 0.66f, 0.7f, 0.0f)
                .formFrom(0.6f)
                .revealAt(0.65f)
                .event(0.0f, EventKind.SOUND_POWER, 1.0f)
                .event(0.6f, EventKind.BURST, 1.4f).event(0.6f, EventKind.SOUND_BURST, 0.8f).event(0.6f, EventKind.GROUND_BREAK, 1.0f);
    }
}
