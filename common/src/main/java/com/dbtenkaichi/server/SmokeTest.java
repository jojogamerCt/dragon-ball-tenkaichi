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
        server.halt(false);
    }
}
