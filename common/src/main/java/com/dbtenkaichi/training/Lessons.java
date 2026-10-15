package com.dbtenkaichi.training;

import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.entity.MasterKind;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.registry.ModItems;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.BattlePower;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class Lessons {
    public static final int AVAILABLE = 0;
    public static final int DONE = 1;
    public static final int LOCKED = 2;
    public static final int COOLDOWN = 3;

    public enum Reward {
        SKILL, SENZU, SACRED_WATER, TIME_CHAMBER
    }

    public record Lesson(String id, Set<MasterKind> mentors, Reward reward, String skill, int price, long battlePower, boolean trial) {
        public boolean buyable() {
            return price > 0;
        }

        public String translationKey() {
            return "lesson.dbtenkaichi." + id;
        }
    }

    public static final List<Lesson> ALL = List.of(
            new Lesson("kamehameha", EnumSet.of(MasterKind.MASTER, MasterKind.ROSHI), Reward.SKILL, Techniques.KAMEHAMEHA.id(), 300, 1_500L, true),
            new Lesson("senzu", EnumSet.of(MasterKind.KARIN), Reward.SENZU, "", 40, 0L, false),
            new Lesson("sacred_water", EnumSet.of(MasterKind.KARIN), Reward.SACRED_WATER, "dbtenkaichi:sacred_water", 0, 2_000L, true),
            new Lesson("time_chamber", EnumSet.of(MasterKind.KAMI), Reward.TIME_CHAMBER, "", 0, 6_000L, true));

    private Lessons() {
    }

    public static Lesson get(String id) {
        for (Lesson lesson : ALL) {
            if (lesson.id().equals(id)) {
                return lesson;
            }
        }
        return null;
    }

    public static List<Lesson> forMaster(MasterKind kind) {
        List<Lesson> out = new ArrayList<>();
        for (Lesson lesson : ALL) {
            if (lesson.mentors().contains(kind)) {
                out.add(lesson);
            }
        }
        return out;
    }

    public static int state(ServerPlayer player, PlayerData data, Lesson lesson) {
        if (!lesson.skill().isEmpty() && data.hasSkill(lesson.skill())) {
            return DONE;
        }
        if (BattlePower.base(data.stats(), ConfigManager.balance()) < lesson.battlePower()) {
            return LOCKED;
        }
        if (lesson.reward() == Reward.TIME_CHAMBER && data.trainingRepetitions("time_chamber", TrainingSystem.day(player)) > 0) {
            return COOLDOWN;
        }
        return AVAILABLE;
    }

    public static void buy(ServerPlayer player, PlayerData data, MasterEntity master, Lesson lesson) {
        if (!lesson.buyable() || !lesson.mentors().contains(master.kind())) {
            return;
        }
        int state = state(player, data, lesson);
        if (state == LOCKED) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.lesson_locked", lesson.battlePower()));
            return;
        }
        if (state != AVAILABLE) {
            return;
        }
        if (data.trainingPoints() < lesson.price()) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.not_enough_tp", lesson.price()));
            return;
        }
        data.setTrainingPoints(data.trainingPoints() - lesson.price());
        grant(player, data, lesson);
        master.openMenu(player);
    }

    public static void grant(ServerPlayer player, PlayerData data, Lesson lesson) {
        data.missions().setFlag("lesson:" + lesson.id(), true);
        switch (lesson.reward()) {
            case SKILL -> {
                data.learnSkill(lesson.skill());
                player.sendSystemMessage(Component.translatable("message.dbtenkaichi.learned", Component.translatable(lesson.translationKey())));
            }
            case SENZU -> {
                ItemStack stack = new ItemStack(ModItems.SENZU_BEAN.get());
                player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
            }
            case SACRED_WATER -> {
                data.learnSkill(lesson.skill());
                for (StatType type : new StatType[]{StatType.SPEED, StatType.AGILITY, StatType.MARTIAL_ARTS}) {
                    data.stats().add(type, 2);
                }
                PlayerManager.refreshCaches(player, data);
                PlayerManager.applyAttributes(player, data);
                player.sendSystemMessage(Component.translatable("message.dbtenkaichi.sacred_water"));
            }
            case TIME_CHAMBER -> {
                data.recordTraining("time_chamber", TrainingSystem.day(player));
                PlayerManager.grantTp(player, data, 250.0);
                TrainingSystem.addPractice(player, data, StatType.SPIRIT, 0.6);
                player.sendSystemMessage(Component.translatable("message.dbtenkaichi.time_chamber"));
            }
        }
        data.runtime().dataDirty = true;
        CombatSystem.fx(player, CombatFxKind.LEVEL_UP, player, player, player.position(), 1.0f);
        CombatSystem.playSound(player.level(), player, ModSounds.UI_CONFIRM.get(), 1.0f, 1.0f);
    }
}
