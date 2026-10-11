package com.dbtenkaichi.player;

import com.dbtenkaichi.race.Race;
import net.minecraft.nbt.CompoundTag;

public final class Appearance {
    public static final int PRESENTATIONS = 2;
    public static final int EYE_STYLES = 4;
    public static final int EYEBROW_STYLES = 4;
    public static final int HAIR_STYLES = HairStyle.PLAYER_SELECTABLE;
    public static final int EXPRESSIONS = 4;
    public static final int VOICES = 4;
    public static final int OUTFITS = 3;
    public static final int BODY_BUILDS = 3;

    public int presentation;
    public float height = 1.0f;
    public int build = 1;
    public int skinColor = 0xF1C27D;
    public int eyeStyle;
    public int eyeColor = 0x1B1B1B;
    public int eyebrowStyle;
    public int hairStyle;
    public int hairColor = 0x111111;
    public int expression;
    public int voice;
    public int auraColor = 0xE8F4FF;
    public int outfit;
    public int outfitPrimary = 0xF07A1A;
    public int outfitSecondary = 0x1F3FAE;
    public boolean tail = true;
    public boolean useOwnSkin;

    public Appearance copy() {
        Appearance a = new Appearance();
        a.load(save());
        return a;
    }

    public void sanitize(Race race, float minHeight, float maxHeight) {
        presentation = clamp(presentation, 0, PRESENTATIONS - 1);
        height = Math.max(minHeight, Math.min(maxHeight, Float.isFinite(height) ? height : 1.0f));
        build = clamp(build, 0, BODY_BUILDS - 1);
        eyeStyle = clamp(eyeStyle, 0, EYE_STYLES - 1);
        eyebrowStyle = clamp(eyebrowStyle, 0, EYEBROW_STYLES - 1);
        hairStyle = clamp(hairStyle, 0, HAIR_STYLES - 1);
        expression = clamp(expression, 0, EXPRESSIONS - 1);
        voice = clamp(voice, 0, VOICES - 1);
        outfit = clamp(outfit, 0, OUTFITS - 1);
        skinColor &= 0xFFFFFF;
        eyeColor &= 0xFFFFFF;
        hairColor &= 0xFFFFFF;
        auraColor &= 0xFFFFFF;
        outfitPrimary &= 0xFFFFFF;
        outfitSecondary &= 0xFFFFFF;
        if (!race.has(com.dbtenkaichi.race.RacePassive.TAIL)) {
            tail = false;
        }
        if (!race.hasHair()) {
            hairStyle = HairStyle.BALD.ordinal();
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("presentation", presentation);
        tag.putFloat("height", height);
        tag.putInt("build", build);
        tag.putInt("skin", skinColor);
        tag.putInt("eye_style", eyeStyle);
        tag.putInt("eye_color", eyeColor);
        tag.putInt("brow", eyebrowStyle);
        tag.putInt("hair_style", hairStyle);
        tag.putInt("hair_color", hairColor);
        tag.putInt("expression", expression);
        tag.putInt("voice", voice);
        tag.putInt("aura", auraColor);
        tag.putInt("outfit", outfit);
        tag.putInt("outfit_primary", outfitPrimary);
        tag.putInt("outfit_secondary", outfitSecondary);
        tag.putBoolean("tail", tail);
        tag.putBoolean("own_skin", useOwnSkin);
        return tag;
    }

    public void load(CompoundTag tag) {
        presentation = tag.getIntOr("presentation", presentation);
        height = tag.getFloatOr("height", height);
        build = tag.getIntOr("build", build);
        skinColor = tag.getIntOr("skin", skinColor);
        eyeStyle = tag.getIntOr("eye_style", eyeStyle);
        eyeColor = tag.getIntOr("eye_color", eyeColor);
        eyebrowStyle = tag.getIntOr("brow", eyebrowStyle);
        hairStyle = tag.getIntOr("hair_style", hairStyle);
        hairColor = tag.getIntOr("hair_color", hairColor);
        expression = tag.getIntOr("expression", expression);
        voice = tag.getIntOr("voice", voice);
        auraColor = tag.getIntOr("aura", auraColor);
        outfit = tag.getIntOr("outfit", outfit);
        outfitPrimary = tag.getIntOr("outfit_primary", outfitPrimary);
        outfitSecondary = tag.getIntOr("outfit_secondary", outfitSecondary);
        tail = tag.getBooleanOr("tail", tail);
        useOwnSkin = tag.getBooleanOr("own_skin", useOwnSkin);
    }

    public static Appearance defaultsFor(Race race) {
        Appearance a = new Appearance();
        switch (race) {
            case SAIYAN -> {
                a.hairStyle = HairStyle.WILD_SPIKES.ordinal();
                a.hairColor = 0x111111;
                a.auraColor = 0xF2F6FF;
                a.tail = true;
            }
            case HUMAN -> {
                a.hairStyle = HairStyle.SHORT_NEAT.ordinal();
                a.hairColor = 0x3B2416;
                a.auraColor = 0x9FD8FF;
                a.tail = false;
            }
            default -> {
                a.tail = race.tailByDefault();
            }
        }
        return a;
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }
}
