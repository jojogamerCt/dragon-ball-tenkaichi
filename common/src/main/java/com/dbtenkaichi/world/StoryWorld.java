package com.dbtenkaichi.world;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.training.Structures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class StoryWorld {
    private static final int SITE_GENERATION = 2;
    private static MinecraftServer loaded;
    private static final Properties PROPS = new Properties();

    private StoryWorld() {
    }

    public record Site(ServerLevel level, BlockPos pos) {
    }

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("dbtenkaichi_story.properties");
    }

    private static void load(MinecraftServer server) {
        if (loaded == server) {
            return;
        }
        loaded = server;
        PROPS.clear();
        Path path = file(server);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                PROPS.load(reader);
            } catch (IOException e) {
                Tenkaichi.LOG.warn("Could not read story sites", e);
            }
        }
    }

    private static void save(MinecraftServer server) {
        try (Writer writer = Files.newBufferedWriter(file(server))) {
            PROPS.store(writer, "Dragon Block: Tenkaichi story sites");
        } catch (IOException e) {
            Tenkaichi.LOG.warn("Could not save story sites", e);
        }
    }

    public static Site site(MinecraftServer server, String key) {
        load(server);
        ServerLevel overworld = server.overworld();
        switch (key) {
            case "kame_house" -> {
                BlockPos pos = Landmarks.sites().get(Landmarks.KAME_HOUSE);
                return pos == null ? null : new Site(overworld, pos);
            }
            case "korin_tower" -> {
                BlockPos pos = Landmarks.sites().get(Landmarks.KORIN_TOWER);
                return pos == null ? null : new Site(overworld, pos);
            }
            case "kami_lookout" -> {
                BlockPos pos = Landmarks.sites().get(Landmarks.KAMI_LOOKOUT);
                return pos == null ? null : new Site(overworld, pos);
            }
            case "check_in" -> {
                ServerLevel other = OtherWorld.level(server);
                return other == null ? null : new Site(other, OtherWorld.CHECK_IN);
            }
            case "king_kai_planet" -> {
                ServerLevel other = OtherWorld.level(server);
                return other == null ? null : new Site(other, OtherWorld.planet());
            }
            case "raditz_field", "battlefield" -> {
                String prop = key + "." + SITE_GENERATION;
                BlockPos pos = parse(PROPS.getProperty(prop));
                if (pos == null) {
                    boolean raditz = "raditz_field".equals(key);
                    BlockPos avoid = parse(PROPS.getProperty((raditz ? "battlefield." : "raditz_field.") + SITE_GENERATION));
                    pos = findSite(overworld, raditz ? 180 : 300, raditz ? 0.6 : 2.5, raditz ? 30 : 50, avoid);
                    if (raditz) {
                        buildRaditzField(overworld, pos);
                    } else {
                        buildBattlefield(overworld, pos);
                        fillBiome(server, pos, 58, "dbtenkaichi:wasteland");
                    }
                    PROPS.setProperty(prop, pos.getX() + "," + pos.getY() + "," + pos.getZ());
                    save(server);
                    Tenkaichi.LOG.info("Story site {} placed at {}", key, pos.toShortString());
                }
                return new Site(overworld, pos);
            }
            default -> {
                return null;
            }
        }
    }

    private static boolean unsuitable(ServerLevel level, int x, int z) {
        var biome = level.getUncachedNoiseBiome(x >> 2, level.getSeaLevel() >> 2, z >> 2);
        return biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN) || biome.is(net.minecraft.tags.BiomeTags.IS_RIVER) || biome.is(net.minecraft.tags.BiomeTags.IS_BEACH)
                || biome.is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN);
    }

    private static int score(ServerLevel level, int cx, int cz, int radius) {
        var generator = level.getChunkSource().getGenerator();
        var state = level.getChunkSource().randomState();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int sea = level.getSeaLevel();
        for (int dx = -radius; dx <= radius; dx += 6) {
            for (int dz = -radius; dz <= radius; dz += 6) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, state);
                int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, state);
                if (surface != floor || floor <= sea + 1) {
                    return -1;
                }
                min = Math.min(min, floor);
                max = Math.max(max, floor);
            }
        }
        for (int[] c : new int[][]{{0, 0}, {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius}}) {
            if (unsuitable(level, cx + c[0], cz + c[1])) {
                return -1;
            }
        }
        return max - min;
    }

    private static BlockPos findSite(ServerLevel level, int distance, double angle, int radius, BlockPos avoid) {
        BlockPos spawn = level.getRespawnData().pos();
        var generator = level.getChunkSource().getGenerator();
        var state = level.getChunkSource().randomState();
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int ring = 0; ring < 10; ring++) {
            int d = distance + ring * 70;
            for (int step = 0; step < 18; step++) {
                double a = angle + (step % 2 == 0 ? 1 : -1) * ((step + 1) / 2) * Math.PI / 9.0;
                int x = spawn.getX() + (int) Math.round(Math.cos(a) * d);
                int z = spawn.getZ() + (int) Math.round(Math.sin(a) * d);
                BlockPos candidate = new BlockPos(x, 0, z);
                if (avoid != null && avoid.distSqr(new BlockPos(x, avoid.getY(), z)) < 200 * 200) {
                    continue;
                }
                boolean nearLandmark = false;
                for (BlockPos landmark : Landmarks.sites().values()) {
                    if (landmark.distSqr(new BlockPos(x, landmark.getY(), z)) < 180 * 180) {
                        nearLandmark = true;
                    }
                }
                if (nearLandmark) {
                    continue;
                }
                int score = score(level, x, z, radius);
                if (score < 0) {
                    continue;
                }
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
                if (score <= 4) {
                    ring = 10;
                    break;
                }
            }
        }
        if (best == null) {
            int x = spawn.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = spawn.getZ() + (int) Math.round(Math.sin(angle) * distance);
            best = new BlockPos(x, 0, z);
            Tenkaichi.LOG.warn("No dry flat story site found near {}, using {}", spawn.toShortString(), best.toShortString());
        }
        int y = generator.getBaseHeight(best.getX(), best.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, level, state);
        return new BlockPos(best.getX(), Math.max(level.getSeaLevel() + 2, y), best.getZ());
    }

    private static void fillBiome(MinecraftServer server, BlockPos center, int radius, String biome) {
        var source = server.createCommandSourceStack().withSuppressedOutput();
        int y0 = center.getY() - 40;
        int y1 = center.getY() + 79;
        for (int cx = (center.getX() - radius) >> 4; cx <= (center.getX() + radius) >> 4; cx++) {
            for (int cz = (center.getZ() - radius) >> 4; cz <= (center.getZ() + radius) >> 4; cz++) {
                int x = cx << 4;
                int z = cz << 4;
                server.getCommands().performPrefixedCommand(source, "fillbiome " + x + " " + y0 + " " + z + " " + (x + 15) + " " + y1 + " " + (z + 15) + " " + biome);
            }
        }
    }

    private static BlockPos parse(String value) {
        if (value == null) {
            return null;
        }
        String[] parts = value.split(",");
        try {
            return new BlockPos(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void flatten(ServerLevel level, BlockPos center, int radius, RandomSource random, boolean wasteland) {
        int y0 = center.getY();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius) {
                    continue;
                }
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                level.getChunk(x >> 4, z >> 4);
                double edge = Math.max(0.0, (d - radius * 0.7) / (radius * 0.3));
                int target = y0 + (int) Math.round(edge * (level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1 - y0) * 0.6);
                for (int y = target + 1; y <= target + 24; y++) {
                    Structures.set(level, new BlockPos(x, y, z), Blocks.AIR);
                }
                for (int y = target - 3; y <= target; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (y == target) {
                        Structures.set(level, pos, wasteland ? (random.nextInt(4) == 0 ? Blocks.GRAVEL : random.nextInt(3) == 0 ? Blocks.STONE : Blocks.COARSE_DIRT) : Blocks.GRASS_BLOCK);
                    } else {
                        Structures.set(level, pos, wasteland ? Blocks.STONE : Blocks.DIRT);
                    }
                }
            }
        }
    }

    private static void pod(ServerLevel level, BlockPos crater) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 5.5) {
                    continue;
                }
                int depth = (int) Math.round((5.5 - d) * 0.5);
                for (int y = 0; y < depth; y++) {
                    Structures.set(level, crater.offset(dx, -1 - y, dz), Blocks.AIR);
                }
                Structures.set(level, crater.offset(dx, -1 - depth, dz), d < 3 ? Blocks.BLACKSTONE : Blocks.COARSE_DIRT);
            }
        }
        BlockPos pod = crater.below(1);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx * dx + (dy - 1) * (dy - 1) + dz * dz <= 4.5) {
                        Structures.set(level, pod.offset(dx, dy, dz), Blocks.CONCRETE.white());
                    }
                }
            }
        }
        Structures.set(level, pod.offset(0, 2, -2), Blocks.STAINED_GLASS.red());
        Structures.set(level, pod.offset(1, 2, -2), Blocks.STAINED_GLASS.red());
        Structures.set(level, pod.offset(-1, 2, -2), Blocks.STAINED_GLASS.red());
        Structures.set(level, pod.offset(0, 1, -2), Blocks.STAINED_GLASS.red());
    }

    private static void buildRaditzField(ServerLevel level, BlockPos center) {
        RandomSource random = RandomSource.create(center.asLong());
        flatten(level, center, 30, random, false);
        pod(level, center.offset(0, 0, 11));
        for (int i = 0; i < 4; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            Structures.tree(level, center.offset((int) Math.round(Math.cos(a) * 33), 0, (int) Math.round(Math.sin(a) * 33)));
        }
    }

    private static net.minecraft.world.level.block.Block wastelandGround(RandomSource random) {
        int r = random.nextInt(20);
        if (r < 7) {
            return Blocks.PACKED_MUD;
        }
        if (r < 11) {
            return Blocks.SANDSTONE;
        }
        if (r < 15) {
            return Blocks.COARSE_DIRT;
        }
        if (r < 18) {
            return Blocks.TERRACOTTA;
        }
        return Blocks.GRAVEL;
    }

    private static void butte(ServerLevel level, BlockPos base, double radius, int height, RandomSource random) {
        net.minecraft.world.level.block.Block[] bands = {Blocks.TERRACOTTA, Blocks.PACKED_MUD, Blocks.DYED_TERRACOTTA.brown(), Blocks.SANDSTONE, Blocks.TERRACOTTA, Blocks.DYED_TERRACOTTA.lightGray()};
        int offset = random.nextInt(bands.length);
        for (int y = -2; y < height; y++) {
            double taper = y < height - 2 ? radius * (1.0 - 0.25 * y / Math.max(1.0, height)) : radius * 0.9;
            net.minecraft.world.level.block.Block block = bands[((y + 2) / 3 + offset) % bands.length];
            Structures.disc(level, base.above(y), taper + (random.nextInt(5) == 0 ? 0.6 : 0.0), block);
        }
        Structures.disc(level, base.above(height), radius * 0.95, Blocks.SANDSTONE);
    }

    private static void boulder(ServerLevel level, BlockPos base, double radius, RandomSource random) {
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -1; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy * 1.4 + dz * dz <= radius * radius) {
                        Structures.set(level, base.offset(dx, dy, dz), random.nextBoolean() ? Blocks.TERRACOTTA : Blocks.PACKED_MUD);
                    }
                }
            }
        }
    }

    private static void buildBattlefield(ServerLevel level, BlockPos center) {
        RandomSource random = RandomSource.create(center.asLong());
        int radius = 50;
        int y0 = center.getY();
        for (int dx = -radius - 8; dx <= radius + 8; dx++) {
            for (int dz = -radius - 8; dz <= radius + 8; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius + 8) {
                    continue;
                }
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                level.getChunk(x >> 4, z >> 4);
                int natural = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                double edge = Math.max(0.0, (d - radius * 0.75) / (radius * 0.25 + 8));
                double ripple = Math.sin(x * 0.11) * 0.6 + Math.cos(z * 0.09) * 0.6;
                int target = (int) Math.round(y0 + ripple * (1.0 - Math.min(1.0, edge)) + Math.min(1.0, edge) * (natural - y0));
                for (int y = target + 1; y <= Math.max(target, natural) + 30; y++) {
                    Structures.set(level, new BlockPos(x, y, z), Blocks.AIR);
                }
                for (int y = target - 4; y <= target; y++) {
                    Structures.set(level, new BlockPos(x, y, z), y == target ? wastelandGround(random) : y > target - 2 ? Blocks.SANDSTONE : Blocks.STONE);
                }
            }
        }
        pod(level, center.offset(6, 0, 22));
        pod(level, center.offset(-6, 0, 22));
        for (int i = 0; i < 14; i++) {
            double a = i * Math.PI * 2 / 14 + random.nextDouble() * 0.3;
            double r = 30 + random.nextDouble() * 18;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
            if (base.distSqr(center.offset(0, 0, 22)) < 12 * 12) {
                continue;
            }
            butte(level, base, 2.5 + random.nextDouble() * 3.0, 8 + random.nextInt(16), random);
        }
        for (int i = 0; i < 18; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = 20 + random.nextDouble() * 28;
            boulder(level, center.offset((int) Math.round(Math.cos(a) * r), 1, (int) Math.round(Math.sin(a) * r)), 0.8 + random.nextDouble() * 1.4, random);
        }
    }
}
