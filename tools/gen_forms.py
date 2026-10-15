import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FORMS = os.path.join(ROOT, "common", "src", "main", "resources", "data", "dbtenkaichi", "tenkaichi_forms")
LANG = os.path.join(ROOT, "common", "src", "main", "resources", "assets", "dbtenkaichi", "lang")


def P(kind, amount=0.0):
    return {"type": kind, "amount": amount}


def form(fid, en, it, races, parent, order, bp, pot, mult, speed, drain, passives, visual, cinematic, ticks=160,
         unlock="trial", arena=None, required=None, tail=False, absorptions=0, kaioken=False, stamina=0.0, health=0.0,
         activation=None, mastery_rate=0.001, control=0.15):
    req = {"battlePower": bp, "stats": {"potential": pot} if pot else {}, "unlock": unlock}
    if required:
        req["requiredForm"] = "dbtenkaichi:" + required
    if tail:
        req["tail"] = True
    if absorptions:
        req["absorptions"] = absorptions
    data = {
        "name": "form.dbtenkaichi." + fid,
        "races": races,
        "parent": "dbtenkaichi:" + parent if parent != "base" else "dbtenkaichi:base",
        "order": order,
        "requirements": req,
        "bpMultiplier": mult,
        "speedMultiplier": speed,
        "kiDrainPerSecond": drain,
        "staminaDrainPerSecond": stamina,
        "healthDrainPerSecond": health,
        "masteryPerSecond": mastery_rate,
        "masteryDrainReduction": 0.6,
        "lowMasteryControlPenalty": control,
        "kiActivationCost": activation if activation is not None else round(15 + drain * 3, 1),
        "cinematicTicks": ticks,
        "cinematic": cinematic,
        "quickTicks": 10,
        "kaiokenCompatible": kaioken,
        "passives": passives,
        "visual": visual,
    }
    if arena:
        data["trialArena"] = arena
    return fid, en, it, data


def V(**kw):
    out = {"auraStyle": "flame", "auraScale": 1.0, "auraIntensity": 1.0}
    for key, value in kw.items():
        out[key] = value
    return out


S = ["saiyan"]
SH = ["saiyan", "half_saiyan"]
GOLD = dict(hairColor="#FFE25A", hairGlow=True, eyeColor="#33D6B0", auraColor="#FFD43B", auraSecondary="#FFF6B8")

FORM_LIST = [
    form("great_ape", "Great Ape", "Scimmione", S, "base", 5, 0, 0, 10.0, 0.8, 1.0,
         [P("giant", 4.0), P("strike", 0.4), P("super_armor"), P("rampage", 0.5), P("moon_bound"), P("tail_bound"), P("speed", -0.25)],
         V(body="great_ape", eyeColor="#FF2A1A", auraColor="#FFE6A8", auraSecondary="#FFFFFF", auraScale=1.6),
         "great_ape_transformation", 190, unlock="moon", tail=True, activation=0, mastery_rate=0.0008, control=0.1),
    form("super_saiyan", "Super Saiyan", "Super Saiyan", SH, "base", 10, 20000, 8, 50.0, 1.15, 3.0,
         [P("last_stand", 0.2)], V(hairStyle="super_spikes", lightning=False, auraScale=1.15, **GOLD),
         "super_saiyan_awakening", 150, arena="NAMEK", kaioken=True, activation=20, mastery_rate=0.0012, control=0.12),
    form("ascended_super_saiyan", "Ascended Super Saiyan", "Super Saiyan di secondo grado", SH, "super_saiyan", 12, 60000, 12, 60.0, 1.1, 4.5,
         [P("strike", 0.15), P("guard", 0.1)], V(hairStyle="super_spikes", build=2, auraScale=1.25, **GOLD),
         "ascended_super_saiyan", 140, arena="TIME_CHAMBER"),
    form("ultra_super_saiyan", "Ultra Super Saiyan", "Super Saiyan di terzo grado", SH, "ascended_super_saiyan", 13, 90000, 16, 75.0, 0.75, 10.0,
         [P("strike", 0.4), P("speed", -0.35), P("super_armor")], V(hairStyle="super_spikes", build=3, auraScale=1.45, **GOLD),
         "ultra_super_saiyan", 150, arena="TIME_CHAMBER"),
    form("full_power_super_saiyan", "Full Power Super Saiyan", "Super Saiyan Full Power", SH, "super_saiyan", 14, 120000, 18, 65.0, 1.2, 0.6,
         [P("stamina_free"), P("efficient", 0.6), P("ki_regen", 0.3)], V(hairStyle="super_spikes", auraScale=0.9, auraIntensity=0.8, **GOLD),
         "full_power_super_saiyan", 140, arena="CELL_RING", kaioken=True),
    form("super_saiyan_2", "Super Saiyan 2", "Super Saiyan 2", SH, "super_saiyan", 20, 90000, 20, 100.0, 1.25, 5.0,
         [P("ki_power", 0.15), P("last_stand", 0.15)], V(hairStyle="ascended_spikes", hairColor="#FFE04A", hairGlow=True, eyeColor="#2FD8B4",
                                                          auraColor="#FFD43B", auraSecondary="#FFFBD8", lightning=True, auraScale=1.25, auraIntensity=1.15),
         "super_saiyan_2_awakening", 150, arena="CELL_RING", required="super_saiyan", activation=40),
    form("super_saiyan_3", "Super Saiyan 3", "Super Saiyan 3", S, "super_saiyan_2", 30, 250000, 35, 400.0, 1.35, 12.0,
         [P("strike", 0.2), P("ki_power", 0.2)], V(hairStyle="legendary_mane", hairColor="#FFD640", hairGlow=True, eyeColor="#2FD8B4",
                                                    auraColor="#FFC830", auraSecondary="#FFF4C0", lightning=True, auraScale=1.4, auraIntensity=1.3),
         "super_saiyan_3_awakening", 170, arena="CANYON", required="super_saiyan_2", activation=80, stamina=0.4, mastery_rate=0.0008),
    form("super_saiyan_4", "Super Saiyan 4", "Super Saiyan 4", S, "base", 40, 650000, 60, 1500.0, 1.35, 9.0,
         [P("strike", 0.3), P("guard", 0.15), P("tail_bound")],
         V(hairStyle="ssj4_mane", hairColor="#16110F", eyeColor="#FFC93A", body="ssj4", auraColor="#FF4A2A", auraSecondary="#FFC060", lightning=True, auraScale=1.3),
         "super_saiyan_4", 170, arena="DEMON_REALM", required="super_saiyan_3", tail=True),
    form("super_saiyan_god", "Super Saiyan God", "Super Saiyan God", S, "base", 50, 350000, 40, 1000.0, 1.3, 2.5,
         [P("god_ki"), P("speed", 0.2), P("efficient", 0.4), P("regeneration", 0.005)],
         V(hairColor="#D8302C", hairGlow=True, eyeColor="#C8302C", build=0, auraColor="#FF5A2A", auraSecondary="#FFD08A", auraScale=1.1),
         "super_saiyan_god", 180, arena="BEERUS_PLANET"),
    form("super_saiyan_blue", "Super Saiyan Blue", "Super Saiyan Blue", S, "base", 55, 500000, 50, 2500.0, 1.3, 12.0,
         [P("god_ki"), P("strike", 0.25), P("ki_power", 0.25)],
         V(hairStyle="super_spikes", hairColor="#3AB7FF", hairGlow=True, eyeColor="#4AC8FF", auraColor="#3FA8FF", auraSecondary="#CFF0FF", auraScale=1.2),
         "super_saiyan_blue", 160, arena="BEERUS_PLANET", required="super_saiyan_god", kaioken=True),
    form("super_saiyan_blue_evolved", "Super Saiyan Blue Evolved", "Super Saiyan Blue Evolution", S, "super_saiyan_blue", 60, 650000, 60, 4000.0, 1.25, 16.0,
         [P("god_ki"), P("strike", 0.35), P("guard", 0.2), P("last_stand", 0.2)],
         V(hairStyle="ascended_spikes", hairColor="#1E5FD8", hairGlow=True, eyeColor="#2E6FE0", auraColor="#1E4FD0", auraSecondary="#9FC8FF", lightning=True, auraScale=1.35),
         "super_saiyan_blue_evolved", 170, arena="TOURNAMENT_OF_POWER", required="super_saiyan_blue"),
    form("super_saiyan_rose", "Super Saiyan Rosé", "Super Saiyan Rosé", S, "base", 58, 500000, 50, 2500.0, 1.3, 11.0,
         [P("god_ki"), P("ki_power", 0.35), P("intimidate", 0.15)],
         V(hairStyle="super_spikes", hairColor="#FF6FB4", hairGlow=True, eyeColor="#E0306A", auraColor="#FF5FA8", auraSecondary="#FFD0E8", auraScale=1.2),
         "super_saiyan_rose", 170, arena="RUINED_CITY", required="super_saiyan_god"),
    form("ultra_instinct_sign", "Ultra Instinct -Sign-", "Ultra Istinto -Presagio-", S, "base", 70, 800000, 70, 5000.0, 1.5, 14.0,
         [P("auto_dodge", 0.35), P("god_ki"), P("speed", 0.25)],
         V(eyeColor="#C8D4FF", auraColor="#CFE3FF", auraSecondary="#FFFFFF", auraScale=1.3, auraIntensity=1.2),
         "ultra_instinct_sign", 180, arena="TOURNAMENT_OF_POWER", required="super_saiyan_blue"),
    form("ultra_instinct", "Ultra Instinct", "Ultra Istinto", S, "ultra_instinct_sign", 75, 1000000, 80, 8000.0, 1.7, 18.0,
         [P("auto_dodge", 0.55), P("reflect", 0.3), P("god_ki"), P("speed", 0.3), P("strike", 0.2)],
         V(hairStyle="super_spikes", hairColor="#E6EAF5", hairGlow=True, eyeColor="#DCE6FF", auraColor="#DDE8FF", auraSecondary="#FFFFFF", lightning=True, auraScale=1.4),
         "ultra_instinct", 190, arena="TOURNAMENT_OF_POWER"),
    form("true_ultra_instinct", "True Ultra Instinct", "Vero Ultra Istinto", S, "ultra_instinct", 78, 1200000, 90, 10000.0, 1.6, 15.0,
         [P("auto_dodge", 0.45), P("strike", 0.45), P("last_stand", 0.2), P("god_ki")],
         V(eyeColor="#D8E2FF", auraColor="#C8D8FF", auraSecondary="#FFFFFF", lightning=True, auraScale=1.35),
         "true_ultra_instinct", 180, arena="PLANET_CEREAL"),
    form("ultra_ego", "Ultra Ego", "Ultra Ego", S, "base", 80, 1200000, 90, 10000.0, 1.2, 12.0,
         [P("battle_lust", 0.6), P("destruction", 0.5), P("god_ki"), P("strike", 0.25)],
         V(hairStyle="ascended_spikes", hairColor="#5A2A8C", hairGlow=True, eyeColor="#E040FF", build=2, auraColor="#8A2BE2", auraSecondary="#F0A0FF", auraScale=1.35),
         "ultra_ego", 180, arena="PLANET_CEREAL", required="super_saiyan_blue"),
    form("wrathful", "Wrathful", "Furia", S, "base", 45, 120000, 25, 200.0, 1.1, 4.0,
         [P("last_stand", 0.3), P("strike", 0.25), P("super_armor"), P("rampage", 0.3)],
         V(eyeColor="#F4F4F4", build=2, auraColor="#5CFF6A", auraSecondary="#D8FFD8", auraScale=1.3),
         "wrathful", 170, arena="GLACIER"),
    form("legendary_super_saiyan", "Legendary Super Saiyan", "Super Saiyan Leggendario", S, "wrathful", 47, 500000, 50, 3000.0, 1.15, 13.0,
         [P("strike", 0.4), P("super_armor"), P("intimidate", 0.25), P("last_stand", 0.2), P("rampage", 0.4), P("giant", 1.3)],
         V(hairStyle="super_spikes", hairColor="#C8FF5A", hairGlow=True, eyeColor="#F4F4F4", build=3, auraColor="#66FF44", auraSecondary="#E8FFB0", lightning=True, auraScale=1.5),
         "legendary_super_saiyan", 190, arena="GLACIER"),
    form("super_saiyan_rage", "Super Saiyan Rage", "Super Saiyan Rabbia", ["half_saiyan"], "super_saiyan_2", 25, 500000, 50, 1200.0, 1.25, 10.0,
         [P("last_stand", 0.35), P("ki_power", 0.2)],
         V(hairStyle="ascended_spikes", hairColor="#EAF2FF", hairGlow=True, eyeColor="#2FD8B4", auraColor="#9FD0FF", auraSecondary="#FFFFFF", lightning=True, auraScale=1.3),
         "super_saiyan_rage", 170, arena="RUINED_CITY"),
    form("ultimate", "Ultimate", "Potenziale Liberato", ["half_saiyan"], "base", 50, 350000, 40, 600.0, 1.25, 0.8,
         [P("efficient", 0.5), P("strike", 0.15), P("ki_power", 0.15), P("stamina_free")],
         V(auraColor="#F4F4FF", auraSecondary="#FFFFFF", auraScale=1.1),
         "ultimate", 170, arena="SACRED_WORLD", kaioken=True),
    form("beast", "Beast", "Bestia", ["half_saiyan"], "ultimate", 70, 800000, 70, 5000.0, 1.4, 14.0,
         [P("last_stand", 0.4), P("ki_power", 0.35), P("strike", 0.2)],
         V(hairStyle="beast_spire", hairColor="#ECECF4", hairGlow=True, eyeColor="#E01818", auraColor="#C8A8FF", auraSecondary="#FFFFFF", lightning=True, auraScale=1.4),
         "beast", 190, arena="RED_RIBBON_BASE"),
    form("potential_unlocked", "Potential Unlocked", "Potenziale Sbloccato", ["human"], "base", 10, 15000, 10, 25.0, 1.15, 0.2,
         [P("efficient", 0.8), P("ki_power", 0.1)], V(auraColor="#F2F6FF", auraSecondary="#FFFFFF", auraScale=0.9),
         "potential_unlocked", 140, arena="NAMEK", kaioken=True),
    form("max_power", "Max Power", "Massima Potenza", ["human"], "base", 20, 60000, 18, 40.0, 0.9, 3.0,
         [P("strike", 0.3), P("guard", 0.15), P("speed", -0.1)], V(build=3, auraColor="#FFE7A0", auraSecondary="#FFFFFF", auraScale=1.2),
         "max_power", 140, arena="TOURNAMENT_OF_POWER", kaioken=True),
    form("super_namekian", "Super Namekian", "Super Namecciano", ["namekian"], "base", 10, 30000, 10, 60.0, 1.15, 2.0,
         [P("regeneration", 0.01), P("ki_power", 0.15)], V(auraColor="#F2FFF0", auraSecondary="#FFFFFF", auraScale=1.1),
         "super_namekian", 160, arena="NAMEK", kaioken=True),
    form("great_namek", "Great Namek", "Namecciano Gigante", ["namekian"], "base", 15, 60000, 15, 30.0, 0.8, 3.0,
         [P("giant", 3.5), P("strike", 0.35), P("super_armor")], V(auraColor="#E8FFE0", auraSecondary="#FFFFFF", auraScale=1.0),
         "great_namek", 160, arena="NAMEK"),
    form("orange_piccolo", "Orange Piccolo", "Piccolo Arancione", ["namekian"], "super_namekian", 30, 650000, 60, 1500.0, 1.25, 9.0,
         [P("strike", 0.25), P("guard", 0.2), P("regeneration", 0.01)],
         V(skinColor="#E8862E", build=2, eyeColor="#E81E1E", auraColor="#FF9A3A", auraSecondary="#FFE0B0", auraScale=1.25),
         "orange_piccolo", 170, arena="RED_RIBBON_BASE"),
    form("orange_giant", "Orange Giant", "Gigante Arancione", ["namekian"], "orange_piccolo", 35, 800000, 70, 2000.0, 0.8, 14.0,
         [P("giant", 4.0), P("strike", 0.45), P("super_armor")],
         V(skinColor="#E8862E", build=3, eyeColor="#E81E1E", auraColor="#FF9A3A", auraSecondary="#FFE0B0", auraScale=1.2),
         "orange_giant", 170, arena="RED_RIBBON_BASE"),
    form("frieza_second_form", "Second Form", "Seconda Forma", ["frieza_race"], "base", 10, 20000, 8, 4.0, 1.0, 1.0,
         [P("strike", 0.2), P("guard", 0.1), P("giant", 1.35)], V(featureStyle=1, build=3, auraColor="#D8B0FF", auraSecondary="#FFFFFF"),
         "frieza_second_form", 150, arena="NAMEK"),
    form("frieza_third_form", "Third Form", "Terza Forma", ["frieza_race"], "frieza_second_form", 20, 60000, 15, 12.0, 1.1, 2.0,
         [P("ki_power", 0.2)], V(featureStyle=2, auraColor="#C890FF", auraSecondary="#FFFFFF"),
         "frieza_third_form", 150, arena="NAMEK"),
    form("frieza_final_form", "Final Form", "Forma Finale", ["frieza_race"], "frieza_third_form", 30, 120000, 20, 40.0, 1.3, 2.0,
         [P("speed", 0.15), P("efficient", 0.3)], V(featureStyle=3, auraColor="#B070FF", auraSecondary="#F0E0FF"),
         "frieza_final_form", 160, arena="NAMEK"),
    form("frieza_full_power", "100% Full Power", "100% della Potenza", ["frieza_race"], "frieza_final_form", 35, 250000, 35, 80.0, 1.2, 9.0,
         [P("strike", 0.35), P("ki_power", 0.2)], V(featureStyle=3, build=3, auraColor="#9A50FF", auraSecondary="#F0E0FF", lightning=True, auraScale=1.3),
         "frieza_full_power", 160, arena="NAMEK", stamina=2.0),
    form("golden_frieza", "Golden Frieza", "Golden Freezer", ["frieza_race"], "frieza_final_form", 50, 500000, 50, 1200.0, 1.35, 10.0,
         [P("ki_power", 0.3), P("speed", 0.15)], V(featureStyle=3, skinColor="#E8C030", featureColor="#7A2FC0", auraColor="#FFD43B", auraSecondary="#FFF6C0", auraScale=1.25),
         "golden_frieza", 180, arena="CANYON", stamina=3.0),
    form("black_frieza", "Black Frieza", "Black Freezer", ["frieza_race"], "frieza_final_form", 70, 1200000, 90, 12000.0, 1.5, 8.0,
         [P("intimidate", 0.3), P("strike", 0.4), P("ki_power", 0.4), P("guard", 0.2)],
         V(featureStyle=3, skinColor="#1C1A24", featureColor="#A040E0", eyeColor="#E03030", auraColor="#6A1A9A", auraSecondary="#E0A0FF", lightning=True, auraScale=1.35),
         "black_frieza", 190, arena="PLANET_CEREAL", required="golden_frieza"),
    form("super_buu", "Super Buu", "Super Bu", ["majin"], "base", 10, 250000, 30, 500.0, 1.3, 2.0,
         [P("regeneration", 0.015), P("ki_power", 0.2)], V(build=0, skinColor="#D86A9E", eyeColor="#C81E1E", auraColor="#E070C0", auraSecondary="#FFD0F0", auraScale=1.2),
         "super_buu", 170, arena="LOOKOUT"),
    form("super_buu_gotenks", "Super Buu (Gotenks absorbed)", "Super Bu (con Gotenks)", ["majin"], "super_buu", 15, 400000, 40, 900.0, 1.35, 3.0,
         [P("strike", 0.2), P("ki_power", 0.25), P("regeneration", 0.015)],
         V(build=1, skinColor="#D86A9E", eyeColor="#C81E1E", auraColor="#E070C0", auraSecondary="#FFD0F0", auraScale=1.25),
         "super_buu_gotenks", 160, unlock="absorb", absorptions=1),
    form("super_buu_gohan", "Super Buu (Gohan absorbed)", "Super Bu (con Gohan)", ["majin"], "super_buu_gotenks", 20, 600000, 55, 1600.0, 1.4, 4.0,
         [P("strike", 0.3), P("ki_power", 0.35), P("regeneration", 0.02)],
         V(build=2, skinColor="#D86A9E", eyeColor="#C81E1E", auraColor="#D060B8", auraSecondary="#FFD0F0", lightning=True, auraScale=1.3),
         "super_buu_gohan", 170, unlock="absorb", absorptions=2),
    form("kid_buu", "Kid Buu", "Bu Puro", ["majin"], "super_buu", 30, 700000, 60, 1400.0, 1.5, 3.0,
         [P("regeneration", 0.03), P("speed", 0.2), P("ki_power", 0.2), P("giant", 0.8)],
         V(build=0, skinColor="#F5A6C8", eyeColor="#141414", auraColor="#FF90D0", auraSecondary="#FFFFFF", auraScale=1.2),
         "kid_buu", 170, arena="SACRED_WORLD"),
    form("semi_perfect", "Semi-Perfect Form", "Forma Semi-Perfetta", ["bio_android"], "base", 10, 60000, 12, 8.0, 1.0, 1.0,
         [P("strike", 0.2), P("regeneration", 0.01), P("giant", 1.25)], V(build=3, skinColor="#6FAE3C", auraColor="#B8FF8A", auraSecondary="#FFFFFF"),
         "semi_perfect", 160, unlock="absorb", absorptions=1),
    form("perfect", "Perfect Form", "Forma Perfetta", ["bio_android"], "semi_perfect", 20, 150000, 25, 40.0, 1.3, 1.5,
         [P("efficient", 0.4), P("regeneration", 0.015), P("ki_power", 0.15)], V(build=1, skinColor="#7FCC58", auraColor="#E8FFD0", auraSecondary="#FFFFFF"),
         "perfect", 170, unlock="absorb", absorptions=2),
    form("super_perfect", "Super Perfect Form", "Forma Super Perfetta", ["bio_android"], "perfect", 30, 300000, 40, 120.0, 1.4, 4.0,
         [P("regeneration", 0.02), P("ki_power", 0.3), P("strike", 0.15)],
         V(build=1, skinColor="#7FCC58", auraColor="#FFD43B", auraSecondary="#FFF6C0", lightning=True, auraScale=1.3),
         "super_perfect", 170, arena="CELL_RING"),
    form("fused_zamasu", "Fused Zamasu", "Zamasu Fuso", ["kaioshin"], "base", 20, 500000, 50, 2500.0, 1.25, 9.0,
         [P("god_ki"), P("regeneration", 0.02), P("ki_power", 0.3)],
         V(skinColor="#5FB860", hairColor="#F2F2F2", eyeColor="#3A2A6A", auraColor="#C06AFF", auraSecondary="#FFE0FF", auraScale=1.3),
         "fused_zamasu", 180, arena="RUINED_CITY"),
    form("half_corrupted_zamasu", "Half-Corrupted Zamasu", "Zamasu Semi-Corrotto", ["kaioshin"], "fused_zamasu", 30, 900000, 75, 6000.0, 1.2, 12.0,
         [P("god_ki"), P("regeneration", 0.03), P("intimidate", 0.2), P("ki_power", 0.4)],
         V(skinColor="#6A3A8C", hairColor="#F2F2F2", eyeColor="#E03030", auraColor="#A050E0", auraSecondary="#FFC0FF", lightning=True, auraScale=1.4),
         "half_corrupted_zamasu", 180, arena="RUINED_CITY"),
    form("third_eye", "Third Eye", "Terzo Occhio", ["demon"], "base", 20, 250000, 30, 300.0, 1.2, 5.0,
         [P("ki_power", 0.3), P("intimidate", 0.15)], V(eyeColor="#FF3A3A", auraColor="#5A1A7A", auraSecondary="#E080FF", lightning=True, auraScale=1.25),
         "third_eye", 170, arena="DEMON_REALM"),
]

PASSIVES = {
    "speed": ("Speed %s", "Velocità %s"),
    "strike": ("Strike power %s", "Potenza dei colpi %s"),
    "ki_power": ("Ki power %s", "Potenza del Ki %s"),
    "guard": ("Damage taken -%s", "Danni subiti -%s"),
    "efficient": ("Ki drain -%s", "Consumo di Ki -%s"),
    "auto_dodge": ("Automatic dodge %s", "Schivata automatica %s"),
    "god_ki": ("God Ki: cannot be sensed", "Ki divino: non percepibile"),
    "giant": ("Giant body x%s", "Corpo gigante x%s"),
    "rampage": ("Rampage until %s mastery", "Furia incontrollata fino al %s di padronanza"),
    "regeneration": ("Regenerates %s health/s", "Rigenera %s di salute/s"),
    "battle_lust": ("Grows stronger when hit, up to %s", "Più forte quando viene colpito, fino a %s"),
    "last_stand": ("Up to %s power at low health", "Fino a %s di potenza con poca salute"),
    "reflect": ("Deflects ki blasts %s", "Devia i colpi di Ki %s"),
    "super_armor": ("Unstoppable: no knockback", "Inarrestabile: nessun contraccolpo"),
    "destruction": ("Destruction: hits burn %s of the target's Ki", "Distruzione: i colpi bruciano Ki al bersaglio (%s)"),
    "stamina_free": ("No stamina drain", "Nessun consumo di resistenza"),
    "intimidate": ("Weaker foes deal -%s damage", "I nemici più deboli infliggono -%s danni"),
    "ki_regen": ("Ki regeneration %s", "Rigenerazione del Ki %s"),
    "tail_bound": ("Ends if the tail is lost", "Termina se si perde la coda"),
    "moon_bound": ("Needs the full moon", "Richiede la luna piena"),
}

ARENAS = {
    "tournament_of_power": ("Tournament of Power", "Torneo del Potere"),
    "beerus_planet": ("Beerus's Planet", "Pianeta di Beerus"),
    "sacred_world": ("Sacred World of the Kai", "Mondo Sacro dei Kaioshin"),
    "demon_realm": ("Demon Realm", "Regno dei Demoni"),
    "red_ribbon_base": ("Red Ribbon Army Base", "Base del Red Ribbon"),
    "planet_cereal": ("Planet Cereal", "Pianeta Cereal"),
    "ruined_city": ("Future Trunks's Ruined City", "Città in rovina del futuro di Trunks"),
    "glacier": ("Frozen Glacier", "Ghiacciaio"),
    "lookout": ("Kami's Lookout", "Palazzo del Supremo"),
}


def merge_lang(path, entries):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    data.update(entries)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def main():
    os.makedirs(FORMS, exist_ok=True)
    en, it = {}, {}
    for fid, name_en, name_it, data in FORM_LIST:
        with open(os.path.join(FORMS, fid + ".json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write("\n")
        en["form.dbtenkaichi." + fid] = name_en
        it["form.dbtenkaichi." + fid] = name_it
    for key, (pe, pi) in PASSIVES.items():
        en["form_passive.dbtenkaichi." + key] = pe
        it["form_passive.dbtenkaichi." + key] = pi
    for key, (ae, ai) in ARENAS.items():
        en["arena.dbtenkaichi." + key] = ae
        it["arena.dbtenkaichi." + key] = ai
    merge_lang(os.path.join(LANG, "en_us.json"), en)
    merge_lang(os.path.join(LANG, "it_it.json"), it)
    print("forms", len(FORM_LIST))


if __name__ == "__main__":
    sys.exit(main())
