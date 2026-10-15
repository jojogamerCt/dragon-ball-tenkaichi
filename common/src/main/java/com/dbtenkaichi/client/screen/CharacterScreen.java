package com.dbtenkaichi.client.screen;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.config.ClientConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.network.ActionType;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.progression.ProgressionMath;
import com.dbtenkaichi.race.RacePassive;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.dbtenkaichi.client.ui.TenkaichiButton;
import com.dbtenkaichi.client.ui.TenkaichiUi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CharacterScreen extends Screen {
    private int tab;
    private String selectedSkill = "";

    public CharacterScreen(int tab) {
        this();
        this.tab = tab;
    }

    public CharacterScreen() {
        super(Component.translatable("screen.dbtenkaichi.character"));
    }

    private PlayerData data() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? new PlayerData() : Services.PLATFORM.playerData(mc.player);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 170;
        String[] tabs = {"stats", "skills", "forms", "settings"};
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            Button button = TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.character.tab." + tabs[i]), b -> {
                tab = index;
                rebuildWidgets();
            }).bounds(left + i * 86, 8, 84, 18).build();
            button.active = tab != i;
            ((TenkaichiButton) button).selected = tab == i;
            addRenderableWidget(button);
        }
        PlayerData data = data();
        switch (tab) {
            case 0 -> {
                int y = 44;
                for (StatType type : StatType.all()) {
                    final StatType stat = type;
                    addRenderableWidget(TenkaichiButton.create(Component.literal("+"), b -> Services.NETWORK.sendToServer(new Packets.Action(ActionType.UPGRADE_STAT.ordinal(), stat.ordinal())))
                            .bounds(left + 300, y - 3, 20, 12).build());
                    y += 13;
                }
            }
            case 1 -> {
                int y = 50;
                for (String skill : data.skills()) {
                    if (Techniques.get(skill) == null) {
                        continue;
                    }
                    final String id = skill;
                    addRenderableWidget(TenkaichiButton.create(Component.translatable(technique(skill)), b -> {
                        selectedSkill = id;
                        rebuildWidgets();
                    }).bounds(left, y, 150, 18).build()).active = !skill.equals(selectedSkill);
                    y += 22;
                }
                for (int slot = 0; slot < PlayerData.SKILL_SLOTS; slot++) {
                    final int s = slot;
                    String current = data.skillInSlot(slot);
                    Component label = Component.translatable("screen.dbtenkaichi.character.slot", slot + 1)
                            .append(": ").append(current.isEmpty() ? Component.literal("-") : Component.translatable(technique(current)));
                    addRenderableWidget(TenkaichiButton.create(label, b -> {
                        Services.NETWORK.sendToServer(new Packets.AssignSlot(s, selectedSkill));
                    }).bounds(left + 180, 50 + slot * 22, 160, 18).build());
                }
                int buyY = 50 + PlayerData.SKILL_SLOTS * 22 + 18;
                if (!data.hasSkill(com.dbtenkaichi.form.KaiokenSystem.ABILITY)) {
                    Component kaioken = Component.translatable("screen.dbtenkaichi.character.buy_technique", Component.translatable("technique.dbtenkaichi.kaioken"), com.dbtenkaichi.form.KaiokenSystem.PRICE, com.dbtenkaichi.form.KaiokenSystem.BATTLE_POWER);
                    addRenderableWidget(TenkaichiButton.create(kaioken, b -> Services.NETWORK.sendToServer(new Packets.Action(ActionType.BUY_TECHNIQUE.ordinal(), com.dbtenkaichi.form.KaiokenSystem.SHOP_INDEX)))
                            .bounds(left + 180, buyY, 160, 18).build()).active = ClientState.trainingPoints >= com.dbtenkaichi.form.KaiokenSystem.PRICE;
                    buyY += 22;
                }
                for (int i = 0; i < Techniques.UNLOCKS.size(); i++) {
                    Techniques.Unlock unlock = Techniques.UNLOCKS.get(i);
                    if (!unlock.allows(data.race()) || data.hasSkill(unlock.technique().id())) {
                        continue;
                    }
                    final int index = i;
                    Component label = Component.translatable("screen.dbtenkaichi.character.buy_technique", Component.translatable(unlock.technique().translationKey()), unlock.price(), unlock.battlePower());
                    addRenderableWidget(TenkaichiButton.create(label, b -> Services.NETWORK.sendToServer(new Packets.Action(ActionType.BUY_TECHNIQUE.ordinal(), index)))
                            .bounds(left + 180, buyY, 160, 18).build()).active = ClientState.trainingPoints >= unlock.price();
                    buyY += 22;
                }
            }
            case 2 -> {
                List<FormDefinition> chain = new ArrayList<>(FormRegistry.chainFor(data.race(), true));
                for (int i = 0; i < chain.size(); i++) {
                    FormDefinition form = chain.get(i);
                    if (data.hasUnlockedForm(form.id) || !"trial".equals(form.requirements.unlock)) {
                        continue;
                    }
                    final int index = i;
                    addRenderableWidget(TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.character.trial"), b -> {
                        Services.NETWORK.sendToServer(new Packets.Action(ActionType.FORM_TRIAL.ordinal(), index));
                        onClose();
                    }).bounds(left + 290, 47 + i * 38, 56, 14).build()).active = formReady(data, form);
                }
            }
            case 3 -> {
                ClientConfig cfg = ConfigManager.client();
                int y = 50;
                addRenderableWidget(TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.settings.quality", Component.translatable("quality.dbtenkaichi." + cfg.quality().name().toLowerCase(Locale.ROOT))), b -> {
                    ClientConfig.Quality[] values = ClientConfig.Quality.values();
                    cfg.graphicsQuality = values[(cfg.quality().ordinal() + 1) % values.length].name().toLowerCase(Locale.ROOT);
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.settings.cinematics", Component.translatable("cinematics.dbtenkaichi." + cfg.cinematics)), b -> {
                    cfg.cinematics = switch (cfg.cinematics) {
                        case "full" -> "short";
                        case "short" -> "off";
                        default -> "full";
                    };
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.settings.shake", Math.round(cfg.cameraShake * 100)), b -> {
                    cfg.cameraShake = cfg.cameraShake >= 1.0f ? 0.0f : cfg.cameraShake + 0.25f;
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.settings.hitstop", Component.translatable(cfg.hitstop ? "options.on" : "options.off")), b -> {
                    cfg.hitstop = !cfg.hitstop;
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.settings.bp", Component.translatable(cfg.showBattlePower ? "options.on" : "options.off")), b -> {
                    cfg.showBattlePower = !cfg.showBattlePower;
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
            }
            default -> {
            }
        }
    }

    private static boolean formReady(PlayerData data, FormDefinition form) {
        if (!PlayerData.BASE_FORM.equals(form.parent) && !data.hasUnlockedForm(form.parent)) {
            return false;
        }
        if (com.dbtenkaichi.stats.BattlePower.base(data.stats(), ConfigManager.balance()) < form.requirements.battlePower) {
            return false;
        }
        for (java.util.Map.Entry<String, Integer> entry : form.requirements.stats.entrySet()) {
            com.dbtenkaichi.stats.StatType type = com.dbtenkaichi.stats.StatType.byId(entry.getKey());
            if (type != null && data.stat(type) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void requirements(GuiGraphicsExtractor g, PlayerData data, FormDefinition form, int x, int y) {
        int ok = 0xFF7CFFB0;
        int bad = 0xFFFF7A6A;
        java.text.NumberFormat fmt = java.text.NumberFormat.getIntegerInstance(Locale.ROOT);
        List<Component> parts = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : form.requirements.stats.entrySet()) {
            com.dbtenkaichi.stats.StatType type = com.dbtenkaichi.stats.StatType.byId(entry.getKey());
            if (type != null) {
                parts.add(Component.translatable(type.translationKey()).append(" " + data.stat(type) + "/" + entry.getValue()));
                colors.add(data.stat(type) >= entry.getValue() ? ok : bad);
            }
        }
        long bp = com.dbtenkaichi.stats.BattlePower.base(data.stats(), ConfigManager.balance());
        if (form.requirements.battlePower > 0) {
            parts.add(Component.translatable("screen.dbtenkaichi.character.req_bp", fmt.format(bp), fmt.format(form.requirements.battlePower)));
            colors.add(bp >= form.requirements.battlePower ? ok : bad);
        }
        if (!PlayerData.BASE_FORM.equals(form.parent)) {
            FormDefinition parent = FormRegistry.get(form.parent, true);
            parts.add(Component.translatable("screen.dbtenkaichi.character.req_parent", Component.translatable(parent == null ? form.parent : parent.name)));
            colors.add(data.hasUnlockedForm(form.parent) ? ok : bad);
        }
        if ("trial".equals(form.requirements.unlock)) {
            parts.add(Component.translatable("screen.dbtenkaichi.character.req_trial"));
            colors.add(0xFFC8C8C8);
        }
        int cx = x;
        int cy = y;
        int split = Math.max(1, (parts.size() + 1) / 2);
        for (int i = 0; i < parts.size(); i++) {
            if (i == split) {
                cx = x;
                cy += 10;
            } else if (i > 0) {
                g.text(this.font, " · ", cx, cy, 0xFF888888, true);
                cx += this.font.width(" · ");
            }
            g.text(this.font, parts.get(i), cx, cy, colors.get(i), true);
            cx += this.font.width(parts.get(i));
        }
    }

    private static String technique(String id) {
        Technique technique = Techniques.get(id);
        return technique == null ? id : technique.translationKey();
    }

    @Override
    public void tick() {
        super.tick();
        if (tab == 1 && ClientState.clientTicks % 10 == 0) {
            rebuildWidgets();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        TenkaichiUi.background(g, this.width, this.height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        TenkaichiUi.panel(g, this.width / 2 - 180, 2, 360, this.height - 6);
        TenkaichiUi.divider(g, this.width / 2 - 172, 41, 344);
        super.extractRenderState(g, mouseX, mouseY, partial);
        PlayerData data = data();
        int left = this.width / 2 - 170;
        g.text(this.font, Component.translatable(data.race().translationKey()).append(" · ").append(Component.translatable("hud.dbtenkaichi.tp", data.trainingPoints())), left, 30, 0xFFFFD45A, true);
        g.text(this.font, "BP " + java.text.NumberFormat.getIntegerInstance(java.util.Locale.ROOT).format(ClientState.battlePower), left + 230, 30, 0xFFFFD45A, true);
        switch (tab) {
            case 0 -> {
                int y = 44;
                int cap = ProgressionMath.statCap(data.stat(StatType.POTENTIAL), ConfigManager.balance());
                for (StatType type : StatType.all()) {
                    int level = data.stat(type);
                    double affinity = data.race().affinity(type) * (type == StatType.POTENTIAL ? 0.5 : 1.0);
                    long cost = ProgressionMath.statUpgradeCost(level, affinity, ConfigManager.balance());
                    int color = data.trainingPoints() >= cost ? 0xFFFFFFFF : 0xFF888888;
                    g.text(this.font, Component.translatable(type.translationKey()), left, y, 0xFFDDDDDD, true);
                    g.text(this.font, String.valueOf(level), left + 120, y, 0xFFFFFFFF, true);
                    float practice = data.practice(type);
                    TenkaichiUi.gauge(g, left + 150, y + 1, 52, 6, practice, 0.0f, 0xFF7CFFB0);
                    g.text(this.font, Component.translatable("screen.dbtenkaichi.character.cost", cost), left + 210, y, color, true);
                    y += 13;
                }
                g.text(this.font, Component.translatable("screen.dbtenkaichi.character.cap", cap), left, y + 2, 0xFFAAAAAA, true);
                StringBuilder passives = new StringBuilder();
                for (RacePassive passive : data.race().passives()) {
                    if (!passives.isEmpty()) {
                        passives.append(", ");
                    }
                    passives.append(Component.translatable("passive.dbtenkaichi." + passive.name().toLowerCase(Locale.ROOT)).getString());
                }
                g.text(this.font, Component.translatable("screen.dbtenkaichi.character.passives", passives.toString()), left, y + 13, 0xFFBFE6FF, true);
            }
            case 1 -> g.text(this.font, Component.translatable("screen.dbtenkaichi.character.skills_hint"), left, this.height - 24, 0xFFAAAAAA, true);
            case 2 -> {
                int y = 50;
                List<FormDefinition> chain = new ArrayList<>(FormRegistry.chainFor(data.race(), true));
                if (chain.isEmpty()) {
                    g.text(this.font, Component.translatable("screen.dbtenkaichi.character.no_forms"), left, y, 0xFFAAAAAA, true);
                }
                for (FormDefinition form : chain) {
                    boolean unlocked = data.hasUnlockedForm(form.id);
                    int color = unlocked ? 0xFF000000 | FormDefinition.parseColor(form.visual.auraColor, 0xFFFFFF) : 0xFF777777;
                    g.text(this.font, Component.translatable(form.name).append(" x" + (int) form.bpMultiplier), left, y, color, true);
                    float mastery = data.mastery(form.id);
                    TenkaichiUi.gauge(g, left + 180, y + 1, 102, 7, mastery, 0.0f, 0xFFFFE25A);
                    if (unlocked) {
                        g.text(this.font, Component.translatable("screen.dbtenkaichi.character.unlocked"), left + 10, y + 12, 0xFF7CFFB0, true);
                    } else {
                        requirements(g, data, form, left + 10, y + 12);
                    }
                    y += 38;
                }
            }
            default -> {
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
