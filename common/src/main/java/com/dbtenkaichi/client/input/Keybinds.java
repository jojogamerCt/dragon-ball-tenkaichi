package com.dbtenkaichi.client.input;

import com.dbtenkaichi.Tenkaichi;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.List;

public final class Keybinds {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Tenkaichi.id("tenkaichi"));

    public static final KeyMapping CHARGE = key("charge", InputConstants.KEY_R);
    public static final KeyMapping FLIGHT = key("flight", InputConstants.KEY_G);
    public static final KeyMapping STANCE = key("combat_stance", InputConstants.KEY_H);
    public static final KeyMapping LOCK_ON = key("lock_on", InputConstants.KEY_J);
    public static final KeyMapping NEXT_TARGET = key("next_target", InputConstants.KEY_K);
    public static final KeyMapping DODGE = key("dodge", InputConstants.KEY_LALT);
    public static final KeyMapping DASH = key("dash", InputConstants.KEY_Z);
    public static final KeyMapping TRANSFORM = key("transform", InputConstants.KEY_V);
    public static final KeyMapping REVERT = key("revert", InputConstants.KEY_B);
    public static final KeyMapping SKILL_1 = key("skill_1", InputConstants.KEY_X);
    public static final KeyMapping SKILL_2 = key("skill_2", InputConstants.KEY_C);
    public static final KeyMapping SKILL_3 = key("skill_3", InputConstants.KEY_N);
    public static final KeyMapping SKILL_4 = key("skill_4", InputConstants.KEY_M);
    public static final KeyMapping MENU = key("character_menu", InputConstants.KEY_U);

    public static final List<KeyMapping> ALL = List.of(CHARGE, FLIGHT, STANCE, LOCK_ON, NEXT_TARGET, DODGE, DASH, TRANSFORM, REVERT, SKILL_1, SKILL_2, SKILL_3, SKILL_4, MENU);
    public static final KeyMapping[] SKILLS = {SKILL_1, SKILL_2, SKILL_3, SKILL_4};

    private Keybinds() {
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.dbtenkaichi." + name, code, CATEGORY);
    }
}
