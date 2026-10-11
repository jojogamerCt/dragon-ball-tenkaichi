package com.dbtenkaichi.client;

import com.dbtenkaichi.entity.FighterNpc;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.entity.RaiderEntity;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.HairStyle;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.race.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientState {
    public static float ki;
    public static float maxKi = 100.0f;
    public static float stamina;
    public static float maxStamina = 100.0f;
    public static float fatigue;
    public static long battlePower;
    public static long trainingPoints;
    public static int flags;
    public static int chargeTicks;
    public static long clientTicks;

    public static int clashId = -1;
    public static int clashFirst = -1;
    public static int clashSecond = -1;
    public static float clashBalance;
    public static int clashPulse;
    public static long clashUpdated;

    public static @Nullable Appearance preview;
    public static @Nullable Race previewRace;

    private static final Map<Integer, EntityVisual> VISUALS = new HashMap<>();
    private static final EntityVisual NPC_FALLBACK = new EntityVisual();

    private ClientState() {
    }

    public static EntityVisual visual(int entityId) {
        return VISUALS.computeIfAbsent(entityId, id -> new EntityVisual());
    }

    public static @Nullable EntityVisual existing(int entityId) {
        return VISUALS.get(entityId);
    }

    public static EntityVisual forEntity(@Nullable Entity entity) {
        if (entity == null) {
            return NPC_FALLBACK;
        }
        EntityVisual visual = visual(entity.getId());
        visual.lastSeen = clientTicks;
        if (entity instanceof FighterNpc npc) {
            visual.created = true;
            visual.flags = npc.visualFlags();
            visual.kiRatio = npc.visualKiRatio();
            String form = npc.visualForm();
            visual.form = form.isEmpty() ? PlayerData.BASE_FORM : form;
            if (npc instanceof RaiderEntity) {
                visual.race = Race.SAIYAN;
                visual.appearance = raiderAppearance();
            } else if (npc instanceof MasterEntity) {
                visual.race = Race.HUMAN;
                visual.appearance = masterAppearance();
            }
        }
        if (entity instanceof Player player && player.isLocalPlayer()) {
            if (preview != null && previewRace != null) {
                visual.appearance = preview;
                visual.race = previewRace;
                visual.created = true;
            }
        }
        return visual;
    }

    public static boolean localHas(int flag) {
        return (flags & flag) != 0;
    }

    public static @Nullable PlayerData localData() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? null : com.dbtenkaichi.platform.Services.PLATFORM.playerData(mc.player);
    }

    public static void tick() {
        clientTicks++;
        if (clientTicks % 200 == 0) {
            Minecraft mc = Minecraft.getInstance();
            Iterator<Map.Entry<Integer, EntityVisual>> it = VISUALS.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, EntityVisual> entry = it.next();
                if (mc.level == null || mc.level.getEntity(entry.getKey()) == null && clientTicks - entry.getValue().lastSeen > 600) {
                    it.remove();
                }
            }
        }
        if (clashId >= 0 && clientTicks - clashUpdated > 40) {
            clashId = -1;
        }
    }

    public static void clear() {
        VISUALS.clear();
        ki = 0;
        flags = 0;
        clashId = -1;
        preview = null;
        previewRace = null;
    }

    private static Appearance raiderAppearance() {
        Appearance a = Appearance.defaultsFor(Race.SAIYAN);
        a.hairStyle = HairStyle.LONG_MANE.ordinal();
        a.hairColor = 0x0E0E12;
        a.auraColor = 0xC24BFF;
        a.skinColor = 0xD9A06B;
        a.tail = true;
        return a;
    }

    private static Appearance masterAppearance() {
        Appearance a = Appearance.defaultsFor(Race.HUMAN);
        a.hairStyle = HairStyle.BALD.ordinal();
        a.hairColor = 0xEDEDED;
        a.auraColor = 0xBFE6FF;
        a.skinColor = 0xE8B88A;
        a.tail = false;
        return a;
    }
}
