package com.dbtenkaichi.server;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.combat.TenkaichiDamage;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.entity.RaiderEntity;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.registry.ModItems;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.world.DestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class SmokeTest {
    private static final boolean ENABLED = Boolean.getBoolean("dbtenkaichi.smokeTest");
    private static final List<String> FAILURES = new ArrayList<>();
    private static final EnumSet<com.dbtenkaichi.entity.ai.RaiderBrain.State> STATES = EnumSet.noneOf(com.dbtenkaichi.entity.ai.RaiderBrain.State.class);
    private static int tick = -40;
    private static RaiderEntity raider;
    private static MasterEntity master;
    private static Zombie dummy;
    private static float dummyHealth;
    private static boolean done;
    private static int tenkaichiHits;

    private SmokeTest() {
    }

    public static void tick(MinecraftServer server) {
        if (!ENABLED || done) {
            return;
        }
        tick++;
        if (tick < 0) {
            return;
        }
        ServerLevel level = server.overworld();
        try {
            if (tick == 0) {
                staticChecks(level);
                storyChecks(level);
                spawn(level);
            } else if (tick > 0 && tick < 400) {
                observe(level);
            } else if (tick == 400) {
                finish(server);
            }
        } catch (RuntimeException e) {
            FAILURES.add("exception at tick " + tick + ": " + e);
            Tenkaichi.LOG.error("[DBT-SMOKE] exception", e);
            finish(server);
        }
    }

    private static void check(boolean condition, String message) {
        if (condition) {
            Tenkaichi.LOG.info("[DBT-SMOKE] PASS {}", message);
        } else {
            FAILURES.add(message);
            Tenkaichi.LOG.error("[DBT-SMOKE] FAIL {}", message);
        }
    }

    private static void staticChecks(ServerLevel level) {
        check(FormRegistry.get("dbtenkaichi:super_saiyan", false) != null, "super_saiyan form loaded from datapack");
        check(FormRegistry.get("dbtenkaichi:raider_rage", false) != null, "raider_rage form loaded from datapack");
        for (ResourceKey<DamageType> key : List.of(TenkaichiDamage.MARTIAL_ARTS, TenkaichiDamage.KI_BLAST, TenkaichiDamage.KI_BEAM, TenkaichiDamage.KI_EXPLOSION)) {
            check(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(key).isPresent(), "damage type " + key.identifier());
        }
        check(ModSounds.NAMES.size() > 20 && ModSounds.PUNCH_LIGHT.get() != null, "sound events registered");
        check(ModItems.SENZU_BEAN.get() != null && ModItems.TAB.get() != null, "items and creative tab registered");
        check(ModEntities.RAIDER.get() != null && ModEntities.KI_BEAM.get() != null, "entity types registered");
        ServerLevel realm = level.getServer().getLevel(com.dbtenkaichi.training.TrialManager.REALM);
        check(realm != null, "training realm dimension loaded");
        if (realm != null) {
            for (com.dbtenkaichi.training.Arena arena : com.dbtenkaichi.training.Arena.values()) {
                BlockPos center = new BlockPos(arena.ordinal() * 200, 100, -4000);
                arena.build(realm, center);
                check(!realm.getBlockState(center.below()).isAir(), "arena " + arena + " built in the training realm");
            }
        }
        var sites = com.dbtenkaichi.world.Landmarks.sites();
        check(sites.size() == 3, "three landmarks registered " + sites);
        sites.forEach((id, pos) -> {
            level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            check(!level.getBlockState(pos.below()).isAir(), "landmark " + id + " has ground under its master at " + pos);
        });
        check(com.dbtenkaichi.training.Lessons.forMaster(com.dbtenkaichi.entity.MasterKind.ROSHI).stream().anyMatch(l -> l.skill().equals(Techniques.KAMEHAMEHA.id())), "Master Roshi teaches the Kamehameha");
        check(com.dbtenkaichi.form.KaiokenSystem.SHOP_INDEX >= com.dbtenkaichi.ki.Techniques.UNLOCKS.size() && com.dbtenkaichi.form.KaiokenSystem.PRICE > 0, "Kaioken can be bought from the technique list");
        FormDefinition ssj = FormRegistry.get("dbtenkaichi:super_saiyan", false);
        check(ssj != null && ssj.kaiokenCompatible, "Super Saiyan accepts the Kaioken");
        check(com.dbtenkaichi.form.KaiokenSystem.multiplier(5) == 20.0 && com.dbtenkaichi.form.KaiokenSystem.fromFlags(3 << com.dbtenkaichi.form.KaiokenSystem.FLAG_SHIFT) == 3, "Kaioken levels and flag encoding");
    }

    private static void storyChecks(ServerLevel level) {
        java.util.Collection<FormDefinition> forms = FormRegistry.all(false);
        check(forms.stream().filter(f -> !f.id.startsWith("dbtenkaichi:raider")).count() >= 43, "all canon forms loaded (" + forms.size() + ")");
        for (FormDefinition form : forms) {
            boolean parentOk = form.parent.equals(FormRegistry.BASE) || FormRegistry.get(form.parent, false) != null;
            boolean required = form.requirements.requiredForm == null || form.requirements.requiredForm.isEmpty() || FormRegistry.get(form.requirements.requiredForm, false) != null;
            boolean passives = form.passives.stream().allMatch(p -> p.type != null);
            if (!parentOk || !required || !passives) {
                check(false, "form " + form.id + " links (parent " + parentOk + ", required " + required + ", passives " + passives + ")");
            }
        }
        for (com.dbtenkaichi.race.Race race : com.dbtenkaichi.race.Race.values()) {
            long count = FormRegistry.chainFor(race, false).stream().filter(f -> !f.id.startsWith("dbtenkaichi:raider")).count();
            check(race == com.dbtenkaichi.race.Race.ANDROID || count > 0, race.id() + " has canon forms (" + count + ")");
        }
        net.minecraft.server.MinecraftServer server = level.getServer();
        ServerLevel other = com.dbtenkaichi.world.OtherWorld.level(server);
        check(other != null, "Other World dimension loaded");
        if (other != null) {
            check(!other.getBlockState(com.dbtenkaichi.world.OtherWorld.CHECK_IN.below()).isAir(), "check-in station built in the Other World");
            check(!other.getBlockState(com.dbtenkaichi.world.OtherWorld.roadPoint(450).below()).isAir(), "Snake Way road built");
            BlockPos planet = com.dbtenkaichi.world.OtherWorld.planet();
            check(!other.getBlockState(planet.below()).isAir() && !other.getBlockState(planet.offset(5, -1, 3)).isAir() && !other.getBlockState(planet.offset(10, -1, 0)).isAir(), "King Kai's planet built with a flat top under the house and path");
        }
        java.util.Set<String> sites = new java.util.HashSet<>();
        for (com.dbtenkaichi.mission.Mission mission : com.dbtenkaichi.mission.Missions.all().values()) {
            if (!mission.next.isEmpty()) {
                check(com.dbtenkaichi.mission.Missions.get(mission.next) != null, mission.id + " continues into " + mission.next);
            }
            for (com.dbtenkaichi.mission.Mission.Stage stage : mission.stages) {
                if (stage.kind == com.dbtenkaichi.mission.Mission.Kind.SCENE) {
                    com.dbtenkaichi.story.StoryScene scene = com.dbtenkaichi.story.Scenes.get(stage.target);
                    if (scene == null) {
                        check(false, mission.id + " scene " + stage.target + " exists");
                        continue;
                    }
                    sites.add(scene.site);
                    for (com.dbtenkaichi.story.StoryScene.ActorSpec actor : scene.actors) {
                        if (com.dbtenkaichi.story.StoryCharacters.get(actor.character()) == null) {
                            check(false, "scene " + scene.id + " actor " + actor.character() + " exists");
                        }
                    }
                } else if (!stage.site.isEmpty()) {
                    sites.add(stage.site);
                }
                for (com.dbtenkaichi.mission.Mission.Fighter fighter : stage.fighters) {
                    if (com.dbtenkaichi.story.StoryCharacters.get(fighter.character()) == null) {
                        check(false, mission.id + " fighter " + fighter.character() + " exists");
                    }
                }
            }
        }
        for (String site : sites) {
            com.dbtenkaichi.world.StoryWorld.Site found = com.dbtenkaichi.world.StoryWorld.site(server, site);
            check(found != null && !found.level().getBlockState(found.pos().below()).isAir(), "story site " + site + " exists with ground");
        }
        int spawned = 0;
        BlockPos spawn = level.getRespawnData().pos();
        for (com.dbtenkaichi.story.StoryCharacters.Character character : com.dbtenkaichi.story.StoryCharacters.all().values()) {
            com.dbtenkaichi.entity.StoryNpc npc = com.dbtenkaichi.entity.StoryNpc.spawn(level, character, Vec3.atBottomCenterOf(spawn).add(4, 1, 4), com.dbtenkaichi.entity.StoryNpc.ROLE_ACTOR, null, 1.0);
            if (npc != null && npc.isAlive()) {
                spawned++;
                if ("vegeta".equals(character.id())) {
                    npc.setStoryForm(com.dbtenkaichi.race.TailSystem.GREAT_APE);
                    check(npc.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE) > 2.5, "story Vegeta grows into a giant Great Ape");
                }
                npc.discard();
            }
        }
        check(spawned == com.dbtenkaichi.story.StoryCharacters.all().size(), "every story character can be spawned (" + spawned + ")");
        com.dbtenkaichi.entity.PowerBallEntity ball = ModEntities.POWER_BALL.get().create(level, EntitySpawnReason.COMMAND);
        check(ball != null, "Power Ball entity can be created");
        check(ModItems.SCOUTER.get() != null && ModItems.CANDY.get() != null, "scouter and Majin candy items registered");
        check(com.dbtenkaichi.mission.Missions.all().size() >= 16, "story and side missions registered (" + com.dbtenkaichi.mission.Missions.all().size() + ")");
    }

    private static void spawn(ServerLevel level) {
        level.setChunkForced(0, 0, true);
        level.getChunk(0, 0);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0);
        BlockPos base = new BlockPos(0, y + 1, 0);
        master = ModEntities.MASTER.get().spawn(level, base.offset(6, 0, 0), EntitySpawnReason.COMMAND);
        raider = ModEntities.RAIDER.get().spawn(level, base, EntitySpawnReason.COMMAND);
        dummy = EntityTypes.HUSK.spawn(level, base.offset(3, 0, 0), EntitySpawnReason.COMMAND);
        if (dummy != null) {
            dummy.setPersistenceRequired();
            dummy.setNoAi(true);
        }
        check(master != null && raider != null && dummy != null, "spawned master, raider and target dummy");
        if (master != null) {
            master.setKind(com.dbtenkaichi.entity.MasterKind.ROSHI);
            check(master.kind() == com.dbtenkaichi.entity.MasterKind.ROSHI, "master kind is stored");
            master.setKind(com.dbtenkaichi.entity.MasterKind.MASTER);
        }
        if (raider != null && dummy != null) {
            raider.setTarget(dummy);
            dummyHealth = dummy.getHealth();
        }
    }

    private static void observe(ServerLevel level) {
        if (raider != null && raider.isAlive()) {
            STATES.add(raider.brain().state());
            if (dummy != null && dummy.isAlive()) {
                raider.setTarget(dummy);
                DamageSource last = dummy.getLastDamageSource();
                if (last != null && TenkaichiDamage.isTenkaichi(last)) {
                    tenkaichiHits++;
                }
            }
        }
        if (tick == 120 && raider != null && master != null) {
            KiBeamEntity beam = KiBeamEntity.create(level, master, Techniques.KAMEHAMEHA, 0.6);
            master.lookAtPoint(raider.getBoundingBox().getCenter());
            beam.setDirection(raider.getBoundingBox().getCenter().subtract(beam.position()));
            level.addFreshEntity(beam);
            check(beam.isAlive(), "kamehameha beam entity spawned");
            for (com.dbtenkaichi.ki.Techniques.Unlock unlock : com.dbtenkaichi.ki.Techniques.UNLOCKS) {
                com.dbtenkaichi.ki.Technique technique = unlock.technique();
                net.minecraft.world.entity.Entity spawned = technique.type() == com.dbtenkaichi.ki.Technique.Type.BEAM
                        ? KiBeamEntity.create(level, master, technique, 0.5)
                        : com.dbtenkaichi.entity.KiBlastEntity.create(level, master, technique, technique.glowColor(), -1, 1.0);
                level.addFreshEntity(spawned);
                check(spawned.isAlive(), technique.id() + " entity spawned");
                spawned.discard();
            }
            for (String form : List.of("dbtenkaichi:super_saiyan_2", "dbtenkaichi:super_saiyan_3")) {
                check(com.dbtenkaichi.form.FormRegistry.get(form, false) != null, form + " form loaded from datapack");
            }
        }
        if (tick == 200) {
            Vec3 sky = new Vec3(0, level.getMaxY() - 10, 0);
            DestructionManager.explode(level, sky, 3.0, null, 0.0, 0.0);
            check(true, "explosion in open air handled");
        }
        if (tick == 240) {
            safeZones(level);
        }
        if (tick == 300) {
            craterCheck(level);
            migration(level.getServer());
        }
        if (tick == 340) {
            residents(level);
        }
    }

    private static BlockPos craterCenter;

    private static void safeZones(ServerLevel level) {
        var zones = com.dbtenkaichi.world.SafeZones.zones();
        check(zones.size() == 2, "landmark safe zones registered " + zones.size());
        BlockPos kame = com.dbtenkaichi.world.Landmarks.sites().get(com.dbtenkaichi.world.Landmarks.KAME_HOUSE);
        BlockPos korin = com.dbtenkaichi.world.Landmarks.sites().get(com.dbtenkaichi.world.Landmarks.KORIN_TOWER);
        if (kame == null || korin == null) {
            check(false, "landmark sites available for safe zone checks");
            return;
        }
        level.getChunk(kame.getX() >> 4, kame.getZ() >> 4);
        level.getChunk(korin.getX() >> 4, korin.getZ() >> 4);
        BlockPos floor = kame.below();
        var before = level.getBlockState(floor);
        check(com.dbtenkaichi.world.SafeZones.isProtected(level, floor) && com.dbtenkaichi.world.SafeZones.isProtected(level, korin.below()), "landmark blocks are inside a safe zone");
        DestructionManager.explode(level, Vec3.atCenterOf(floor), 8.0, null, 0.0, 0.0);
        level.explode(null, floor.getX() + 0.5, floor.getY() + 1.0, floor.getZ() + 0.5, 4.0f, net.minecraft.world.level.Level.ExplosionInteraction.TNT);
        DestructionManager.explode(level, Vec3.atCenterOf(korin.below()), 8.0, null, 0.0, 0.0);
        check(level.getBlockState(floor) == before && !level.getBlockState(korin.below()).isAir(), "Tenkaichi and vanilla explosions cannot damage landmarks");
        var zombie = EntityTypes.ZOMBIE.spawn(level, kame.offset(2, 0, 0), EntitySpawnReason.COMMAND);
        check(zombie == null || level.getEntity(zombie.getUUID()) == null, "mobs cannot spawn inside a landmark safe zone");
        level.getChunk(40 >> 4, 40 >> 4);
        craterCenter = new BlockPos(40, level.getMaxY() - 40, 40);
        for (BlockPos pos : BlockPos.betweenClosed(craterCenter.offset(-9, -8, -9), craterCenter.offset(9, 0, 9))) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 2);
        }
        check(!com.dbtenkaichi.world.SafeZones.isProtected(level, craterCenter), "crater test site is outside safe zones");
        DestructionManager.crater(level, Vec3.atCenterOf(craterCenter), Techniques.BIG_BANG_ATTACK.explosionRadius(), null, 0.0, 1.0);
        check(DestructionManager.battlePowerScale(null) == 1.0 && DestructionManager.scaledRadius(raider, 1000.0) <= com.dbtenkaichi.config.ConfigManager.gameplay().maxExplosionRadius, "explosion size is clamped");
        check(raider == null || DestructionManager.battlePowerScale(raider) >= 0.7 && DestructionManager.battlePowerScale(raider) <= 2.8, "battle power explosion scale stays in range");
    }

    private static void craterCheck(ServerLevel level) {
        if (craterCenter == null) {
            return;
        }
        int air = 0;
        for (BlockPos pos : BlockPos.betweenClosed(craterCenter.offset(-3, -3, -3), craterCenter.offset(3, 0, 3))) {
            if (level.getBlockState(pos).isAir()) {
                air++;
            }
        }
        check(air > 60 && DestructionManager.pendingJobs() == 0, "Big Bang Attack carved a crater (" + air + " air blocks below the impact)");
    }

    private static void migration(MinecraftServer server) {
        ServerLevel level = server.overworld();
        BlockPos kame = com.dbtenkaichi.world.Landmarks.sites().get(com.dbtenkaichi.world.Landmarks.KAME_HOUSE);
        BlockPos korin = com.dbtenkaichi.world.Landmarks.sites().get(com.dbtenkaichi.world.Landmarks.KORIN_TOWER);
        if (kame == null || korin == null) {
            return;
        }
        for (BlockPos site : com.dbtenkaichi.world.Landmarks.sites().values()) {
            level.setChunkForced(site.getX() >> 4, site.getZ() >> 4, true);
        }
        var floor = level.getBlockState(kame.below());
        level.setBlock(kame.below(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(korin.below(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(kame.above(3), net.minecraft.world.level.block.Blocks.COBBLESTONE.defaultBlockState(), 3);
        int generation = com.dbtenkaichi.world.Landmarks.generation();
        int rebuilt = com.dbtenkaichi.world.Landmarks.rebuildNow(server);
        check(rebuilt == 3 && com.dbtenkaichi.world.Landmarks.generation() == generation + 1, "landmark rebuild ran for all structures (generation " + com.dbtenkaichi.world.Landmarks.generation() + ")");
        check(level.getBlockState(kame.below()) == floor && !level.getBlockState(korin.below()).isAir(), "damaged landmarks were rebuilt");
        check(level.getBlockState(kame.above(3)).isAir(), "debris inside a landmark was cleaned up");
        check(com.dbtenkaichi.world.SafeZones.zones().size() == 2, "safe zones registered again after the rebuild");
    }

    private static void residents(ServerLevel level) {
        for (var entry : com.dbtenkaichi.world.Landmarks.sites().entrySet()) {
            BlockPos pos = entry.getValue();
            List<MasterEntity> masters = level.getEntitiesOfClass(MasterEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(30), m -> m.isAlive() && !m.isStaleLandmarkMaster());
            check(masters.size() == 1, "landmark " + entry.getKey() + " has exactly one master after migration (" + masters.size() + ")");
        }
    }

    private static void finish(MinecraftServer server) {
        done = true;
        if (dummy != null) {
            DamageSource last = dummy.getLastDamageSource();
            boolean byTenkaichi = last != null && TenkaichiDamage.isTenkaichi(last) || tenkaichiHits > 0;
            check(byTenkaichi && (dummy.isDeadOrDying() || dummy.getHealth() < dummyHealth), "raider AI damaged its target with Tenkaichi attacks (states visited " + STATES + ")");
        }
        check(STATES.size() >= 2, "raider brain changed state at least once");
        if (FAILURES.isEmpty()) {
            Tenkaichi.LOG.info("[DBT-SMOKE] RESULT PASS");
        } else {
            Tenkaichi.LOG.error("[DBT-SMOKE] RESULT FAIL {}", FAILURES);
        }
        server.overworld().setChunkForced(0, 0, false);
        for (BlockPos site : com.dbtenkaichi.world.Landmarks.sites().values()) {
            server.overworld().setChunkForced(site.getX() >> 4, site.getZ() >> 4, false);
        }
        server.halt(false);
    }
}
