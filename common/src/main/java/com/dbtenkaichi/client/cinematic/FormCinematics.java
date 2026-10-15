package com.dbtenkaichi.client.cinematic;

import com.dbtenkaichi.client.cinematic.CinematicScript.Camera;
import com.dbtenkaichi.client.cinematic.CinematicScript.EventKind;

final class FormCinematics {
    private FormCinematics() {
    }

    private static Camera cam(float yaw, float pitch, float distance, float height) {
        return new Camera(yaw, pitch, distance, height);
    }

    static void registerAll() {
        Cinematics.register(greatApe());
        Cinematics.register(ascended());
        Cinematics.register(ultraSuperSaiyan());
        Cinematics.register(fullPower());
        Cinematics.register(superSaiyan4());
        Cinematics.register(superSaiyanGod());
        Cinematics.register(superSaiyanBlue());
        Cinematics.register(blueEvolved());
        Cinematics.register(rose());
        Cinematics.register(ultraInstinctSign());
        Cinematics.register(ultraInstinct());
        Cinematics.register(trueUltraInstinct());
        Cinematics.register(ultraEgo());
        Cinematics.register(wrathful());
        Cinematics.register(legendary());
        Cinematics.register(rage());
        Cinematics.register(ultimate());
        Cinematics.register(beast());
        Cinematics.register(potentialUnlocked());
        Cinematics.register(maxPower());
        Cinematics.register(superNamekian());
        Cinematics.register(greatNamek());
        Cinematics.register(orangePiccolo());
        Cinematics.register(orangeGiant());
        Cinematics.register(friezaSecond());
        Cinematics.register(friezaThird());
        Cinematics.register(friezaFinal());
        Cinematics.register(friezaFullPower());
        Cinematics.register(goldenFrieza());
        Cinematics.register(blackFrieza());
        Cinematics.register(superBuu());
        Cinematics.register(buuGotenks());
        Cinematics.register(buuGohan());
        Cinematics.register(kidBuu());
        Cinematics.register(semiPerfect());
        Cinematics.register(perfect());
        Cinematics.register(superPerfect());
        Cinematics.register(fusedZamasu());
        Cinematics.register(halfCorrupted());
        Cinematics.register(thirdEye());
    }

    private static CinematicScript greatApe() {
        return CinematicScript.create("great_ape_transformation")
                .colors(0xFFF0D0, 0x05060E, 0xE8F0FF)
                .shot(0.00f, 0.16f, cam(180, -38, 4.0f, 0.4f), cam(180, -46, 3.4f, 0.4f))
                .orb(0.00f, 0.16f, 0xF4F2E8, 0.5f, 0.2f, 0.12f)
                .cut(0.16f, 0.30f, cam(0, 4, 1.1f, 0.93f), cam(0, 4, 0.95f, 0.94f))
                .cut(0.30f, 0.44f, cam(25, -6, 3.0f, 0.6f), cam(40, -10, 3.6f, 0.6f))
                .shot(0.44f, 0.80f, cam(30, -8, 4.0f, 0.5f), cam(10, -20, 12.0f, 0.5f))
                .shot(0.80f, 1.00f, cam(0, -18, 14.0f, 0.75f), cam(-10, -14, 16.0f, 0.8f))
                .darken(0.00f, 0.44f, 0.5f, 0.7f).darken(0.44f, 0.80f, 0.7f, 0.3f).darken(0.80f, 1.0f, 0.3f, 0.0f)
                .heartbeat(0.04f, 0.46f, 0.4f, 1.0f)
                .tint(0.16f, 0.30f, 0xB01010, 0.0f, 0.35f).tint(0.30f, 0.44f, 0xB01010, 0.35f, 0.1f)
                .grow(0.44f, 0.80f, 0.0f, 1.0f)
                .shake(0.30f, 0.80f, 0.4f, 1.4f).shake(0.80f, 0.92f, 1.8f, 0.4f)
                .aura(0.30f, 0.80f, 0.2f, 0.6f).aura(0.80f, 1.0f, 0.8f, 0.4f)
                .rocks(0.44f, 0.90f, 0.6f, 1.0f)
                .flash(0.80f, 0.84f, 0.0f, 0.7f).flash(0.84f, 0.90f, 0.7f, 0.0f)
                .formFrom(0.44f).revealAt(0.82f)
                .event(0.02f, EventKind.SOUND_HEARTBEAT, 0.8f).event(0.10f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.18f, EventKind.SOUND_HEARTBEAT, 1.2f)
                .event(0.26f, EventKind.SOUND_HEARTBEAT, 1.3f).event(0.34f, EventKind.SOUND_HEARTBEAT, 1.4f)
                .event(0.44f, EventKind.SOUND_RUMBLE, 1.4f).event(0.50f, EventKind.GROUND_BREAK, 1.6f)
                .event(0.62f, EventKind.GROUND_BREAK, 2.0f).event(0.62f, EventKind.SOUND_RUMBLE, 1.6f)
                .event(0.80f, EventKind.SOUND_ROAR, 1.6f).event(0.80f, EventKind.BURST, 2.4f).event(0.80f, EventKind.GROUND_BREAK, 3.0f);
    }

    private static CinematicScript ascended() {
        return CinematicScript.create("ascended_super_saiyan")
                .colors(0xFFF6C8, 0x0E1008, 0xD8F0FF)
                .cut(0.00f, 0.22f, cam(0, 2, 1.3f, 0.88f), cam(0, 2, 1.1f, 0.9f))
                .cut(0.22f, 0.42f, cam(70, -4, 2.2f, 0.65f), cam(90, -4, 2.0f, 0.65f))
                .cut(0.42f, 0.62f, cam(-60, -2, 2.0f, 0.7f), cam(-80, -2, 1.8f, 0.7f))
                .shot(0.62f, 0.82f, cam(20, -12, 5.0f, 0.5f), cam(0, -8, 6.5f, 0.5f))
                .shot(0.82f, 1.00f, cam(15, 3, 2.4f, 0.88f), cam(5, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.62f, 0.35f, 0.5f).darken(0.62f, 0.75f, 0.5f, 0.0f)
                .tint(0.22f, 0.62f, 0xFFC830, 0.0f, 0.18f)
                .shake(0.0f, 0.62f, 0.3f, 0.9f).shake(0.62f, 0.75f, 1.4f, 0.3f)
                .aura(0.0f, 0.62f, 0.6f, 1.1f).aura(0.62f, 0.82f, 1.6f, 1.1f).aura(0.82f, 1.0f, 1.0f, 0.8f)
                .rocks(0.22f, 0.70f, 0.5f, 0.9f)
                .flash(0.60f, 0.64f, 0.0f, 0.8f).flash(0.64f, 0.72f, 0.8f, 0.0f)
                .formFrom(0.62f).revealAt(0.82f)
                .event(0.00f, EventKind.SOUND_POWER, 0.8f).event(0.22f, EventKind.SOUND_RUMBLE, 1.0f)
                .event(0.42f, EventKind.SOUND_SCREAM, 0.9f).event(0.62f, EventKind.BURST, 2.0f).event(0.62f, EventKind.SOUND_BURST, 1.0f)
                .event(0.62f, EventKind.GROUND_BREAK, 1.6f).event(0.82f, EventKind.SOUND_POWER, 0.9f);
    }

    private static CinematicScript ultraSuperSaiyan() {
        return CinematicScript.create("ultra_super_saiyan")
                .colors(0xFFF2C0, 0x120C06, 0xD8F0FF)
                .shot(0.00f, 0.20f, cam(0, -10, 3.0f, 0.5f), cam(10, -14, 3.4f, 0.5f))
                .cut(0.20f, 0.40f, cam(90, -2, 2.2f, 0.62f), cam(80, -2, 2.0f, 0.62f))
                .cut(0.40f, 0.56f, cam(-90, -2, 2.2f, 0.62f), cam(-80, -2, 2.0f, 0.62f))
                .shot(0.56f, 0.84f, cam(0, -20, 4.0f, 0.4f), cam(30, -26, 7.0f, 0.4f))
                .shot(0.84f, 1.00f, cam(180, -6, 4.0f, 0.6f), cam(200, -4, 4.4f, 0.6f))
                .darken(0.0f, 0.56f, 0.45f, 0.6f).darken(0.56f, 0.84f, 0.4f, 0.0f)
                .heartbeat(0.18f, 0.58f, 0.3f, 0.9f)
                .tint(0.40f, 0.60f, 0xFFB020, 0.0f, 0.22f)
                .shake(0.0f, 0.56f, 0.6f, 1.6f).shake(0.56f, 0.84f, 2.0f, 0.6f)
                .aura(0.0f, 0.56f, 0.8f, 1.4f).aura(0.56f, 1.0f, 1.8f, 1.2f)
                .rocks(0.10f, 0.90f, 0.8f, 1.0f)
                .flash(0.56f, 0.60f, 0.0f, 0.9f).flash(0.60f, 0.70f, 0.9f, 0.0f)
                .formFrom(0.56f).revealAt(0.84f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.2f).event(0.10f, EventKind.GROUND_BREAK, 1.2f)
                .event(0.20f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.30f, EventKind.SOUND_HEARTBEAT, 1.2f).event(0.40f, EventKind.SOUND_HEARTBEAT, 1.4f)
                .event(0.48f, EventKind.SOUND_SCREAM, 1.2f).event(0.56f, EventKind.BURST, 2.6f).event(0.56f, EventKind.GROUND_BREAK, 2.8f)
                .event(0.56f, EventKind.SOUND_BURST, 1.4f).event(0.84f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript fullPower() {
        return CinematicScript.create("full_power_super_saiyan")
                .colors(0xFFF8D8, 0x10140E, 0xD8F0FF)
                .shot(0.00f, 0.30f, cam(40, 2, 4.0f, 0.6f), cam(20, 2, 3.2f, 0.65f))
                .cut(0.30f, 0.55f, cam(0, 4, 1.2f, 0.92f), cam(0, 2, 1.0f, 0.93f))
                .shot(0.55f, 0.80f, cam(160, -4, 4.0f, 0.55f), cam(200, -4, 4.0f, 0.55f))
                .shot(0.80f, 1.00f, cam(10, 2, 2.6f, 0.85f), cam(0, 2, 2.0f, 0.88f))
                .darken(0.0f, 0.30f, 0.0f, 0.35f).darken(0.30f, 0.60f, 0.35f, 0.0f)
                .tint(0.30f, 0.55f, 0xFFE070, 0.0f, 0.12f).tint(0.55f, 0.70f, 0xFFE070, 0.12f, 0.0f)
                .swirl(0.20f, 0.70f, 0xFFE070, 0.3f, 0.6f)
                .aura(0.0f, 0.30f, 0.9f, 0.5f).aura(0.30f, 0.60f, 0.5f, 0.3f).aura(0.60f, 1.0f, 0.4f, 0.32f)
                .flash(0.52f, 0.56f, 0.0f, 0.4f).flash(0.56f, 0.62f, 0.4f, 0.0f)
                .formFrom(0.55f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_POWER, 0.6f).event(0.30f, EventKind.SOUND_CHIME, 0.8f)
                .event(0.55f, EventKind.SOUND_CHIME, 1.0f).event(0.80f, EventKind.SOUND_POWER, 0.6f);
    }

    private static CinematicScript superSaiyan4() {
        return CinematicScript.create("super_saiyan_4")
                .colors(0xFFE0C0, 0x140404, 0xFFB080)
                .cut(0.00f, 0.16f, cam(0, 2, 1.2f, 0.9f), cam(0, 2, 1.05f, 0.92f))
                .cut(0.16f, 0.40f, cam(0, 2, 1.1f, 0.9f), cam(0, 2, 1.1f, 0.9f))
                .backdrop(0.16f, 0.40f, 0x2A0402, 0.9f, 1.0f, 0.0f)
                .shot(0.40f, 0.62f, cam(150, -8, 4.0f, 0.55f), cam(210, -10, 5.0f, 0.55f))
                .cut(0.62f, 0.80f, cam(30, -14, 6.0f, 0.5f), cam(20, -10, 7.0f, 0.5f))
                .shot(0.80f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.8f, 0.9f))
                .darken(0.0f, 0.16f, 0.3f, 0.6f).darken(0.40f, 0.62f, 0.5f, 0.2f).darken(0.62f, 0.75f, 0.2f, 0.0f)
                .tint(0.40f, 0.62f, 0xC81810, 0.1f, 0.3f).tint(0.62f, 0.72f, 0xC81810, 0.3f, 0.0f)
                .heartbeat(0.0f, 0.40f, 0.5f, 1.0f)
                .shake(0.16f, 0.40f, 0.4f, 1.0f).shake(0.40f, 0.70f, 1.4f, 1.8f).shake(0.70f, 0.80f, 1.6f, 0.2f)
                .aura(0.0f, 0.40f, 0.3f, 0.8f).aura(0.40f, 0.62f, 1.2f, 1.6f).aura(0.62f, 1.0f, 1.4f, 0.9f)
                .rocks(0.40f, 0.80f, 0.8f, 1.0f)
                .flicker(0.30f, 0.40f)
                .flash(0.60f, 0.64f, 0.0f, 1.0f).flash(0.64f, 0.74f, 1.0f, 0.0f)
                .formFrom(0.62f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.10f, EventKind.SOUND_HEARTBEAT, 1.2f).event(0.20f, EventKind.SOUND_HEARTBEAT, 1.4f)
                .event(0.40f, EventKind.SOUND_ROAR, 1.2f).event(0.44f, EventKind.LIGHTNING, 1.0f).event(0.50f, EventKind.LIGHTNING, 1.2f)
                .event(0.62f, EventKind.BURST, 2.6f).event(0.62f, EventKind.SOUND_BURST, 1.4f).event(0.62f, EventKind.GROUND_BREAK, 2.6f)
                .event(0.80f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript superSaiyanGod() {
        return CinematicScript.create("super_saiyan_god")
                .colors(0xFFC8A0, 0x14060A, 0xFFB0A0)
                .shot(0.00f, 0.22f, cam(180, -30, 6.0f, 0.5f), cam(140, -40, 7.0f, 0.5f))
                .swirl(0.00f, 0.50f, 0xFF4A2A, 0.6f, 1.0f)
                .cut(0.22f, 0.45f, cam(0, 4, 1.4f, 0.9f), cam(0, 2, 1.15f, 0.92f))
                .shot(0.45f, 0.70f, cam(60, -6, 4.0f, 0.55f), cam(-20, -10, 6.0f, 0.55f))
                .shot(0.70f, 1.00f, cam(10, 2, 2.6f, 0.88f), cam(0, 2, 2.0f, 0.9f))
                .darken(0.0f, 0.45f, 0.5f, 0.7f).darken(0.45f, 0.70f, 0.7f, 0.0f)
                .tint(0.22f, 0.50f, 0xFF3A18, 0.05f, 0.32f).tint(0.50f, 0.70f, 0xFF3A18, 0.32f, 0.0f)
                .aura(0.0f, 0.45f, 0.4f, 0.9f).aura(0.45f, 0.70f, 1.3f, 0.6f).aura(0.70f, 1.0f, 0.6f, 0.4f)
                .flash(0.44f, 0.50f, 0.0f, 0.9f).flash(0.50f, 0.60f, 0.9f, 0.0f)
                .formFrom(0.46f).revealAt(0.70f)
                .event(0.00f, EventKind.SOUND_CHIME, 0.8f).event(0.08f, EventKind.SOUND_CHIME, 0.9f).event(0.16f, EventKind.SOUND_CHIME, 1.0f)
                .event(0.22f, EventKind.SOUND_POWER, 0.9f).event(0.46f, EventKind.BURST, 1.8f).event(0.46f, EventKind.SOUND_BURST, 1.0f)
                .event(0.70f, EventKind.SOUND_CHIME, 1.2f);
    }

    private static CinematicScript superSaiyanBlue() {
        return CinematicScript.create("super_saiyan_blue")
                .colors(0xD8F4FF, 0x040A14, 0xA8E0FF)
                .cut(0.00f, 0.25f, cam(0, 2, 1.3f, 0.9f), cam(0, 2, 1.1f, 0.92f))
                .shot(0.25f, 0.45f, cam(40, -6, 3.0f, 0.6f), cam(70, -6, 2.6f, 0.6f))
                .shot(0.45f, 0.75f, cam(200, -10, 5.0f, 0.5f), cam(160, -10, 6.0f, 0.5f))
                .shot(0.75f, 1.00f, cam(10, 3, 2.5f, 0.88f), cam(0, 2, 2.0f, 0.9f))
                .darken(0.0f, 0.45f, 0.4f, 0.55f).darken(0.45f, 0.60f, 0.55f, 0.0f)
                .tint(0.25f, 0.45f, 0x1E78FF, 0.0f, 0.3f).tint(0.45f, 0.60f, 0x1E78FF, 0.3f, 0.0f)
                .swirl(0.20f, 0.60f, 0x3FA8FF, 0.5f, 1.0f)
                .speedLines(0.40f, 0.55f, 0.8f, 0.0f)
                .shake(0.25f, 0.45f, 0.3f, 1.0f).shake(0.45f, 0.60f, 1.4f, 0.2f)
                .aura(0.0f, 0.45f, 0.4f, 1.2f).aura(0.45f, 0.75f, 1.6f, 1.0f).aura(0.75f, 1.0f, 0.8f, 0.6f)
                .flash(0.42f, 0.46f, 0.0f, 1.0f).flash(0.46f, 0.56f, 1.0f, 0.0f)
                .formFrom(0.45f).revealAt(0.75f)
                .event(0.00f, EventKind.SOUND_POWER, 0.8f).event(0.25f, EventKind.SOUND_CHIME, 1.0f)
                .event(0.45f, EventKind.BURST, 2.2f).event(0.45f, EventKind.SOUND_BURST, 1.2f).event(0.45f, EventKind.GROUND_BREAK, 1.6f)
                .event(0.75f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript blueEvolved() {
        return CinematicScript.create("super_saiyan_blue_evolved")
                .colors(0xC0D8FF, 0x020610, 0x9FC0FF)
                .cut(0.00f, 0.18f, cam(0, 2, 1.2f, 0.9f), cam(0, 2, 1.05f, 0.92f))
                .cut(0.18f, 0.36f, cam(0, -60, 2.4f, 2.0f), cam(40, -64, 2.4f, 2.0f))
                .cut(0.36f, 0.54f, cam(-30, 6, 1.5f, 0.92f), cam(-20, 4, 1.3f, 0.92f))
                .shot(0.54f, 0.80f, cam(170, -8, 5.0f, 0.5f), cam(200, -6, 6.0f, 0.5f))
                .shot(0.80f, 1.00f, cam(15, 2, 2.4f, 0.88f), cam(5, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.54f, 0.55f, 0.75f).darken(0.54f, 0.70f, 0.6f, 0.0f)
                .tint(0.18f, 0.54f, 0x0A2A90, 0.1f, 0.35f).tint(0.54f, 0.66f, 0x0A2A90, 0.35f, 0.0f)
                .heartbeat(0.36f, 0.54f, 0.4f, 0.9f)
                .shake(0.18f, 0.54f, 0.6f, 1.6f).shake(0.54f, 0.80f, 2.0f, 0.3f)
                .aura(0.0f, 0.54f, 0.8f, 1.4f).aura(0.54f, 0.80f, 1.8f, 1.2f).aura(0.80f, 1.0f, 1.0f, 0.8f)
                .rocks(0.36f, 0.80f, 0.8f, 1.0f)
                .flash(0.52f, 0.56f, 0.0f, 1.0f).flash(0.56f, 0.66f, 1.0f, 0.0f)
                .formFrom(0.54f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.0f).event(0.18f, EventKind.LIGHTNING, 1.2f).event(0.18f, EventKind.SOUND_THUNDER, 1.0f)
                .event(0.28f, EventKind.LIGHTNING, 1.0f).event(0.36f, EventKind.SOUND_SCREAM, 1.3f)
                .event(0.54f, EventKind.BURST, 2.8f).event(0.54f, EventKind.SOUND_BURST, 1.4f).event(0.54f, EventKind.GROUND_BREAK, 2.6f)
                .event(0.62f, EventKind.LIGHTNING, 1.2f).event(0.80f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript rose() {
        return CinematicScript.create("super_saiyan_rose")
                .colors(0xFFD0E8, 0x12040C, 0xFFB0D8)
                .shot(0.00f, 0.25f, cam(180, -10, 5.0f, 0.5f), cam(200, -6, 4.0f, 0.55f))
                .cut(0.25f, 0.45f, cam(0, 2, 1.3f, 0.9f), cam(0, 2, 1.1f, 0.92f))
                .shot(0.45f, 0.75f, cam(-40, -12, 5.0f, 0.5f), cam(20, -12, 6.0f, 0.5f))
                .shot(0.75f, 1.00f, cam(10, 4, 2.4f, 0.88f), cam(0, 4, 1.9f, 0.9f))
                .darken(0.0f, 0.45f, 0.6f, 0.8f).darken(0.45f, 0.65f, 0.6f, 0.0f)
                .tint(0.25f, 0.45f, 0xFF3A9A, 0.05f, 0.3f).tint(0.45f, 0.62f, 0xFF3A9A, 0.3f, 0.0f)
                .swirl(0.10f, 0.60f, 0xFF5FA8, 0.4f, 1.0f)
                .aura(0.0f, 0.45f, 0.3f, 0.9f).aura(0.45f, 0.75f, 1.5f, 1.0f).aura(0.75f, 1.0f, 0.9f, 0.6f)
                .flash(0.43f, 0.47f, 0.0f, 0.9f).flash(0.47f, 0.58f, 0.9f, 0.0f)
                .formFrom(0.45f).revealAt(0.75f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 0.8f).event(0.25f, EventKind.SOUND_CHIME, 0.7f)
                .event(0.45f, EventKind.BURST, 2.2f).event(0.45f, EventKind.SOUND_BURST, 1.2f).event(0.75f, EventKind.SOUND_POWER, 0.9f);
    }

    private static CinematicScript ultraInstinctSign() {
        return CinematicScript.create("ultra_instinct_sign")
                .colors(0xF0F4FF, 0x02040C, 0xDDE8FF)
                .cut(0.00f, 0.20f, cam(0, 2, 1.1f, 0.93f), cam(0, 2, 0.95f, 0.94f))
                .shot(0.20f, 0.45f, cam(180, -8, 4.5f, 0.55f), cam(150, -6, 5.0f, 0.55f))
                .cut(0.45f, 0.62f, cam(0, 30, 3.0f, 0.4f), cam(30, 40, 4.0f, 0.4f))
                .shot(0.62f, 0.85f, cam(60, -10, 6.0f, 0.5f), cam(10, -8, 7.0f, 0.5f))
                .shot(0.85f, 1.00f, cam(8, 2, 2.0f, 0.9f), cam(0, 2, 1.6f, 0.92f))
                .darken(0.0f, 0.62f, 0.7f, 0.85f).darken(0.62f, 0.80f, 0.85f, 0.0f)
                .heartbeat(0.0f, 0.45f, 0.2f, 0.5f)
                .swirl(0.20f, 0.85f, 0xCFE3FF, 0.3f, 1.0f)
                .tint(0.45f, 0.62f, 0xB8C8FF, 0.0f, 0.2f)
                .aura(0.0f, 0.45f, 0.1f, 0.5f).aura(0.45f, 0.70f, 1.2f, 1.0f).aura(0.70f, 1.0f, 0.8f, 0.5f)
                .flash(0.60f, 0.64f, 0.0f, 0.8f).flash(0.64f, 0.74f, 0.8f, 0.0f)
                .formFrom(0.62f).revealAt(0.85f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 0.6f).event(0.15f, EventKind.SOUND_HEARTBEAT, 0.6f).event(0.30f, EventKind.SOUND_HEARTBEAT, 0.7f)
                .event(0.45f, EventKind.SOUND_CHIME, 1.0f).event(0.62f, EventKind.BURST, 1.8f).event(0.62f, EventKind.SOUND_CHIME, 1.4f)
                .event(0.85f, EventKind.SOUND_POWER, 0.6f);
    }

    private static CinematicScript ultraInstinct() {
        return CinematicScript.create("ultra_instinct")
                .colors(0xFFFFFF, 0x020410, 0xEEF4FF)
                .cut(0.00f, 0.18f, cam(0, 2, 1.2f, 0.92f), cam(0, 2, 1.0f, 0.93f))
                .shot(0.18f, 0.40f, cam(0, -60, 2.6f, 2.0f), cam(60, -64, 2.6f, 2.0f))
                .cut(0.40f, 0.58f, cam(-25, 6, 1.4f, 0.93f), cam(-15, 4, 1.2f, 0.93f))
                .shot(0.58f, 0.82f, cam(180, -8, 5.0f, 0.5f), cam(220, -6, 6.0f, 0.5f))
                .shot(0.82f, 1.00f, cam(10, 2, 2.3f, 0.9f), cam(0, 2, 1.8f, 0.92f))
                .darken(0.0f, 0.58f, 0.7f, 0.9f).darken(0.58f, 0.75f, 0.85f, 0.0f)
                .swirl(0.0f, 0.82f, 0xDDE8FF, 0.6f, 1.0f)
                .speedLines(0.56f, 0.70f, 1.0f, 0.0f)
                .tint(0.40f, 0.58f, 0xC8D8FF, 0.0f, 0.3f).tint(0.58f, 0.70f, 0xC8D8FF, 0.3f, 0.0f)
                .shake(0.18f, 0.58f, 0.3f, 0.9f).shake(0.58f, 0.70f, 1.4f, 0.2f)
                .aura(0.0f, 0.58f, 0.6f, 1.4f).aura(0.58f, 0.82f, 1.8f, 1.1f).aura(0.82f, 1.0f, 1.0f, 0.7f)
                .flash(0.56f, 0.60f, 0.0f, 1.0f).flash(0.60f, 0.70f, 1.0f, 0.0f)
                .formFrom(0.58f).revealAt(0.82f)
                .event(0.00f, EventKind.SOUND_CHIME, 1.0f).event(0.18f, EventKind.SOUND_RUMBLE, 0.8f)
                .event(0.40f, EventKind.SOUND_SCREAM, 1.0f).event(0.58f, EventKind.BURST, 2.4f).event(0.58f, EventKind.SOUND_BURST, 1.2f)
                .event(0.58f, EventKind.SOUND_CHIME, 1.6f).event(0.82f, EventKind.SOUND_POWER, 0.8f);
    }

    private static CinematicScript trueUltraInstinct() {
        return CinematicScript.create("true_ultra_instinct")
                .colors(0xFFF4F0, 0x06040C, 0xEEF4FF)
                .cut(0.00f, 0.20f, cam(0, 2, 1.2f, 0.92f), cam(0, 2, 1.05f, 0.93f))
                .cut(0.20f, 0.40f, cam(40, -4, 2.4f, 0.65f), cam(60, -4, 2.2f, 0.65f))
                .shot(0.40f, 0.60f, cam(0, -20, 4.0f, 0.4f), cam(-30, -24, 5.0f, 0.4f))
                .shot(0.60f, 0.85f, cam(170, -8, 5.5f, 0.5f), cam(210, -6, 6.0f, 0.5f))
                .shot(0.85f, 1.00f, cam(10, 2, 2.2f, 0.9f), cam(0, 2, 1.8f, 0.92f))
                .darken(0.0f, 0.60f, 0.6f, 0.8f).darken(0.60f, 0.78f, 0.7f, 0.0f)
                .heartbeat(0.20f, 0.60f, 0.4f, 1.0f)
                .swirl(0.10f, 0.85f, 0xC8D8FF, 0.5f, 1.0f)
                .speedLines(0.58f, 0.72f, 1.0f, 0.0f)
                .tint(0.40f, 0.60f, 0xFFE8E0, 0.0f, 0.25f)
                .shake(0.20f, 0.60f, 0.5f, 1.4f).shake(0.60f, 0.72f, 1.8f, 0.3f)
                .aura(0.0f, 0.60f, 0.6f, 1.4f).aura(0.60f, 0.85f, 1.9f, 1.2f).aura(0.85f, 1.0f, 1.0f, 0.8f)
                .rocks(0.40f, 0.80f, 0.5f, 0.9f)
                .flash(0.58f, 0.62f, 0.0f, 1.0f).flash(0.62f, 0.72f, 1.0f, 0.0f)
                .formFrom(0.60f).revealAt(0.85f)
                .event(0.00f, EventKind.SOUND_CHIME, 0.9f).event(0.20f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.30f, EventKind.SOUND_HEARTBEAT, 1.2f)
                .event(0.40f, EventKind.SOUND_SCREAM, 1.2f).event(0.60f, EventKind.BURST, 2.6f).event(0.60f, EventKind.SOUND_BURST, 1.3f)
                .event(0.60f, EventKind.GROUND_BREAK, 2.0f).event(0.85f, EventKind.SOUND_POWER, 0.9f);
    }

    private static CinematicScript ultraEgo() {
        return CinematicScript.create("ultra_ego")
                .colors(0xF0C8FF, 0x0C0212, 0xE0A0FF)
                .cut(0.00f, 0.16f, cam(0, 2, 1.2f, 0.92f), cam(0, 2, 1.0f, 0.93f))
                .shot(0.16f, 0.40f, cam(180, -14, 5.0f, 0.5f), cam(140, -18, 6.0f, 0.5f))
                .cut(0.40f, 0.58f, cam(20, 8, 1.5f, 0.92f), cam(10, 6, 1.3f, 0.92f))
                .shot(0.58f, 0.84f, cam(-40, -10, 5.5f, 0.5f), cam(10, -8, 7.0f, 0.5f))
                .shot(0.84f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.58f, 0.65f, 0.85f).darken(0.58f, 0.76f, 0.7f, 0.0f)
                .heartbeat(0.16f, 0.58f, 0.5f, 1.0f)
                .tint(0.16f, 0.58f, 0x7A1AB8, 0.1f, 0.4f).tint(0.58f, 0.72f, 0x7A1AB8, 0.4f, 0.0f)
                .swirl(0.30f, 0.84f, 0xA040FF, 0.6f, 1.0f)
                .shake(0.16f, 0.58f, 0.6f, 1.6f).shake(0.58f, 0.84f, 2.0f, 0.4f)
                .aura(0.0f, 0.58f, 0.5f, 1.3f).aura(0.58f, 0.84f, 1.8f, 1.2f).aura(0.84f, 1.0f, 1.0f, 0.8f)
                .rocks(0.30f, 0.84f, 0.7f, 1.0f)
                .flash(0.56f, 0.60f, 0.0f, 1.0f).flash(0.60f, 0.72f, 1.0f, 0.0f)
                .formFrom(0.58f).revealAt(0.84f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.0f).event(0.16f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.26f, EventKind.SOUND_HEARTBEAT, 1.2f)
                .event(0.36f, EventKind.SOUND_HEARTBEAT, 1.4f).event(0.46f, EventKind.SOUND_ROAR, 1.0f)
                .event(0.58f, EventKind.BURST, 2.8f).event(0.58f, EventKind.SOUND_BURST, 1.4f).event(0.58f, EventKind.GROUND_BREAK, 2.6f)
                .event(0.84f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript wrathful() {
        return CinematicScript.create("wrathful")
                .colors(0xD8FFD8, 0x041004, 0xB0FFB0)
                .cut(0.00f, 0.20f, cam(0, -4, 1.2f, 0.92f), cam(0, -4, 1.05f, 0.93f))
                .shot(0.20f, 0.45f, cam(60, -10, 4.0f, 0.5f), cam(30, -14, 5.0f, 0.5f))
                .cut(0.45f, 0.65f, cam(0, 20, 1.6f, 0.7f), cam(0, 26, 2.0f, 0.7f))
                .shot(0.65f, 1.00f, cam(180, -10, 6.0f, 0.5f), cam(200, -16, 9.0f, 0.5f))
                .darken(0.0f, 0.45f, 0.5f, 0.75f).darken(0.45f, 0.70f, 0.6f, 0.0f)
                .heartbeat(0.0f, 0.45f, 0.6f, 1.0f)
                .tint(0.20f, 0.65f, 0x18A020, 0.05f, 0.3f).tint(0.65f, 0.80f, 0x18A020, 0.3f, 0.0f)
                .shake(0.20f, 0.65f, 0.6f, 1.8f).shake(0.65f, 0.90f, 2.2f, 0.4f)
                .aura(0.0f, 0.45f, 0.6f, 1.2f).aura(0.45f, 1.0f, 1.8f, 1.2f)
                .rocks(0.20f, 0.90f, 0.8f, 1.0f)
                .flash(0.63f, 0.67f, 0.0f, 0.9f).flash(0.67f, 0.76f, 0.9f, 0.0f)
                .formFrom(0.65f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 1.2f).event(0.12f, EventKind.SOUND_HEARTBEAT, 1.4f).event(0.24f, EventKind.SOUND_HEARTBEAT, 1.6f)
                .event(0.45f, EventKind.SOUND_ROAR, 1.4f).event(0.65f, EventKind.BURST, 2.6f).event(0.65f, EventKind.SOUND_BURST, 1.4f)
                .event(0.65f, EventKind.GROUND_BREAK, 3.0f);
    }

    private static CinematicScript legendary() {
        return CinematicScript.create("legendary_super_saiyan")
                .colors(0xE8FFD0, 0x061006, 0xC8FFA0)
                .cut(0.00f, 0.16f, cam(0, 2, 1.2f, 0.9f), cam(0, 2, 1.05f, 0.92f))
                .shot(0.16f, 0.40f, cam(0, -60, 2.6f, 2.0f), cam(70, -64, 2.6f, 2.0f))
                .cut(0.40f, 0.60f, cam(-20, 8, 1.6f, 0.9f), cam(-10, 6, 1.4f, 0.9f))
                .shot(0.60f, 0.85f, cam(160, -12, 6.0f, 0.5f), cam(200, -18, 10.0f, 0.5f))
                .shot(0.85f, 1.00f, cam(10, -6, 4.0f, 0.75f), cam(0, -4, 3.4f, 0.8f))
                .darken(0.0f, 0.60f, 0.55f, 0.75f).darken(0.60f, 0.80f, 0.6f, 0.0f)
                .cloudRush(0.16f, 0.40f, 0.6f, 1.0f).cloudRush(0.40f, 0.60f, 1.0f, 0.0f)
                .tint(0.40f, 0.60f, 0x40FF20, 0.05f, 0.28f).tint(0.60f, 0.72f, 0x40FF20, 0.28f, 0.0f)
                .grow(0.60f, 0.85f, 0.0f, 1.0f)
                .shake(0.16f, 0.60f, 0.8f, 1.8f).shake(0.60f, 0.85f, 2.4f, 0.6f)
                .aura(0.0f, 0.60f, 0.8f, 1.6f).aura(0.60f, 1.0f, 2.0f, 1.3f)
                .rocks(0.16f, 0.90f, 0.9f, 1.0f)
                .flash(0.58f, 0.62f, 0.0f, 1.0f).flash(0.62f, 0.74f, 1.0f, 0.0f)
                .formFrom(0.60f).revealAt(0.85f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.4f).event(0.16f, EventKind.LIGHTNING, 1.4f).event(0.16f, EventKind.SOUND_THUNDER, 1.2f)
                .event(0.28f, EventKind.LIGHTNING, 1.6f).event(0.40f, EventKind.SOUND_ROAR, 1.6f)
                .event(0.60f, EventKind.BURST, 3.0f).event(0.60f, EventKind.SOUND_BURST, 1.6f).event(0.60f, EventKind.GROUND_BREAK, 3.4f)
                .event(0.70f, EventKind.LIGHTNING, 1.4f).event(0.85f, EventKind.SOUND_ROAR, 1.0f);
    }

    private static CinematicScript rage() {
        return CinematicScript.create("super_saiyan_rage")
                .colors(0xE8F4FF, 0x060810, 0xC8E8FF)
                .cut(0.00f, 0.16f, cam(0, 2, 1.2f, 0.92f), cam(0, 2, 1.05f, 0.93f))
                .cut(0.16f, 0.36f, cam(0, 2, 1.1f, 0.92f), cam(0, 2, 1.1f, 0.92f))
                .backdrop(0.16f, 0.36f, 0x000000, 0.95f, 1.0f, 0.0f)
                .streak(0.20f, 0.36f)
                .shot(0.36f, 0.62f, cam(170, -10, 4.5f, 0.5f), cam(210, -8, 5.5f, 0.5f))
                .shot(0.62f, 0.85f, cam(40, -14, 6.0f, 0.5f), cam(20, -10, 7.0f, 0.5f))
                .shot(0.85f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.36f, 0.62f, 0.5f, 0.2f).darken(0.62f, 0.72f, 0.2f, 0.0f)
                .tint(0.36f, 0.62f, 0x5AA8FF, 0.1f, 0.25f).tint(0.62f, 0.72f, 0x5AA8FF, 0.25f, 0.0f)
                .shake(0.36f, 0.62f, 0.8f, 1.6f).shake(0.62f, 0.85f, 1.8f, 0.4f)
                .aura(0.0f, 0.36f, 0.4f, 0.8f).aura(0.36f, 0.85f, 1.6f, 1.2f).aura(0.85f, 1.0f, 1.0f, 0.8f)
                .rocks(0.36f, 0.85f, 0.7f, 1.0f)
                .flash(0.60f, 0.64f, 0.0f, 1.0f).flash(0.64f, 0.74f, 1.0f, 0.0f)
                .formFrom(0.62f).revealAt(0.85f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.0f).event(0.20f, EventKind.SOUND_THUNDER, 1.1f)
                .event(0.36f, EventKind.SOUND_SCREAM, 1.3f).event(0.40f, EventKind.LIGHTNING, 1.2f).event(0.50f, EventKind.LIGHTNING, 1.0f)
                .event(0.62f, EventKind.BURST, 2.6f).event(0.62f, EventKind.SOUND_BURST, 1.3f).event(0.62f, EventKind.GROUND_BREAK, 2.4f)
                .event(0.85f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript ultimate() {
        return CinematicScript.create("ultimate")
                .colors(0xFFFFFF, 0x0A0C14, 0xF0F4FF)
                .shot(0.00f, 0.30f, cam(30, -4, 4.0f, 0.55f), cam(10, -2, 3.0f, 0.6f))
                .cut(0.30f, 0.55f, cam(0, 6, 1.3f, 0.9f), cam(0, 4, 1.1f, 0.92f))
                .shot(0.55f, 0.80f, cam(180, -8, 4.0f, 0.55f), cam(150, -6, 5.0f, 0.55f))
                .shot(0.80f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.55f, 0.3f, 0.55f).darken(0.55f, 0.70f, 0.55f, 0.0f)
                .swirl(0.05f, 0.55f, 0xF4F4FF, 0.4f, 0.9f)
                .tint(0.30f, 0.55f, 0xFFFFFF, 0.0f, 0.2f).tint(0.55f, 0.68f, 0xFFFFFF, 0.2f, 0.0f)
                .aura(0.0f, 0.55f, 0.3f, 0.9f).aura(0.55f, 0.80f, 1.5f, 0.8f).aura(0.80f, 1.0f, 0.6f, 0.4f)
                .rocks(0.50f, 0.75f, 0.4f, 0.8f)
                .flash(0.53f, 0.57f, 0.0f, 1.0f).flash(0.57f, 0.68f, 1.0f, 0.0f)
                .formFrom(0.55f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_CHIME, 0.8f).event(0.15f, EventKind.SOUND_CHIME, 0.9f).event(0.30f, EventKind.SOUND_POWER, 0.9f)
                .event(0.55f, EventKind.BURST, 2.2f).event(0.55f, EventKind.SOUND_BURST, 1.1f).event(0.55f, EventKind.GROUND_BREAK, 1.6f)
                .event(0.80f, EventKind.SOUND_CHIME, 1.2f);
    }

    private static CinematicScript beast() {
        return CinematicScript.create("beast")
                .colors(0xFFF0F0, 0x0A0410, 0xE8D8FF)
                .cut(0.00f, 0.18f, cam(0, 2, 1.15f, 0.92f), cam(0, 2, 1.0f, 0.93f))
                .tint(0.00f, 0.18f, 0x900808, 0.0f, 0.3f)
                .shot(0.18f, 0.40f, cam(180, -10, 4.0f, 0.55f), cam(150, -10, 4.5f, 0.55f))
                .cut(0.40f, 0.58f, cam(0, 30, 2.4f, 0.6f), cam(0, 40, 3.2f, 0.6f))
                .shot(0.58f, 0.84f, cam(-30, -14, 6.0f, 0.5f), cam(30, -10, 8.0f, 0.5f))
                .shot(0.84f, 1.00f, cam(10, 2, 2.3f, 0.9f), cam(0, 2, 1.8f, 0.92f))
                .darken(0.0f, 0.58f, 0.55f, 0.8f).darken(0.58f, 0.76f, 0.7f, 0.0f)
                .heartbeat(0.0f, 0.40f, 0.5f, 1.0f)
                .swirl(0.40f, 0.84f, 0xD8C8FF, 0.5f, 1.0f)
                .speedLines(0.56f, 0.70f, 1.0f, 0.0f)
                .shake(0.18f, 0.58f, 0.6f, 1.6f).shake(0.58f, 0.84f, 2.0f, 0.4f)
                .aura(0.0f, 0.58f, 0.6f, 1.4f).aura(0.58f, 0.84f, 2.0f, 1.2f).aura(0.84f, 1.0f, 1.0f, 0.8f)
                .rocks(0.40f, 0.84f, 0.7f, 1.0f)
                .flash(0.56f, 0.60f, 0.0f, 1.0f).flash(0.60f, 0.72f, 1.0f, 0.0f)
                .formFrom(0.58f).revealAt(0.84f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.12f, EventKind.SOUND_HEARTBEAT, 1.2f).event(0.24f, EventKind.SOUND_HEARTBEAT, 1.4f)
                .event(0.40f, EventKind.SOUND_SCREAM, 1.4f).event(0.46f, EventKind.LIGHTNING, 1.2f)
                .event(0.58f, EventKind.BURST, 3.0f).event(0.58f, EventKind.SOUND_BURST, 1.5f).event(0.58f, EventKind.GROUND_BREAK, 3.0f)
                .event(0.84f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript potentialUnlocked() {
        return CinematicScript.create("potential_unlocked")
                .colors(0xFFFFFF, 0x0A0E0A, 0xF0FFF0)
                .shot(0.00f, 0.35f, cam(20, 6, 1.6f, 0.92f), cam(0, 10, 1.3f, 0.95f))
                .shot(0.35f, 0.65f, cam(150, -6, 3.6f, 0.6f), cam(200, -6, 3.6f, 0.6f))
                .shot(0.65f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.35f, 0.2f, 0.45f).darken(0.35f, 0.60f, 0.45f, 0.0f)
                .swirl(0.10f, 0.55f, 0xF6FFF0, 0.3f, 0.7f)
                .tint(0.30f, 0.45f, 0xFFFFFF, 0.0f, 0.25f).tint(0.45f, 0.60f, 0xFFFFFF, 0.25f, 0.0f)
                .aura(0.0f, 0.35f, 0.2f, 0.7f).aura(0.35f, 0.65f, 1.1f, 0.6f).aura(0.65f, 1.0f, 0.5f, 0.35f)
                .flash(0.42f, 0.46f, 0.0f, 0.7f).flash(0.46f, 0.56f, 0.7f, 0.0f)
                .formFrom(0.45f).revealAt(0.65f)
                .event(0.00f, EventKind.SOUND_CHIME, 0.7f).event(0.20f, EventKind.SOUND_CHIME, 0.9f)
                .event(0.45f, EventKind.BURST, 1.4f).event(0.45f, EventKind.SOUND_CHIME, 1.2f).event(0.65f, EventKind.SOUND_POWER, 0.7f);
    }

    private static CinematicScript maxPower() {
        return CinematicScript.create("max_power")
                .colors(0xFFF2D0, 0x100C08, 0xFFE8C0)
                .cut(0.00f, 0.20f, cam(0, 2, 1.3f, 0.88f), cam(0, 2, 1.15f, 0.9f))
                .cut(0.20f, 0.40f, cam(80, -2, 2.0f, 0.62f), cam(90, -2, 1.8f, 0.62f))
                .cut(0.40f, 0.55f, cam(-80, -2, 2.0f, 0.62f), cam(-90, -2, 1.8f, 0.62f))
                .shot(0.55f, 0.80f, cam(20, -10, 4.0f, 0.5f), cam(0, -6, 5.0f, 0.5f))
                .shot(0.80f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 2.0f, 0.9f))
                .darken(0.0f, 0.55f, 0.3f, 0.5f).darken(0.55f, 0.70f, 0.5f, 0.0f)
                .heartbeat(0.10f, 0.55f, 0.3f, 0.8f)
                .shake(0.20f, 0.55f, 0.4f, 1.2f).shake(0.55f, 0.75f, 1.4f, 0.2f)
                .aura(0.0f, 0.55f, 0.5f, 1.0f).aura(0.55f, 0.80f, 1.4f, 0.8f).aura(0.80f, 1.0f, 0.7f, 0.5f)
                .rocks(0.30f, 0.70f, 0.4f, 0.8f)
                .flash(0.53f, 0.57f, 0.0f, 0.6f).flash(0.57f, 0.66f, 0.6f, 0.0f)
                .formFrom(0.55f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_POWER, 0.7f).event(0.20f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.32f, EventKind.SOUND_HEARTBEAT, 1.2f)
                .event(0.45f, EventKind.SOUND_SCREAM, 0.8f).event(0.55f, EventKind.BURST, 1.6f).event(0.55f, EventKind.SOUND_BURST, 1.0f)
                .event(0.80f, EventKind.SOUND_POWER, 0.8f);
    }

    private static CinematicScript superNamekian() {
        return CinematicScript.create("super_namekian")
                .colors(0xF4FFF0, 0x060E06, 0xE8FFE0)
                .shot(0.00f, 0.25f, cam(30, 4, 2.0f, 0.75f), cam(10, 4, 1.6f, 0.78f))
                .cut(0.25f, 0.50f, cam(0, 2, 1.2f, 0.9f), cam(0, 2, 1.0f, 0.92f))
                .shot(0.50f, 0.78f, cam(170, -8, 4.5f, 0.55f), cam(210, -8, 5.0f, 0.55f))
                .shot(0.78f, 1.00f, cam(10, 2, 2.5f, 0.88f), cam(0, 2, 2.0f, 0.9f))
                .darken(0.0f, 0.50f, 0.4f, 0.6f).darken(0.50f, 0.68f, 0.6f, 0.0f)
                .swirl(0.10f, 0.60f, 0xE8FFE0, 0.4f, 1.0f)
                .tint(0.25f, 0.50f, 0x7AFF6A, 0.0f, 0.2f).tint(0.50f, 0.62f, 0x7AFF6A, 0.2f, 0.0f)
                .aura(0.0f, 0.50f, 0.3f, 1.0f).aura(0.50f, 0.78f, 1.5f, 0.9f).aura(0.78f, 1.0f, 0.7f, 0.5f)
                .flash(0.48f, 0.52f, 0.0f, 1.0f).flash(0.52f, 0.62f, 1.0f, 0.0f)
                .formFrom(0.50f).revealAt(0.78f)
                .event(0.00f, EventKind.SOUND_CHIME, 0.8f).event(0.25f, EventKind.SOUND_POWER, 0.9f)
                .event(0.50f, EventKind.BURST, 2.0f).event(0.50f, EventKind.SOUND_BURST, 1.0f).event(0.78f, EventKind.SOUND_POWER, 0.8f);
    }

    private static CinematicScript greatNamek() {
        return CinematicScript.create("great_namek")
                .colors(0xF0FFE8, 0x061006, 0xE0FFE0)
                .shot(0.00f, 0.20f, cam(20, -6, 3.0f, 0.6f), cam(30, -6, 3.4f, 0.6f))
                .shot(0.20f, 0.75f, cam(30, -10, 4.0f, 0.4f), cam(10, -24, 16.0f, 0.4f))
                .shot(0.75f, 1.00f, cam(0, -16, 16.0f, 0.8f), cam(-10, -12, 18.0f, 0.85f))
                .darken(0.0f, 0.20f, 0.2f, 0.4f).darken(0.20f, 0.75f, 0.4f, 0.0f)
                .grow(0.20f, 0.75f, 0.0f, 1.0f)
                .shake(0.20f, 0.75f, 0.6f, 1.6f).shake(0.75f, 0.85f, 1.6f, 0.2f)
                .aura(0.0f, 0.20f, 0.6f, 1.0f).aura(0.20f, 1.0f, 0.8f, 0.4f)
                .rocks(0.20f, 0.85f, 0.7f, 1.0f)
                .flash(0.74f, 0.78f, 0.0f, 0.5f).flash(0.78f, 0.86f, 0.5f, 0.0f)
                .formFrom(0.20f).revealAt(0.76f)
                .event(0.00f, EventKind.SOUND_POWER, 0.9f).event(0.20f, EventKind.SOUND_RUMBLE, 1.4f)
                .event(0.35f, EventKind.GROUND_BREAK, 1.6f).event(0.55f, EventKind.GROUND_BREAK, 2.2f).event(0.55f, EventKind.SOUND_RUMBLE, 1.6f)
                .event(0.76f, EventKind.SOUND_ROAR, 0.8f).event(0.76f, EventKind.GROUND_BREAK, 2.6f);
    }

    private static CinematicScript orangePiccolo() {
        return CinematicScript.create("orange_piccolo")
                .colors(0xFFE0B0, 0x100804, 0xFFC890)
                .cut(0.00f, 0.20f, cam(0, 2, 1.2f, 0.9f), cam(0, 2, 1.05f, 0.92f))
                .shot(0.20f, 0.45f, cam(160, -10, 4.0f, 0.55f), cam(200, -10, 4.5f, 0.55f))
                .cut(0.45f, 0.62f, cam(-20, 8, 1.5f, 0.9f), cam(-10, 6, 1.3f, 0.9f))
                .shot(0.62f, 0.85f, cam(40, -12, 6.0f, 0.5f), cam(10, -10, 7.0f, 0.5f))
                .shot(0.85f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.62f, 0.4f, 0.7f).darken(0.62f, 0.78f, 0.6f, 0.0f)
                .tint(0.20f, 0.62f, 0xFF7A10, 0.05f, 0.32f).tint(0.62f, 0.74f, 0xFF7A10, 0.32f, 0.0f)
                .swirl(0.20f, 0.70f, 0xFF9A3A, 0.4f, 1.0f)
                .shake(0.20f, 0.62f, 0.5f, 1.4f).shake(0.62f, 0.85f, 1.8f, 0.3f)
                .aura(0.0f, 0.62f, 0.5f, 1.3f).aura(0.62f, 0.85f, 1.8f, 1.1f).aura(0.85f, 1.0f, 1.0f, 0.7f)
                .rocks(0.30f, 0.85f, 0.6f, 1.0f)
                .flash(0.60f, 0.64f, 0.0f, 1.0f).flash(0.64f, 0.74f, 1.0f, 0.0f)
                .formFrom(0.62f).revealAt(0.85f)
                .event(0.00f, EventKind.SOUND_POWER, 0.9f).event(0.20f, EventKind.SOUND_RUMBLE, 1.2f)
                .event(0.45f, EventKind.SOUND_SCREAM, 1.2f).event(0.62f, EventKind.BURST, 2.6f).event(0.62f, EventKind.SOUND_BURST, 1.3f)
                .event(0.62f, EventKind.GROUND_BREAK, 2.4f).event(0.85f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript orangeGiant() {
        return CinematicScript.create("orange_giant")
                .colors(0xFFE0B0, 0x100804, 0xFFC890)
                .shot(0.00f, 0.15f, cam(20, -6, 3.0f, 0.6f), cam(30, -6, 3.4f, 0.6f))
                .shot(0.15f, 0.72f, cam(30, -12, 4.0f, 0.4f), cam(0, -26, 18.0f, 0.4f))
                .shot(0.72f, 1.00f, cam(0, -18, 18.0f, 0.8f), cam(-15, -14, 20.0f, 0.85f))
                .darken(0.0f, 0.15f, 0.3f, 0.5f).darken(0.15f, 0.72f, 0.5f, 0.0f)
                .tint(0.15f, 0.72f, 0xFF7A10, 0.1f, 0.2f).tint(0.72f, 0.82f, 0xFF7A10, 0.2f, 0.0f)
                .grow(0.15f, 0.72f, 0.0f, 1.0f)
                .shake(0.15f, 0.72f, 0.8f, 2.0f).shake(0.72f, 0.85f, 2.0f, 0.3f)
                .aura(0.0f, 0.15f, 0.8f, 1.2f).aura(0.15f, 1.0f, 1.0f, 0.6f)
                .rocks(0.15f, 0.90f, 0.8f, 1.0f)
                .flash(0.70f, 0.74f, 0.0f, 0.6f).flash(0.74f, 0.82f, 0.6f, 0.0f)
                .formFrom(0.15f).revealAt(0.74f)
                .event(0.00f, EventKind.SOUND_POWER, 1.0f).event(0.15f, EventKind.SOUND_RUMBLE, 1.6f)
                .event(0.30f, EventKind.GROUND_BREAK, 2.0f).event(0.50f, EventKind.GROUND_BREAK, 2.6f).event(0.50f, EventKind.SOUND_RUMBLE, 1.8f)
                .event(0.74f, EventKind.SOUND_ROAR, 1.2f).event(0.74f, EventKind.GROUND_BREAK, 3.0f);
    }

    private static CinematicScript friezaSecond() {
        return CinematicScript.create("frieza_second_form")
                .colors(0xF4E8FF, 0x0C0610, 0xE0C8FF)
                .shot(0.00f, 0.20f, cam(0, -4, 2.0f, 0.7f), cam(10, -6, 2.4f, 0.7f))
                .cut(0.20f, 0.45f, cam(60, -4, 2.6f, 0.65f), cam(80, -8, 3.4f, 0.6f))
                .shot(0.45f, 0.75f, cam(-30, -10, 3.4f, 0.55f), cam(0, -16, 6.0f, 0.5f))
                .shot(0.75f, 1.00f, cam(10, -4, 3.0f, 0.85f), cam(0, -4, 2.6f, 0.88f))
                .darken(0.0f, 0.45f, 0.4f, 0.6f).darken(0.45f, 0.72f, 0.6f, 0.0f)
                .heartbeat(0.0f, 0.45f, 0.4f, 1.0f)
                .grow(0.20f, 0.75f, 0.0f, 1.0f)
                .tint(0.20f, 0.45f, 0x6A2A9A, 0.0f, 0.2f)
                .shake(0.20f, 0.75f, 0.5f, 1.4f)
                .aura(0.0f, 0.45f, 0.4f, 0.9f).aura(0.45f, 1.0f, 1.2f, 0.5f)
                .rocks(0.30f, 0.75f, 0.5f, 0.9f)
                .flash(0.72f, 0.76f, 0.0f, 0.6f).flash(0.76f, 0.84f, 0.6f, 0.0f)
                .formFrom(0.30f).revealAt(0.75f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.12f, EventKind.SOUND_HEARTBEAT, 1.2f)
                .event(0.24f, EventKind.SOUND_SLASH, 1.2f).event(0.30f, EventKind.SOUND_RUMBLE, 1.2f).event(0.40f, EventKind.GROUND_BREAK, 1.4f)
                .event(0.75f, EventKind.SOUND_ROAR, 0.8f).event(0.75f, EventKind.BURST, 1.6f);
    }

    private static CinematicScript friezaThird() {
        return CinematicScript.create("frieza_third_form")
                .colors(0xF0E0FF, 0x08040C, 0xD8C0FF)
                .shot(0.00f, 0.25f, cam(180, -6, 3.0f, 0.6f), cam(150, -6, 3.4f, 0.6f))
                .cut(0.25f, 0.50f, cam(0, -10, 1.6f, 0.92f), cam(20, -20, 1.8f, 0.95f))
                .shot(0.50f, 0.78f, cam(-40, -8, 4.0f, 0.55f), cam(10, -6, 5.0f, 0.55f))
                .shot(0.78f, 1.00f, cam(15, -8, 2.6f, 0.9f), cam(0, -10, 2.2f, 0.92f))
                .darken(0.0f, 0.50f, 0.5f, 0.75f).darken(0.50f, 0.72f, 0.6f, 0.0f)
                .heartbeat(0.10f, 0.50f, 0.4f, 0.9f)
                .tint(0.25f, 0.50f, 0x4A1A6A, 0.0f, 0.3f).tint(0.50f, 0.62f, 0x4A1A6A, 0.3f, 0.0f)
                .shake(0.25f, 0.55f, 0.5f, 1.2f)
                .aura(0.0f, 0.50f, 0.3f, 0.8f).aura(0.50f, 1.0f, 1.1f, 0.5f)
                .flash(0.48f, 0.52f, 0.0f, 0.8f).flash(0.52f, 0.60f, 0.8f, 0.0f)
                .formFrom(0.50f).revealAt(0.78f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 0.8f).event(0.25f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.35f, EventKind.SOUND_HEARTBEAT, 1.2f)
                .event(0.50f, EventKind.BURST, 1.8f).event(0.50f, EventKind.SOUND_BURST, 1.0f).event(0.78f, EventKind.SOUND_POWER, 0.8f);
    }

    private static CinematicScript friezaFinal() {
        return CinematicScript.create("frieza_final_form")
                .colors(0xFFFFFF, 0x060408, 0xE8D8FF)
                .shot(0.00f, 0.30f, cam(30, -6, 4.0f, 0.55f), cam(0, -6, 3.0f, 0.6f))
                .cocoon(0.00f, 0.45f, 0xF4F0FF, 0.2f, 1.0f)
                .cut(0.30f, 0.50f, cam(0, 4, 2.2f, 0.7f), cam(0, 4, 1.8f, 0.75f))
                .shot(0.50f, 0.78f, cam(170, -10, 4.5f, 0.55f), cam(200, -8, 5.0f, 0.55f))
                .shot(0.78f, 1.00f, cam(10, 2, 2.0f, 0.9f), cam(0, 2, 1.6f, 0.92f))
                .darken(0.0f, 0.45f, 0.5f, 0.75f).darken(0.45f, 0.66f, 0.7f, 0.0f)
                .shake(0.20f, 0.45f, 0.4f, 1.0f).shake(0.45f, 0.55f, 1.6f, 0.2f)
                .aura(0.0f, 0.45f, 0.5f, 1.0f).aura(0.45f, 0.78f, 1.4f, 0.7f).aura(0.78f, 1.0f, 0.5f, 0.3f)
                .flash(0.44f, 0.48f, 0.0f, 1.0f).flash(0.48f, 0.58f, 1.0f, 0.0f)
                .formFrom(0.46f).revealAt(0.78f)
                .event(0.00f, EventKind.SOUND_POWER, 0.8f).event(0.20f, EventKind.SOUND_RUMBLE, 1.0f).event(0.36f, EventKind.SOUND_SLASH, 1.2f)
                .event(0.46f, EventKind.BURST, 2.4f).event(0.46f, EventKind.SOUND_BURST, 1.2f).event(0.46f, EventKind.GROUND_BREAK, 2.0f)
                .event(0.78f, EventKind.SOUND_CHIME, 0.8f);
    }

    private static CinematicScript friezaFullPower() {
        return CinematicScript.create("frieza_full_power")
                .colors(0xF0D8FF, 0x0A040E, 0xD8B0FF)
                .cut(0.00f, 0.20f, cam(0, 2, 1.2f, 0.9f), cam(0, 2, 1.05f, 0.92f))
                .cut(0.20f, 0.40f, cam(80, -2, 2.2f, 0.62f), cam(90, -2, 2.0f, 0.62f))
                .shot(0.40f, 0.70f, cam(0, -60, 2.6f, 2.0f), cam(60, -64, 2.6f, 2.0f))
                .shot(0.70f, 1.00f, cam(15, 2, 2.6f, 0.88f), cam(0, 2, 2.0f, 0.9f))
                .darken(0.0f, 0.55f, 0.5f, 0.7f).darken(0.55f, 0.75f, 0.6f, 0.0f)
                .heartbeat(0.0f, 0.40f, 0.4f, 0.9f)
                .tint(0.40f, 0.62f, 0x9A50FF, 0.05f, 0.28f).tint(0.62f, 0.74f, 0x9A50FF, 0.28f, 0.0f)
                .shake(0.20f, 0.60f, 0.6f, 1.6f).shake(0.60f, 0.80f, 1.8f, 0.3f)
                .aura(0.0f, 0.55f, 0.6f, 1.4f).aura(0.55f, 1.0f, 1.6f, 0.8f)
                .rocks(0.30f, 0.80f, 0.6f, 1.0f)
                .flash(0.58f, 0.62f, 0.0f, 1.0f).flash(0.62f, 0.72f, 1.0f, 0.0f)
                .formFrom(0.58f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.20f, EventKind.SOUND_RUMBLE, 1.2f).event(0.30f, EventKind.SOUND_SCREAM, 1.0f)
                .event(0.40f, EventKind.LIGHTNING, 1.0f).event(0.48f, EventKind.LIGHTNING, 1.2f)
                .event(0.58f, EventKind.BURST, 2.6f).event(0.58f, EventKind.SOUND_BURST, 1.3f).event(0.58f, EventKind.GROUND_BREAK, 2.4f)
                .event(0.80f, EventKind.SOUND_POWER, 0.9f);
    }

    private static CinematicScript goldenFrieza() {
        return CinematicScript.create("golden_frieza")
                .colors(0xFFF2A0, 0x0E0A02, 0xFFE070)
                .shot(0.00f, 0.25f, cam(180, -8, 4.0f, 0.55f), cam(150, -8, 4.5f, 0.55f))
                .cocoon(0.10f, 0.55f, 0xFFD43B, 0.4f, 1.0f)
                .shot(0.25f, 0.55f, cam(30, -4, 5.0f, 0.5f), cam(-30, -6, 5.5f, 0.5f))
                .cut(0.55f, 0.75f, cam(0, 4, 1.4f, 0.9f), cam(0, 2, 1.2f, 0.92f))
                .shot(0.75f, 1.00f, cam(20, -4, 3.2f, 0.8f), cam(0, -2, 2.6f, 0.85f))
                .darken(0.0f, 0.55f, 0.5f, 0.7f).darken(0.55f, 0.72f, 0.6f, 0.0f)
                .tint(0.40f, 0.60f, 0xFFC020, 0.05f, 0.35f).tint(0.60f, 0.72f, 0xFFC020, 0.35f, 0.0f)
                .shake(0.25f, 0.55f, 0.4f, 1.2f).shake(0.55f, 0.65f, 1.6f, 0.2f)
                .aura(0.0f, 0.55f, 0.4f, 1.2f).aura(0.55f, 0.80f, 1.6f, 0.8f).aura(0.80f, 1.0f, 0.6f, 0.4f)
                .flash(0.53f, 0.57f, 0.0f, 1.0f).flash(0.57f, 0.68f, 1.0f, 0.0f)
                .formFrom(0.55f).revealAt(0.75f)
                .event(0.00f, EventKind.SOUND_POWER, 0.9f).event(0.10f, EventKind.SOUND_CHIME, 0.9f).event(0.30f, EventKind.SOUND_RUMBLE, 1.0f)
                .event(0.55f, EventKind.BURST, 2.6f).event(0.55f, EventKind.SOUND_BURST, 1.3f).event(0.55f, EventKind.GROUND_BREAK, 2.2f)
                .event(0.75f, EventKind.SOUND_CHIME, 1.2f);
    }

    private static CinematicScript blackFrieza() {
        return CinematicScript.create("black_frieza")
                .colors(0xE0B0FF, 0x000000, 0xC080FF)
                .shot(0.00f, 0.35f, cam(180, -8, 6.0f, 0.5f), cam(180, -8, 5.0f, 0.5f))
                .darken(0.00f, 0.35f, 0.6f, 0.98f).darken(0.35f, 0.55f, 0.98f, 0.98f).darken(0.55f, 0.70f, 0.9f, 0.0f)
                .swirl(0.20f, 0.55f, 0x6A1A9A, 0.6f, 1.0f)
                .cut(0.35f, 0.55f, cam(0, 4, 2.6f, 0.75f), cam(0, 4, 2.2f, 0.78f))
                .cut(0.55f, 0.75f, cam(20, -12, 4.0f, 0.55f), cam(30, -10, 4.5f, 0.55f))
                .shot(0.75f, 1.00f, cam(10, -4, 2.2f, 0.9f), cam(0, -4, 1.8f, 0.92f))
                .tint(0.55f, 0.70f, 0x4A0A6A, 0.4f, 0.0f)
                .shake(0.55f, 0.70f, 2.0f, 0.2f)
                .aura(0.0f, 0.55f, 0.1f, 0.4f).aura(0.55f, 0.75f, 1.8f, 1.0f).aura(0.75f, 1.0f, 0.8f, 0.5f)
                .flash(0.54f, 0.57f, 0.0f, 1.0f).flash(0.57f, 0.66f, 1.0f, 0.0f)
                .formFrom(0.55f).revealAt(0.75f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 0.6f).event(0.20f, EventKind.SOUND_HEARTBEAT, 0.8f)
                .event(0.55f, EventKind.BURST, 3.0f).event(0.55f, EventKind.SOUND_BURST, 1.6f).event(0.55f, EventKind.GROUND_BREAK, 3.0f)
                .event(0.56f, EventKind.LIGHTNING, 1.4f).event(0.75f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript superBuu() {
        return CinematicScript.create("super_buu")
                .colors(0xFFE0F0, 0x0C040A, 0xFFB0D8)
                .shot(0.00f, 0.25f, cam(30, -8, 4.0f, 0.55f), cam(0, -8, 3.6f, 0.55f))
                .cocoon(0.05f, 0.55f, 0xE070C0, 0.4f, 1.0f)
                .shot(0.25f, 0.55f, cam(160, -14, 5.0f, 0.5f), cam(220, -14, 5.5f, 0.5f))
                .cut(0.55f, 0.75f, cam(0, 4, 1.4f, 0.9f), cam(0, 2, 1.2f, 0.92f))
                .shot(0.75f, 1.00f, cam(20, -4, 3.0f, 0.82f), cam(0, -4, 2.4f, 0.86f))
                .darken(0.0f, 0.55f, 0.4f, 0.7f).darken(0.55f, 0.72f, 0.6f, 0.0f)
                .tint(0.25f, 0.55f, 0xFF60B0, 0.05f, 0.25f).tint(0.55f, 0.68f, 0xFF60B0, 0.25f, 0.0f)
                .shake(0.20f, 0.55f, 0.4f, 1.2f)
                .aura(0.0f, 0.55f, 0.3f, 1.0f).aura(0.55f, 1.0f, 1.3f, 0.5f)
                .flash(0.53f, 0.57f, 0.0f, 0.9f).flash(0.57f, 0.66f, 0.9f, 0.0f)
                .formFrom(0.55f).revealAt(0.75f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 0.9f).event(0.20f, EventKind.SOUND_POWER, 0.9f)
                .event(0.55f, EventKind.BURST, 2.2f).event(0.55f, EventKind.SOUND_BURST, 1.2f).event(0.75f, EventKind.SOUND_ROAR, 0.6f);
    }

    private static CinematicScript buuGotenks() {
        return CinematicScript.create("super_buu_gotenks")
                .colors(0xFFF2C0, 0x0C040A, 0xFFE070)
                .shot(0.00f, 0.30f, cam(40, -8, 4.0f, 0.55f), cam(10, -6, 3.4f, 0.55f))
                .cocoon(0.00f, 0.50f, 0xFF90D0, 0.6f, 1.0f)
                .cut(0.30f, 0.52f, cam(0, 4, 1.3f, 0.9f), cam(0, 2, 1.15f, 0.92f))
                .shot(0.52f, 0.80f, cam(170, -8, 4.5f, 0.55f), cam(210, -8, 5.0f, 0.55f))
                .shot(0.80f, 1.00f, cam(15, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.50f, 0.4f, 0.65f).darken(0.50f, 0.68f, 0.6f, 0.0f)
                .tint(0.30f, 0.50f, 0xFFD43B, 0.0f, 0.25f).tint(0.50f, 0.62f, 0xFFD43B, 0.25f, 0.0f)
                .shake(0.20f, 0.52f, 0.4f, 1.2f).shake(0.52f, 0.62f, 1.4f, 0.2f)
                .aura(0.0f, 0.52f, 0.4f, 1.1f).aura(0.52f, 0.80f, 1.5f, 0.8f).aura(0.80f, 1.0f, 0.7f, 0.5f)
                .flash(0.50f, 0.54f, 0.0f, 1.0f).flash(0.54f, 0.64f, 1.0f, 0.0f)
                .formFrom(0.52f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.0f).event(0.30f, EventKind.SOUND_HEARTBEAT, 1.0f)
                .event(0.52f, EventKind.BURST, 2.4f).event(0.52f, EventKind.SOUND_BURST, 1.2f).event(0.80f, EventKind.SOUND_POWER, 0.9f);
    }

    private static CinematicScript buuGohan() {
        return CinematicScript.create("super_buu_gohan")
                .colors(0xFFFFFF, 0x0A0408, 0xFFE0F0)
                .shot(0.00f, 0.30f, cam(-40, -8, 4.0f, 0.55f), cam(-10, -6, 3.4f, 0.55f))
                .cocoon(0.00f, 0.50f, 0xFF90D0, 0.6f, 1.0f)
                .cut(0.30f, 0.52f, cam(0, 4, 1.3f, 0.9f), cam(0, 2, 1.15f, 0.92f))
                .shot(0.52f, 0.80f, cam(190, -8, 4.5f, 0.55f), cam(150, -8, 5.0f, 0.55f))
                .shot(0.80f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.50f, 0.5f, 0.75f).darken(0.50f, 0.68f, 0.7f, 0.0f)
                .tint(0.30f, 0.50f, 0xFFFFFF, 0.0f, 0.3f).tint(0.50f, 0.62f, 0xFFFFFF, 0.3f, 0.0f)
                .shake(0.20f, 0.52f, 0.5f, 1.4f).shake(0.52f, 0.62f, 1.8f, 0.2f)
                .aura(0.0f, 0.52f, 0.5f, 1.3f).aura(0.52f, 0.80f, 1.7f, 1.0f).aura(0.80f, 1.0f, 0.8f, 0.6f)
                .rocks(0.30f, 0.70f, 0.5f, 0.9f)
                .flash(0.50f, 0.54f, 0.0f, 1.0f).flash(0.54f, 0.64f, 1.0f, 0.0f)
                .formFrom(0.52f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.2f).event(0.30f, EventKind.SOUND_HEARTBEAT, 1.2f)
                .event(0.52f, EventKind.BURST, 2.8f).event(0.52f, EventKind.SOUND_BURST, 1.4f).event(0.52f, EventKind.LIGHTNING, 1.2f)
                .event(0.80f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript kidBuu() {
        return CinematicScript.create("kid_buu")
                .colors(0xFFE8F4, 0x0C0408, 0xFFC0E0)
                .shot(0.00f, 0.30f, cam(30, -10, 4.0f, 0.55f), cam(0, -10, 4.4f, 0.55f))
                .cocoon(0.00f, 0.55f, 0xFFA0D0, 0.8f, 0.4f)
                .cut(0.30f, 0.55f, cam(0, 0, 1.2f, 0.8f), cam(0, 2, 1.0f, 0.82f))
                .shot(0.55f, 0.80f, cam(170, -6, 3.4f, 0.5f), cam(200, -6, 3.6f, 0.5f))
                .shot(0.80f, 1.00f, cam(10, 4, 1.8f, 0.85f), cam(0, 4, 1.4f, 0.88f))
                .darken(0.0f, 0.55f, 0.4f, 0.6f).darken(0.55f, 0.70f, 0.6f, 0.0f)
                .tint(0.30f, 0.55f, 0xFF70B0, 0.05f, 0.25f).tint(0.55f, 0.66f, 0xFF70B0, 0.25f, 0.0f)
                .speedLines(0.55f, 0.66f, 0.8f, 0.0f)
                .shake(0.30f, 0.55f, 0.4f, 1.0f)
                .aura(0.0f, 0.55f, 0.4f, 0.9f).aura(0.55f, 1.0f, 1.2f, 0.5f)
                .flash(0.53f, 0.57f, 0.0f, 0.9f).flash(0.57f, 0.65f, 0.9f, 0.0f)
                .formFrom(0.55f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 0.8f).event(0.30f, EventKind.SOUND_POWER, 0.8f)
                .event(0.55f, EventKind.BURST, 2.0f).event(0.55f, EventKind.SOUND_BURST, 1.1f).event(0.80f, EventKind.SOUND_SCREAM, 0.7f);
    }

    private static CinematicScript semiPerfect() {
        return CinematicScript.create("semi_perfect")
                .colors(0xE8FFD0, 0x060A04, 0xD0FFB0)
                .shot(0.00f, 0.25f, cam(160, -10, 4.0f, 0.55f), cam(200, -10, 4.0f, 0.55f))
                .cocoon(0.05f, 0.50f, 0x8AE85A, 0.4f, 1.0f)
                .shot(0.25f, 0.55f, cam(40, -6, 3.0f, 0.6f), cam(10, -10, 5.0f, 0.55f))
                .shot(0.55f, 0.80f, cam(-30, -8, 5.0f, 0.55f), cam(10, -8, 6.0f, 0.55f))
                .shot(0.80f, 1.00f, cam(10, -2, 3.0f, 0.86f), cam(0, -2, 2.4f, 0.88f))
                .darken(0.0f, 0.50f, 0.4f, 0.65f).darken(0.50f, 0.66f, 0.6f, 0.0f)
                .heartbeat(0.10f, 0.50f, 0.3f, 0.8f)
                .grow(0.30f, 0.60f, 0.0f, 1.0f)
                .shake(0.25f, 0.55f, 0.4f, 1.2f)
                .aura(0.0f, 0.55f, 0.3f, 0.9f).aura(0.55f, 1.0f, 1.2f, 0.5f)
                .flash(0.50f, 0.54f, 0.0f, 0.8f).flash(0.54f, 0.62f, 0.8f, 0.0f)
                .formFrom(0.50f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_SLASH, 1.0f).event(0.15f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.30f, EventKind.SOUND_RUMBLE, 1.1f)
                .event(0.50f, EventKind.BURST, 2.0f).event(0.50f, EventKind.SOUND_BURST, 1.1f).event(0.80f, EventKind.SOUND_ROAR, 0.6f);
    }

    private static CinematicScript perfect() {
        return CinematicScript.create("perfect")
                .colors(0xFFFFE8, 0x060804, 0xE8FFD8)
                .shot(0.00f, 0.30f, cam(30, -8, 4.5f, 0.55f), cam(-10, -8, 4.0f, 0.55f))
                .cocoon(0.00f, 0.50f, 0xD8FFB8, 0.6f, 1.0f)
                .cut(0.30f, 0.50f, cam(0, 30, 4.0f, 0.5f), cam(0, 40, 5.0f, 0.5f))
                .shot(0.50f, 0.78f, cam(170, -6, 4.0f, 0.55f), cam(200, -6, 4.4f, 0.55f))
                .shot(0.78f, 1.00f, cam(15, 2, 2.2f, 0.9f), cam(0, 2, 1.8f, 0.92f))
                .darken(0.0f, 0.50f, 0.4f, 0.7f).darken(0.50f, 0.66f, 0.7f, 0.0f)
                .tint(0.30f, 0.50f, 0xF8FFE0, 0.0f, 0.3f).tint(0.50f, 0.62f, 0xF8FFE0, 0.3f, 0.0f)
                .shake(0.20f, 0.50f, 0.4f, 1.2f).shake(0.50f, 0.60f, 1.6f, 0.2f)
                .aura(0.0f, 0.50f, 0.4f, 1.1f).aura(0.50f, 0.78f, 1.5f, 0.8f).aura(0.78f, 1.0f, 0.6f, 0.4f)
                .flash(0.48f, 0.52f, 0.0f, 1.0f).flash(0.52f, 0.62f, 1.0f, 0.0f)
                .formFrom(0.50f).revealAt(0.78f)
                .event(0.00f, EventKind.SOUND_RUMBLE, 1.0f).event(0.30f, EventKind.SOUND_POWER, 1.0f)
                .event(0.50f, EventKind.BURST, 2.6f).event(0.50f, EventKind.SOUND_BURST, 1.3f).event(0.50f, EventKind.GROUND_BREAK, 2.0f)
                .event(0.78f, EventKind.SOUND_CHIME, 1.0f);
    }

    private static CinematicScript superPerfect() {
        return CinematicScript.create("super_perfect")
                .colors(0xFFF6C0, 0x080804, 0xFFE070)
                .shot(0.00f, 0.25f, cam(0, -30, 6.0f, 0.4f), cam(30, -24, 4.0f, 0.5f))
                .cocoon(0.00f, 0.35f, 0x9FFF7A, 0.8f, 0.2f)
                .cut(0.25f, 0.50f, cam(0, 4, 1.3f, 0.9f), cam(0, 2, 1.1f, 0.92f))
                .shot(0.50f, 0.78f, cam(180, -8, 4.5f, 0.55f), cam(140, -8, 5.0f, 0.55f))
                .shot(0.78f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.50f, 0.5f, 0.7f).darken(0.50f, 0.66f, 0.6f, 0.0f)
                .swirl(0.30f, 0.78f, 0xFFD43B, 0.4f, 1.0f)
                .shake(0.25f, 0.50f, 0.5f, 1.4f).shake(0.50f, 0.62f, 1.8f, 0.3f)
                .aura(0.0f, 0.50f, 0.6f, 1.3f).aura(0.50f, 0.78f, 1.8f, 1.2f).aura(0.78f, 1.0f, 0.9f, 0.6f)
                .rocks(0.25f, 0.70f, 0.5f, 0.9f)
                .flash(0.48f, 0.52f, 0.0f, 1.0f).flash(0.52f, 0.62f, 1.0f, 0.0f)
                .formFrom(0.50f).revealAt(0.78f)
                .event(0.00f, EventKind.SOUND_CHIME, 1.0f).event(0.25f, EventKind.SOUND_RUMBLE, 1.2f).event(0.36f, EventKind.LIGHTNING, 1.0f)
                .event(0.50f, EventKind.BURST, 2.8f).event(0.50f, EventKind.SOUND_BURST, 1.4f).event(0.56f, EventKind.LIGHTNING, 1.2f)
                .event(0.78f, EventKind.SOUND_POWER, 1.0f);
    }

    private static CinematicScript fusedZamasu() {
        return CinematicScript.create("fused_zamasu")
                .colors(0xFFFFFF, 0x080410, 0xF0D0FF)
                .shot(0.00f, 0.30f, cam(90, -6, 5.0f, 0.55f), cam(60, -6, 4.0f, 0.55f))
                .swirl(0.00f, 0.50f, 0xC06AFF, 0.6f, 1.0f)
                .orb(0.10f, 0.30f, 0xFFF0A0, 0.35f, 0.4f, 0.04f)
                .orb(0.10f, 0.30f, 0xFFF0A0, 0.65f, 0.4f, 0.04f)
                .cut(0.30f, 0.50f, cam(0, 4, 2.0f, 0.75f), cam(0, 4, 1.6f, 0.8f))
                .shot(0.50f, 0.78f, cam(170, -10, 5.0f, 0.55f), cam(210, -10, 5.5f, 0.55f))
                .shot(0.78f, 1.00f, cam(10, 4, 2.4f, 0.88f), cam(0, 4, 1.9f, 0.9f))
                .darken(0.0f, 0.50f, 0.5f, 0.75f).darken(0.50f, 0.68f, 0.7f, 0.0f)
                .tint(0.30f, 0.50f, 0xFFFFFF, 0.0f, 0.4f).tint(0.50f, 0.62f, 0xFFFFFF, 0.4f, 0.0f)
                .shake(0.30f, 0.50f, 0.4f, 1.2f)
                .aura(0.0f, 0.50f, 0.3f, 1.0f).aura(0.50f, 0.78f, 1.6f, 1.0f).aura(0.78f, 1.0f, 0.8f, 0.5f)
                .flash(0.48f, 0.52f, 0.0f, 1.0f).flash(0.52f, 0.62f, 1.0f, 0.0f)
                .formFrom(0.50f).revealAt(0.78f)
                .event(0.00f, EventKind.SOUND_CHIME, 0.9f).event(0.20f, EventKind.SOUND_CHIME, 1.1f).event(0.30f, EventKind.SOUND_POWER, 1.0f)
                .event(0.50f, EventKind.BURST, 2.6f).event(0.50f, EventKind.SOUND_BURST, 1.3f).event(0.78f, EventKind.SOUND_CHIME, 1.3f);
    }

    private static CinematicScript halfCorrupted() {
        return CinematicScript.create("half_corrupted_zamasu")
                .colors(0xE0B0FF, 0x0A0210, 0xC890FF)
                .cut(0.00f, 0.20f, cam(0, 2, 1.2f, 0.9f), cam(0, 2, 1.05f, 0.92f))
                .shot(0.20f, 0.50f, cam(180, -12, 5.0f, 0.5f), cam(140, -16, 6.0f, 0.5f))
                .cut(0.50f, 0.70f, cam(-20, 6, 1.6f, 0.9f), cam(-10, 4, 1.4f, 0.9f))
                .shot(0.70f, 1.00f, cam(20, -6, 4.0f, 0.75f), cam(0, -4, 3.0f, 0.82f))
                .darken(0.0f, 0.70f, 0.6f, 0.85f).darken(0.70f, 0.85f, 0.7f, 0.0f)
                .heartbeat(0.10f, 0.70f, 0.4f, 1.0f)
                .tint(0.20f, 0.70f, 0x6A1A9A, 0.1f, 0.4f).tint(0.70f, 0.80f, 0x6A1A9A, 0.4f, 0.0f)
                .swirl(0.30f, 0.85f, 0xA050E0, 0.5f, 1.0f)
                .shake(0.20f, 0.70f, 0.5f, 1.6f).shake(0.70f, 0.85f, 1.8f, 0.3f)
                .aura(0.0f, 0.70f, 0.5f, 1.4f).aura(0.70f, 1.0f, 1.8f, 0.9f)
                .flash(0.68f, 0.72f, 0.0f, 1.0f).flash(0.72f, 0.80f, 1.0f, 0.0f)
                .formFrom(0.70f).revealAt(0.85f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.20f, EventKind.SOUND_HEARTBEAT, 1.2f).event(0.35f, EventKind.SOUND_HEARTBEAT, 1.4f)
                .event(0.50f, EventKind.SOUND_SCREAM, 1.0f).event(0.70f, EventKind.BURST, 2.8f).event(0.70f, EventKind.SOUND_BURST, 1.4f)
                .event(0.74f, EventKind.LIGHTNING, 1.2f).event(0.85f, EventKind.SOUND_ROAR, 0.6f);
    }

    private static CinematicScript thirdEye() {
        return CinematicScript.create("third_eye")
                .colors(0xFFC0C0, 0x0C0208, 0xE080FF)
                .cut(0.00f, 0.25f, cam(0, 6, 1.0f, 0.95f), cam(0, 8, 0.85f, 0.96f))
                .tint(0.00f, 0.25f, 0xC01010, 0.0f, 0.35f)
                .shot(0.25f, 0.55f, cam(160, -10, 4.0f, 0.55f), cam(200, -10, 4.5f, 0.55f))
                .shot(0.55f, 0.80f, cam(30, -14, 6.0f, 0.5f), cam(0, -10, 7.0f, 0.5f))
                .shot(0.80f, 1.00f, cam(10, 2, 2.4f, 0.88f), cam(0, 2, 1.9f, 0.9f))
                .darken(0.0f, 0.55f, 0.55f, 0.8f).darken(0.55f, 0.72f, 0.7f, 0.0f)
                .heartbeat(0.05f, 0.55f, 0.5f, 1.0f)
                .shake(0.25f, 0.55f, 0.5f, 1.4f).shake(0.55f, 0.80f, 1.6f, 0.3f)
                .aura(0.0f, 0.55f, 0.4f, 1.2f).aura(0.55f, 0.80f, 1.6f, 1.0f).aura(0.80f, 1.0f, 0.8f, 0.6f)
                .rocks(0.30f, 0.80f, 0.5f, 0.9f)
                .flash(0.53f, 0.57f, 0.0f, 1.0f).flash(0.57f, 0.66f, 1.0f, 0.0f)
                .formFrom(0.55f).revealAt(0.80f)
                .event(0.00f, EventKind.SOUND_HEARTBEAT, 1.0f).event(0.12f, EventKind.SOUND_HEARTBEAT, 1.2f).event(0.25f, EventKind.SOUND_CHIME, 0.6f)
                .event(0.40f, EventKind.LIGHTNING, 1.0f).event(0.55f, EventKind.BURST, 2.4f).event(0.55f, EventKind.SOUND_BURST, 1.2f)
                .event(0.55f, EventKind.GROUND_BREAK, 2.0f).event(0.80f, EventKind.SOUND_ROAR, 0.6f);
    }
}
