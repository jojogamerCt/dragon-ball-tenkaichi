package com.dbtenkaichi.story;

import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.HairStyle;
import com.dbtenkaichi.race.Race;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class StoryCharacters {
    public record Character(String id, Race race, Appearance appearance, int beardColor, double power, Technique signature,
                            int blastColor, boolean scouter, String rageForm) {
        public String translationKey() {
            return "character.dbtenkaichi." + id;
        }
    }

    private static final Map<String, Character> ALL = new LinkedHashMap<>();

    public static final Character GOHAN = add("gohan", Race.HALF_SAIYAN, 0.55, Techniques.MASENKO, 0xFFE070, false, null, -1, a -> {
        a.hairStyle = HairStyle.GOHAN.ordinal();
        a.height = 0.62f;
        a.skinColor = 0xF1C27D;
        a.outfit = 0;
        a.outfitPrimary = 0x4A2A7A;
        a.outfitSecondary = 0xD84030;
        a.auraColor = 0xFFF2C0;
    });
    public static final Character KRILLIN = add("krillin", Race.HUMAN, 0.6, Techniques.KAMEHAMEHA, 0xBFE6FF, false, null, -1, a -> {
        a.hairStyle = HairStyle.BALD.ordinal();
        a.height = 0.84f;
        a.outfit = 0;
        a.outfitPrimary = 0xF07A1A;
        a.outfitSecondary = 0x1F3FAE;
        a.auraColor = 0xE8F4FF;
    });
    public static final Character ROSHI = add("master_roshi", Race.HUMAN, 0.5, Techniques.KAMEHAMEHA, 0xBFE6FF, false, null, 0xEDEDED, a -> {
        a.hairStyle = HairStyle.BALD.ordinal();
        a.height = 0.86f;
        a.skinColor = 0xE8B88A;
        a.eyebrowStyle = 2;
        a.expression = 3;
        a.outfit = 2;
        a.outfitPrimary = 0xF29A3A;
        a.outfitSecondary = 0xC8A46A;
    });
    public static final Character BULMA = add("bulma", Race.HUMAN, 0.05, Techniques.KI_BLAST, 0xFFFFFF, false, null, -1, a -> {
        a.presentation = 1;
        a.hairStyle = HairStyle.ANDROID_18.ordinal();
        a.hairColor = 0x2EC4D6;
        a.eyeColor = 0x2A6FD0;
        a.outfit = 2;
        a.outfitPrimary = 0xE85A9A;
        a.outfitSecondary = 0x2E4FA8;
    });
    public static final Character RADITZ = add("raditz", Race.SAIYAN, 1.05, Techniques.DOUBLE_SUNDAY, 0xC24BFF, true, null, -1, a -> {
        a.hairStyle = HairStyle.RADITZ.ordinal();
        a.hairColor = 0x0E0E12;
        a.skinColor = 0xD9A06B;
        a.outfit = 1;
        a.outfitPrimary = 0x1C1C2A;
        a.outfitSecondary = 0x6A2FA0;
        a.eyebrowStyle = 1;
        a.expression = 2;
        a.auraColor = 0xC24BFF;
        a.height = 1.1f;
        a.tail = true;
    });
    public static final Character PICCOLO = add("piccolo", Race.NAMEKIAN, 0.9, Techniques.SPECIAL_BEAM_CANNON, 0xF4FFB0, false, null, -1, a -> {
        a.skinColor = 0x5FB84E;
        a.height = 1.12f;
        a.eyebrowStyle = 1;
        a.expression = 2;
        a.outfit = 0;
        a.outfitPrimary = 0x4A2A7A;
        a.outfitSecondary = 0x2FA8D8;
        a.auraColor = 0xF4FFF0;
    });
    public static final Character NAPPA = add("nappa", Race.SAIYAN, 1.1, Techniques.BOMBER_DX, 0xFFD24A, true, null, 0x161616, a -> {
        a.hairStyle = HairStyle.BALD.ordinal();
        a.skinColor = 0xD9A06B;
        a.height = 1.18f;
        a.build = 2;
        a.outfit = 1;
        a.outfitPrimary = 0x2A2A3A;
        a.outfitSecondary = 0x7A5A30;
        a.eyebrowStyle = 1;
        a.expression = 2;
        a.auraColor = 0xFFE8A0;
        a.tail = true;
    });
    public static final Character VEGETA = add("vegeta", Race.SAIYAN, 1.2, Techniques.GALICK_GUN, 0xA040FF, true, "dbtenkaichi:great_ape", -1, a -> {
        a.hairStyle = HairStyle.VEGETA.ordinal();
        a.skinColor = 0xE8B07A;
        a.height = 0.95f;
        a.outfit = 1;
        a.outfitPrimary = 0x24356E;
        a.outfitSecondary = 0xF2F2F2;
        a.eyebrowStyle = 1;
        a.expression = 2;
        a.auraColor = 0xB070FF;
        a.tail = true;
    });
    public static final Character YAMCHA = add("yamcha", Race.HUMAN, 0.5, Techniques.KAMEHAMEHA, 0xE8F4FF, false, null, -1, a -> {
        a.hairStyle = HairStyle.YAMCHA.ordinal();
        a.outfit = 0;
        a.outfitPrimary = 0xF07A1A;
        a.outfitSecondary = 0x1F3FAE;
    });
    public static final Character TIEN = add("tien", Race.HUMAN, 0.6, Techniques.TRI_BEAM, 0xFFF2C0, false, null, -1, a -> {
        a.hairStyle = HairStyle.BALD.ordinal();
        a.height = 1.1f;
        a.outfit = 2;
        a.outfitPrimary = 0x2F8A4A;
        a.outfitSecondary = 0xD8B060;
    });
    public static final Character CHIAOTZU = add("chiaotzu", Race.HUMAN, 0.3, Techniques.KI_BLAST, 0xFFFFFF, false, null, -1, a -> {
        a.hairStyle = HairStyle.BALD.ordinal();
        a.height = 0.6f;
        a.skinColor = 0xF6EEE6;
        a.outfit = 2;
        a.outfitPrimary = 0xD84030;
        a.outfitSecondary = 0xF2E4C0;
    });
    public static final Character YAJIROBE = add("yajirobe", Race.HUMAN, 0.3, Techniques.KI_BLAST, 0xFFFFFF, false, null, -1, a -> {
        a.hairStyle = HairStyle.GOKU.ordinal();
        a.height = 0.82f;
        a.build = 2;
        a.outfit = 2;
        a.outfitPrimary = 0xC8A46A;
        a.outfitSecondary = 0x3A2A1A;
    });
    public static final Character KING_KAI = add("king_kai", Race.NAMEKIAN, 0.7, Techniques.KI_BLAST, 0xFFFFFF, false, null, -1, a -> {
        a.skinColor = 0x3A6BD8;
        a.height = 0.82f;
        a.build = 2;
        a.eyeStyle = 1;
        a.eyeColor = 0x111111;
        a.outfit = 2;
        a.outfitPrimary = 0x1A1A1A;
        a.outfitSecondary = 0xE8D23A;
    });
    public static final Character BUBBLES = add("bubbles", Race.HUMAN, 0.2, Techniques.KI_BLAST, 0xFFFFFF, false, null, -1, a -> {
        a.hairStyle = HairStyle.BALD.ordinal();
        a.skinColor = 0x8A5A30;
        a.height = 0.5f;
        a.outfit = Appearance.OUTFIT_NATURAL;
        a.outfitPrimary = 0x7A4A24;
        a.outfitSecondary = 0x7A4A24;
    });
    public static final Character GREGORY = add("gregory", Race.HUMAN, 0.2, Techniques.KI_BLAST, 0xFFFFFF, false, null, -1, a -> {
        a.hairStyle = HairStyle.BALD.ordinal();
        a.skinColor = 0x6AC840;
        a.height = 0.4f;
        a.eyeStyle = 1;
        a.eyeColor = 0x111111;
        a.outfit = Appearance.OUTFIT_NATURAL;
        a.outfitPrimary = 0x4A9A30;
        a.outfitSecondary = 0x4A9A30;
    });
    public static final Character KING_YEMMA = add("king_yemma", Race.DEMON, 2.0, Techniques.KI_BLAST, 0xFFFFFF, false, null, 0x2A1A10, a -> {
        a.skinColor = 0xC83A2A;
        a.height = 2.6f;
        a.build = 2;
        a.outfit = 2;
        a.outfitPrimary = 0x5A2A8C;
        a.outfitSecondary = 0xE8C030;
        a.featureColor = 0xE8D8A0;
    });
    public static final Character SAIBAMAN = add("saibaman", Race.BIO_ANDROID, 0.45, Techniques.KI_BLAST, 0xB8FF8A, false, null, -1, a -> {
        a.skinColor = 0x5AA83A;
        a.featureColor = 0x3A7A2A;
        a.height = 0.78f;
        a.outfit = Appearance.OUTFIT_NATURAL;
        a.tail = false;
    });

    public static final Character KAMI = add("kami", Race.NAMEKIAN, 0.6, Techniques.KI_BLAST, 0xF4FFF0, false, null, -1, a -> {
        a.skinColor = 0x63B84E;
        a.height = 1.06f;
        a.eyebrowStyle = 2;
        a.outfit = 2;
        a.outfitPrimary = 0xF2F2F2;
        a.outfitSecondary = 0x2F4FA0;
        a.auraColor = 0xF4FFF0;
    });
    public static final Character CELL_JR = add("cell_jr", Race.BIO_ANDROID, 0.45, Techniques.KI_BLAST, 0x9FD8FF, false, null, -1, a -> {
        a.skinColor = 0x4AA8E8;
        a.featureColor = 0x2A5AA8;
        a.height = 0.55f;
        a.outfit = Appearance.OUTFIT_NATURAL;
        a.tail = false;
    });

    private StoryCharacters() {
    }

    private static Character add(String id, Race race, double power, Technique signature, int blastColor, boolean scouter, String rageForm, int beard,
                                 Consumer<Appearance> setup) {
        Appearance a = Appearance.defaultsFor(race);
        a.tail = false;
        setup.accept(a);
        Character c = new Character(id, race, a, beard, power, signature, blastColor, scouter, rageForm);
        ALL.put(id, c);
        return c;
    }

    public static Character get(String id) {
        return id == null ? null : ALL.get(id);
    }

    public static Map<String, Character> all() {
        return ALL;
    }
}
