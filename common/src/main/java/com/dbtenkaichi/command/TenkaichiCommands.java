package com.dbtenkaichi.command;

import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.form.TransformationSystem;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.server.ServerEvents;
import com.dbtenkaichi.stats.StatType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Locale;

public final class TenkaichiCommands {
    private TenkaichiCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("tenkaichi");
        root.then(Commands.literal("stats")
                .executes(ctx -> showStats(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> showStats(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
        root.then(Commands.literal("setstat").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("stat", StringArgumentType.word())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(java.util.stream.Stream.concat(java.util.stream.Stream.of("all"), Arrays.stream(StatType.all()).map(StatType::id)), b))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 100000))
                                        .executes(TenkaichiCommands::setStat)))));
        root.then(Commands.literal("givetp").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1, 1_000_000_000L))
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    PlayerData data = PlayerManager.data(player);
                                    data.addTrainingPoints(LongArgumentType.getLong(ctx, "amount"));
                                    data.runtime().dataDirty = true;
                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.done"), true);
                                    return 1;
                                }))));
        root.then(Commands.literal("reset").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            PlayerData data = PlayerManager.data(player);
                            TransformationSystem.forceRevert(player, data, null);
                            data.resetCharacter();
                            data.runtime().resetCombat();
                            player.setNoGravity(false);
                            PlayerManager.refreshCaches(player, data);
                            PlayerManager.applyAttributes(player, data);
                            PlayerManager.markAllDirty(data);
                            PlayerManager.flush(player, data, player.level().getGameTime());
                            PlayerManager.openCreator(player);
                            ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.done"), true);
                            return 1;
                        })));
        root.then(Commands.literal("unlockform").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("form", StringArgumentType.greedyString())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(FormRegistry.all(false).stream().map(f -> f.id), b))
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    String id = StringArgumentType.getString(ctx, "form");
                                    FormDefinition def = FormRegistry.get(id, false);
                                    if (def == null) {
                                        ctx.getSource().sendFailure(Component.translatable("command.dbtenkaichi.unknown_form", id));
                                        return 0;
                                    }
                                    PlayerData data = PlayerManager.data(player);
                                    data.unlockForm(def.id);
                                    data.runtime().dataDirty = true;
                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.done"), true);
                                    return 1;
                                }))));
        root.then(Commands.literal("learn").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("technique", StringArgumentType.greedyString())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(java.util.stream.Stream.concat(Techniques.all().keySet().stream(), java.util.stream.Stream.of(com.dbtenkaichi.form.KaiokenSystem.ABILITY)), b))
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    String id = StringArgumentType.getString(ctx, "technique");
                                    if (Techniques.get(id) == null && !com.dbtenkaichi.form.KaiokenSystem.ABILITY.equals(id)) {
                                        ctx.getSource().sendFailure(Component.translatable("command.dbtenkaichi.unknown_technique", id));
                                        return 0;
                                    }
                                    PlayerData data = PlayerManager.data(player);
                                    data.learnSkill(id);
                                    data.runtime().dataDirty = true;
                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.done"), true);
                                    return 1;
                                }))));
        root.then(Commands.literal("restore").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            PlayerData data = PlayerManager.data(player);
                            data.setKi(data.runtime().cachedMaxKi);
                            data.setStamina(data.runtime().cachedMaxStamina);
                            data.setFatigue(0);
                            player.setHealth(player.getMaxHealth());
                            data.runtime().vitalsDirty = true;
                            ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.done"), true);
                            return 1;
                        })));
        root.then(Commands.literal("landmarks")
                .executes(ctx -> {
                    var sites = com.dbtenkaichi.world.Landmarks.sites();
                    if (sites.isEmpty()) {
                        ctx.getSource().sendFailure(Component.translatable("command.dbtenkaichi.no_landmarks"));
                        return 0;
                    }
                    sites.forEach((id, pos) -> ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.landmark",
                            Component.translatable("landmark.dbtenkaichi." + id), pos.getX(), pos.getY(), pos.getZ()), false));
                    return sites.size();
                }));
        root.then(Commands.literal("trial").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("start")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("lesson", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(com.dbtenkaichi.training.Lessons.ALL.stream().filter(com.dbtenkaichi.training.Lessons.Lesson::trial).map(com.dbtenkaichi.training.Lessons.Lesson::id), b))
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            com.dbtenkaichi.training.Lessons.Lesson lesson = com.dbtenkaichi.training.Lessons.get(StringArgumentType.getString(ctx, "lesson"));
                                            if (lesson == null || !lesson.trial()) {
                                                ctx.getSource().sendFailure(Component.translatable("command.dbtenkaichi.unknown_lesson"));
                                                return 0;
                                            }
                                            boolean ok = com.dbtenkaichi.training.TrialManager.start(player, PlayerManager.data(player), lesson, lesson.mentors().iterator().next());
                                            if (ok) {
                                                ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.done"), true);
                                            }
                                            return ok ? 1 : 0;
                                        }))))
                .then(Commands.literal("form")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("form", StringArgumentType.greedyString())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(FormRegistry.all(false).stream().map(f -> f.id), b))
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            com.dbtenkaichi.form.FormDefinition form = com.dbtenkaichi.form.FormRegistry.get(StringArgumentType.getString(ctx, "form"), false);
                                            if (form == null) {
                                                ctx.getSource().sendFailure(Component.translatable("command.dbtenkaichi.unknown_lesson"));
                                                return 0;
                                            }
                                            return com.dbtenkaichi.training.TrialManager.startForm(player, PlayerManager.data(player), form) ? 1 : 0;
                                        }))))
                .then(Commands.literal("end")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("win").executes(ctx -> com.dbtenkaichi.training.TrialManager.forceEnd(EntityArgument.getPlayer(ctx, "player"), true) ? 1 : 0))
                                .then(Commands.literal("lose").executes(ctx -> com.dbtenkaichi.training.TrialManager.forceEnd(EntityArgument.getPlayer(ctx, "player"), false) ? 1 : 0)))));
        root.then(Commands.literal("reload").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> {
                    ServerEvents.onDataReloaded(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.reloaded"), true);
                    return 1;
                }));
        dispatcher.register(root);
    }

    private static int setStat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String id = StringArgumentType.getString(ctx, "stat").toLowerCase(Locale.ROOT);
        StatType type = StatType.byId(id);
        boolean all = "all".equals(id);
        if (type == null && !all) {
            ctx.getSource().sendFailure(Component.translatable("command.dbtenkaichi.unknown_stat"));
            return 0;
        }
        int level = Math.min(IntegerArgumentType.getInteger(ctx, "level"), ConfigManager.balance().statHardCap);
        PlayerData data = PlayerManager.data(player);
        if (all) {
            for (StatType each : StatType.all()) {
                data.stats().set(each, level);
            }
        } else {
            data.stats().set(type, level);
        }
        PlayerManager.refreshCaches(player, data);
        PlayerManager.applyAttributes(player, data);
        data.runtime().dataDirty = true;
        ctx.getSource().sendSuccess(() -> Component.translatable("command.dbtenkaichi.done"), true);
        return 1;
    }

    private static int showStats(CommandSourceStack source, ServerPlayer player) {
        PlayerData data = PlayerManager.data(player);
        if (!data.isCreated()) {
            source.sendFailure(Component.translatable("command.dbtenkaichi.no_character"));
            return 0;
        }
        Race race = data.race();
        long bp = Fighter.of(player).displayedBattlePower();
        source.sendSuccess(() -> Component.translatable("command.dbtenkaichi.stats_header", player.getDisplayName(), Component.translatable(race.translationKey()), bp, data.trainingPoints()), false);
        for (StatType type : StatType.all()) {
            source.sendSuccess(() -> Component.literal(" - ").append(Component.translatable(type.translationKey())).append(": " + data.stat(type)), false);
        }
        return 1;
    }
}
