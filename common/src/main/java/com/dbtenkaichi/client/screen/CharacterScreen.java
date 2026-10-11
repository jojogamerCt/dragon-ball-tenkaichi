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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CharacterScreen extends Screen {
    private int tab;
    private String selectedSkill = "";

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
            Button button = Button.builder(Component.translatable("screen.dbtenkaichi.character.tab." + tabs[i]), b -> {
                tab = index;
                rebuildWidgets();
            }).bounds(left + i * 86, 8, 84, 18).build();
            button.active = tab != i;
            addRenderableWidget(button);
        }
        PlayerData data = data();
        switch (tab) {
            case 0 -> {
                int y = 44;
                for (StatType type : StatType.all()) {
                    final StatType stat = type;
                    addRenderableWidget(Button.builder(Component.literal("+"), b -> Services.NETWORK.sendToServer(new Packets.Action(ActionType.UPGRADE_STAT.ordinal(), stat.ordinal())))
                            .bounds(left + 300, y - 3, 20, 12).build());
                    y += 13;
                }
            }
            case 1 -> {
                int y = 50;
                for (String skill : data.skills()) {
                    final String id = skill;
                    addRenderableWidget(Button.builder(Component.translatable(technique(skill)), b -> {
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
                    addRenderableWidget(Button.builder(label, b -> {
                        Services.NETWORK.sendToServer(new Packets.AssignSlot(s, selectedSkill));
                    }).bounds(left + 180, 50 + slot * 22, 160, 18).build());
                }
            }
            case 3 -> {
                ClientConfig cfg = ConfigManager.client();
                int y = 50;
                addRenderableWidget(Button.builder(Component.translatable("screen.dbtenkaichi.settings.quality", Component.translatable("quality.dbtenkaichi." + cfg.quality().name().toLowerCase(Locale.ROOT))), b -> {
                    ClientConfig.Quality[] values = ClientConfig.Quality.values();
                    cfg.graphicsQuality = values[(cfg.quality().ordinal() + 1) % values.length].name().toLowerCase(Locale.ROOT);
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(Button.builder(Component.translatable("screen.dbtenkaichi.settings.cinematics", Component.translatable("cinematics.dbtenkaichi." + cfg.cinematics)), b -> {
                    cfg.cinematics = switch (cfg.cinematics) {
                        case "full" -> "short";
                        case "short" -> "off";
                        default -> "full";
                    };
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(Button.builder(Component.translatable("screen.dbtenkaichi.settings.shake", Math.round(cfg.cameraShake * 100)), b -> {
                    cfg.cameraShake = cfg.cameraShake >= 1.0f ? 0.0f : cfg.cameraShake + 0.25f;
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(Button.builder(Component.translatable("screen.dbtenkaichi.settings.hitstop", Component.translatable(cfg.hitstop ? "options.on" : "options.off")), b -> {
                    cfg.hitstop = !cfg.hitstop;
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
                y += 24;
                addRenderableWidget(Button.builder(Component.translatable("screen.dbtenkaichi.settings.bp", Component.translatable(cfg.showBattlePower ? "options.on" : "options.off")), b -> {
                    cfg.showBattlePower = !cfg.showBattlePower;
                    ConfigManager.saveClient();
                    rebuildWidgets();
                }).bounds(left, y, 220, 20).build());
            }
            default -> {
            }
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
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        g.fill(this.width / 2 - 178, 4, this.width / 2 + 178, this.height - 8, 0xC0101018);
        g.outline(this.width / 2 - 178, 4, 356, this.height - 12, 0x66FFD45A);
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
                    g.fill(left + 150, y + 2, left + 200, y + 5, 0xFF303030);
                    g.fill(left + 150, y + 2, left + 150 + (int) (50 * practice), y + 5, 0xFF7CFFB0);
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
                    g.fill(left + 180, y + 2, left + 280, y + 6, 0xFF303030);
                    g.fill(left + 180, y + 2, left + 180 + (int) (100 * mastery), y + 6, 0xFFFFE25A);
                    String req = unlocked ? Component.translatable("screen.dbtenkaichi.character.unlocked").getString()
                            : Component.translatable("screen.dbtenkaichi.character.requires", form.requirements.battlePower).getString();
                    g.text(this.font, req, left + 10, y + 11, 0xFFAAAAAA, true);
                    y += 26;
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
