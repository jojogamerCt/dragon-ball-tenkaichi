package com.dbtenkaichi.client;

import com.dbtenkaichi.client.anim.AnimPlayer;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.HairStyle;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.Race;
import org.jspecify.annotations.Nullable;

public final class EntityVisual {
    public Race race = Race.HUMAN;
    public Appearance appearance = Appearance.defaultsFor(Race.HUMAN);
    public boolean created;
    public boolean beard;
    public String form = PlayerData.BASE_FORM;
    public int flags;
    public float kiRatio = 1.0f;
    public float mastery;
    public float aura;
    public float auraO;
    public float flight;
    public float flightO;
    public long cinematicStart;
    public int cinematicTicks;
    public boolean cinematicFull;
    public String cinematicForm = PlayerData.BASE_FORM;
    public long lastSeen;
    public final AnimPlayer anim = new AnimPlayer();

    public boolean has(int flag) {
        return (flags & flag) != 0;
    }

    public @Nullable FormDefinition formDefinition() {
        return PlayerData.BASE_FORM.equals(form) ? null : FormRegistry.get(form, true);
    }

    public HairStyle hairStyle() {
        FormDefinition def = formDefinition();
        HairStyle formStyle = def == null ? null : def.hairStyle();
        if (formStyle != null && race.hasHair()) {
            return formStyle;
        }
        return HairStyle.byOrdinal(appearance.hairStyle);
    }

    public int hairColor() {
        FormDefinition def = formDefinition();
        return def == null ? appearance.hairColor : FormDefinition.parseColor(def.visual.hairColor, appearance.hairColor);
    }

    public boolean hairGlow() {
        FormDefinition def = formDefinition();
        return def != null && def.visual.hairGlow;
    }

    public int eyeColor() {
        FormDefinition def = formDefinition();
        return def == null ? appearance.eyeColor : FormDefinition.parseColor(def.visual.eyeColor, appearance.eyeColor);
    }

    public int auraColor() {
        FormDefinition def = formDefinition();
        return def == null ? appearance.auraColor : FormDefinition.parseColor(def.visual.auraColor, appearance.auraColor);
    }

    public int auraSecondary() {
        FormDefinition def = formDefinition();
        int fallback = mix(auraColor(), 0xFFFFFF, 0.55f);
        return def == null ? fallback : FormDefinition.parseColor(def.visual.auraSecondary, fallback);
    }

    public float auraScale() {
        FormDefinition def = formDefinition();
        return def == null ? 1.0f : def.visual.auraScale;
    }

    public boolean auraLightning() {
        FormDefinition def = formDefinition();
        return def != null && def.visual.lightning;
    }

    public float auraTarget(long now) {
        float target = 0.0f;
        if (has(RuntimeState.FLAG_CHARGING)) {
            target = 1.0f;
        } else if (has(RuntimeState.FLAG_TRANSFORMING)) {
            target = 1.25f;
        } else if (has(RuntimeState.FLAG_AURA)) {
            target = 0.75f;
        } else if (!PlayerData.BASE_FORM.equals(form)) {
            target = 0.32f;
        }
        if (has(RuntimeState.FLAG_EXHAUSTED)) {
            target *= 0.4f;
        }
        return target;
    }

    public static int mix(int a, int b, float t) {
        int ar = a >> 16 & 255, ag = a >> 8 & 255, ab = a & 255;
        int br = b >> 16 & 255, bg = b >> 8 & 255, bb = b & 255;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return r << 16 | g << 8 | bl;
    }
}
