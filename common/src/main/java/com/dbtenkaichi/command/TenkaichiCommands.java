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
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Arrays.stream(StatType.all()).map(StatType::id), b))
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
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Techniques.all().keySet(), b))
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    String id = StringArgumentType.getString(ctx, "technique");
                                    if (Techniques.get(id) == null) {
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
        StatType type = StatType.byId(StringArgumentType.getString(ctx, "stat").toLowerCase(Locale.ROOT));
        if (type == null) {
            ctx.getSource().sendFailure(Component.translatable("command.dbtenkaichi.unknown_stat"));
            return 0;
        }
        int level = Math.min(IntegerArgumentType.getInteger(ctx, "level"), ConfigManager.balance().statHardCap);
        PlayerData data = PlayerManager.data(player);
        data.stats().set(type, level);
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
