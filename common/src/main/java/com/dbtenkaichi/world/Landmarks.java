package com.dbtenkaichi.world;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.entity.MasterKind;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.training.Structures;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

public final class Landmarks {
    public static final String KAME_HOUSE = "kame_house";
    public static final String KORIN_TOWER = "korin_tower";
    public static final String KAMI_LOOKOUT = "kami_lookout";

    private static MinecraftServer checked;
    private static final Map<String, BlockPos> SITES = new LinkedHashMap<>();

    private Landmarks() {
    }

    public static Map<String, BlockPos> sites() {
        return Map.copyOf(SITES);
    }

    public static void tick(MinecraftServer server) {
        if (checked == server) {
            return;
        }
        checked = server;
        SITES.clear();
        Path file = server.getWorldPath(LevelResource.ROOT).resolve("dbtenkaichi_landmarks.properties");
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                props.load(reader);
            } catch (IOException e) {
                Tenkaichi.LOG.warn("Could not read landmarks file", e);
            }
        }
        ServerLevel level = server.overworld();
        BlockPos spawn = level.getRespawnData().pos();
        boolean changed = false;
        if (!props.containsKey(KAME_HOUSE)) {
            props.setProperty(KAME_HOUSE, format(kameHouse(level, oceanSite(level, spawn))));
            changed = true;
        }
        if (!props.containsKey(KORIN_TOWER)) {
            BlockPos top = korinTower(level, spawn.offset(-170, 0, -150));
            props.setProperty(KORIN_TOWER, format(top));
            props.setProperty(KAMI_LOOKOUT, format(lookout(level, new BlockPos(top.getX(), Math.min(level.getMaxY() - 12, top.getY() + 62), top.getZ() - 6))));
            changed = true;
        }
        for (String key : new String[]{KAME_HOUSE, KORIN_TOWER, KAMI_LOOKOUT}) {
            BlockPos pos = parse(props.getProperty(key));
            if (pos != null) {
                SITES.put(key, pos);
            }
        }
        if (changed) {
            try (Writer writer = Files.newBufferedWriter(file)) {
                props.store(writer, "Dragon Block: Tenkaichi landmarks");
            } catch (IOException e) {
                Tenkaichi.LOG.warn("Could not save landmarks file", e);
            }
            Tenkaichi.LOG.info("Generated Dragon Ball landmarks: {}", SITES);
        }
    }

    private static String format(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
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

    private static void master(ServerLevel level, BlockPos pos, MasterKind kind) {
        MasterEntity master = ModEntities.MASTER.get().spawn(level, pos, EntitySpawnReason.STRUCTURE);
        if (master != null) {
            master.setKind(kind);
            master.setHome(pos);
        }
    }

    private static BlockPos oceanSite(ServerLevel level, BlockPos spawn) {
        BlockPos start = spawn.offset(160, 0, 60);
        BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
        RandomState state = level.getChunkSource().randomState();
        Predicate<Holder<Biome>> ocean = biome -> biome.is(BiomeTags.IS_OCEAN);
        Pair<BlockPos, Holder<Biome>> found = source.findBiomeHorizontal(start.getX(), level.getSeaLevel(), start.getZ(), 1600, 16, ocean, RandomSource.create(level.getSeed()), false, state);
        if (found == null) {
            return start;
        }
        BlockPos coast = found.getFirst();
        double dx = coast.getX() - spawn.getX();
        double dz = coast.getZ() - spawn.getZ();
        double len = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
        BlockPos deeper = coast.offset((int) Math.round(dx / len * 40), 0, (int) Math.round(dz / len * 40));
        boolean open = source.findBiomeHorizontal(deeper.getX(), level.getSeaLevel(), deeper.getZ(), 0, 1, ocean, RandomSource.create(level.getSeed()), false, state) != null;
        return open ? deeper : coast;
    }

    static BlockPos kameHouse(ServerLevel level, BlockPos origin) {
        int sea = level.getSeaLevel();
        BlockPos c = new BlockPos(origin.getX(), sea, origin.getZ());
        for (int dx = -20; dx <= 20; dx++) {
            for (int dz = -20; dz <= 20; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 20.5) {
                    continue;
                }
                int x = c.getX() + dx;
                int z = c.getZ() + dz;
                level.getChunk(x >> 4, z >> 4);
                int surface = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
                for (int y = sea - 1; y <= Math.max(surface, sea) + 1; y++) {
                    Structures.set(level, new BlockPos(x, y, z), Blocks.AIR);
                }
                for (int y = sea - 6; y < sea - 3; y++) {
                    Structures.set(level, new BlockPos(x, y, z), y == sea - 4 && d > 9.5 ? Blocks.SAND : Blocks.SANDSTONE);
                }
                if (d <= 9.5) {
                    for (int y = sea - 3; y <= sea; y++) {
                        Structures.set(level, new BlockPos(x, y, z), y == sea && d <= 7.0 ? Blocks.GRASS_BLOCK : y >= sea - 1 && d <= 7.0 ? Blocks.DIRT : y == sea ? Blocks.SAND : Blocks.SANDSTONE);
                    }
                } else {
                    for (int y = sea - 3; y < sea; y++) {
                        Structures.set(level, new BlockPos(x, y, z), Blocks.WATER);
                    }
                }
            }
        }
        BlockPos f = c.above();
        kameHouseBuilding(level, f);
        BlockPos npc = f.offset(-1, 0, 7);
        master(level, npc, MasterKind.ROSHI);
        return npc;
    }

    public static void kameHouseBuilding(ServerLevel level, BlockPos f) {
        Block pink = Blocks.CONCRETE.pink();
        Block white = Blocks.CONCRETE.white();
        Block red = Blocks.CONCRETE.red();
        BlockPos c = f.below();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -3; dz <= 4; dz++) {
                Structures.set(level, c.offset(dx, 0, dz), Blocks.OAK_PLANKS);
                boolean edge = Math.abs(dx) == 4 || dz == -3 || dz == 4;
                boolean corner = Math.abs(dx) == 4 && (dz == -3 || dz == 4);
                for (int y = 0; y < 5; y++) {
                    Block block = !edge ? (y == 2 ? Blocks.OAK_PLANKS : Blocks.AIR) : corner ? white : pink;
                    Structures.set(level, f.offset(dx, y, dz), block);
                }
            }
        }
        for (int k = 0; k <= 5; k++) {
            int half = 5 - k;
            for (int dz = -4; dz <= 5; dz++) {
                Structures.set(level, f.offset(half, 5 + k, dz), red);
                Structures.set(level, f.offset(-half, 5 + k, dz), red);
            }
            for (int dx = -half + 1; dx <= half - 1; dx++) {
                Structures.set(level, f.offset(dx, 5 + k, 4), pink);
                Structures.set(level, f.offset(dx, 5 + k, -3), pink);
            }
        }
        window(level, f.offset(-2, 1, 4), white);
        window(level, f.offset(-1, 1, 4), white);
        window(level, f.offset(-2, 5, 4), white);
        window(level, f.offset(4, 1, 0), white);
        window(level, f.offset(-4, 1, 0), white);
        window(level, f.offset(-4, 1, 1), white);
        Structures.set(level, f.offset(2, 0, 4), Blocks.AIR);
        Structures.set(level, f.offset(2, 1, 4), Blocks.AIR);
        for (int dx = 1; dx <= 4; dx++) {
            for (int dz = 5; dz <= 6; dz++) {
                Structures.set(level, f.offset(dx, 3, dz), red);
            }
        }
        for (int y = 0; y < 3; y++) {
            Structures.set(level, f.offset(1, y, 6), Blocks.BIRCH_FENCE);
            Structures.set(level, f.offset(4, y, 6), Blocks.BIRCH_FENCE);
        }
        for (int dz = 0; dz <= 1; dz++) {
            Structures.set(level, f.offset(4, 6, dz), Blocks.GLASS);
            Structures.set(level, f.offset(4, 7, dz), white);
        }
        for (int dz = -1; dz <= 2; dz++) {
            for (int dx = 3; dx <= 5; dx++) {
                Structures.set(level, f.offset(dx, 8, dz), red);
            }
        }
        sign(level, f.offset(0, 6, 5), "KAME", "HOUSE");
        Structures.palm(level, f.offset(5, 0, -5), 7);
        Structures.palm(level, f.offset(7, 0, 1), 6);
        Structures.palm(level, f.offset(-6, 0, -4), 5);
    }

    private static void window(ServerLevel level, BlockPos pos, Block frame) {
        Structures.set(level, pos, Blocks.GLASS);
        Structures.set(level, pos.above(), Blocks.GLASS);
        Structures.set(level, pos.below(), frame);
    }

    private static void sign(ServerLevel level, BlockPos pos, String first, String second) {
        BlockState state = Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.SOUTH);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            List<Component> lines = List.of(Component.empty(), Component.literal(first), Component.literal(second), Component.empty());
            sign.setText(new SignText(lines, lines, DyeColor.RED, true), SignTextSlot.FRONT);
            sign.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    static BlockPos korinTower(ServerLevel level, BlockPos origin) {
        return korinTower(level, origin, Structures.ground(level, origin.getX(), origin.getZ()), 120, true);
    }

    public static BlockPos korinTower(ServerLevel level, BlockPos origin, int ground, int height, boolean withMaster) {
        int top = Math.min(level.getMaxY() - 100, ground + height);
        Block white = Blocks.CONCRETE.white();
        Block grey = Blocks.CONCRETE.lightGray();
        BlockPos base = new BlockPos(origin.getX(), ground - 3, origin.getZ());
        Structures.disc(level, base.above(3), 4.5, Blocks.STONE_BRICKS);
        for (int y = 0; y <= top - base.getY() - 6; y++) {
            double spiral = y * 0.35;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    double d2 = dx * dx + dz * dz;
                    if (d2 > 5.5) {
                        continue;
                    }
                    double a = Math.atan2(dz, dx);
                    double diff = Math.abs(Math.IEEEremainder(a - spiral, Math.PI / 2));
                    boolean relief = d2 > 2.5 && diff < 0.35;
                    boolean band = y % 12 == 0;
                    Structures.set(level, base.offset(dx, y, dz), band ? Blocks.CHISELED_QUARTZ_BLOCK : relief ? grey : Blocks.QUARTZ_BLOCK);
                }
            }
        }
        BlockPos t = new BlockPos(origin.getX(), top, origin.getZ());
        double[] hull = {3.0, 5.5, 7.3, 8.3};
        for (int i = 0; i < hull.length; i++) {
            int y = i - hull.length;
            Structures.disc(level, t.above(y), hull[i], i < 2 ? grey : white);
            if (y == -2) {
                ring(level, t.above(y), hull[i], (idx, k) -> idx % 4 == 1 || idx % 4 == 2 ? Blocks.STAINED_GLASS.black() : white);
            }
        }
        Structures.disc(level, t, 8.6, white);
        Structures.ring(level, t.above(1), 7.7, 8.6, white);
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2 * i / 12;
            BlockPos post = t.offset((int) Math.round(Math.cos(a) * 8), 0, (int) Math.round(Math.sin(a) * 8));
            for (int y = 2; y <= 3; y++) {
                Structures.set(level, post.above(y), white);
            }
        }
        Structures.disc(level, t.above(4), 8.8, white);
        Structures.ring(level, t.above(4), 7.8, 8.8, grey);
        Structures.flatDome(level, t.above(5), 7, 2, white);
        Structures.disc(level, t.above(5), 7.2, white);
        Structures.set(level, t.above(8), Blocks.CONCRETE.yellow());
        Structures.set(level, t.above(9), Blocks.CONCRETE.yellow());
        Structures.set(level, t.above(10), Blocks.END_ROD);
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            Structures.set(level, t.offset((int) Math.round(Math.cos(a) * 5), 0, (int) Math.round(Math.sin(a) * 5)), Blocks.SEA_LANTERN);
        }
        Structures.set(level, t.offset(-3, 1, -3), Blocks.WATER_CAULDRON);
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8;
            for (int[] layer : new int[][]{{11, -3}, {11, 2}, {10, 7}}) {
                Structures.set(level, t.offset((int) Math.round(Math.cos(a) * layer[0]), layer[1], (int) Math.round(Math.sin(a) * layer[0])), Blocks.LIGHT);
            }
        }
        BlockPos npc = t.offset(0, 1, 6);
        if (withMaster) {
            master(level, npc, MasterKind.KARIN);
        }
        return npc;
    }

    private interface Shell {
        Block at(int arcIndex, int depth);
    }

    private static void ring(ServerLevel level, BlockPos center, double radius, Shell shell) {
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= radius + 0.5 && d > radius - 1.0) {
                    int idx = Math.floorMod((int) Math.round(Math.atan2(dz, dx) * radius), 60);
                    Structures.set(level, center.offset(dx, 0, dz), shell.at(idx, 0));
                }
            }
        }
    }

    static BlockPos lookout(ServerLevel level, BlockPos center) {
        return lookout(level, center, true);
    }

    public static BlockPos lookout(ServerLevel level, BlockPos center, boolean withMaster) {
        int radius = 22;
        int depth = 14;
        Block white = Blocks.CONCRETE.white();
        Block red = Blocks.CONCRETE.red();
        Block tan = Blocks.SMOOTH_SANDSTONE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius + 0.5) {
                    continue;
                }
                boolean grid = Math.floorMod(dx, 4) == 0 || Math.floorMod(dz, 4) == 0;
                Structures.set(level, center.offset(dx, 0, dz), d > radius - 1.5 ? white : grid ? Blocks.QUARTZ_BRICKS : Blocks.SMOOTH_QUARTZ);
                int idx = Math.floorMod((int) Math.round(Math.atan2(dz, dx) * radius), 60);
                for (int k = 1; k <= depth; k++) {
                    double rk = k <= 3 ? radius : radius * Math.sqrt(Math.max(0.0, 1.0 - Math.pow((k - 3) / (double) (depth - 1), 2)));
                    if (d > rk + 0.5) {
                        break;
                    }
                    boolean shell = d > rk - 1.6;
                    Block block;
                    if (!shell) {
                        block = tan;
                    } else if (k <= 3) {
                        int m = Math.floorMod(idx + 3, 6) - 3;
                        block = Math.abs(m) < k ? red : white;
                    } else if (k == 4 || k == 8) {
                        block = red;
                    } else if (k == 6) {
                        block = Math.floorMod(idx, 5) != 0 ? Blocks.STAINED_GLASS.black() : tan;
                    } else {
                        block = k >= depth - 1 ? Blocks.CUT_SANDSTONE : tan;
                    }
                    Structures.set(level, center.offset(dx, -k, dz), block);
                }
            }
        }
        Structures.set(level, center.below(depth + 1), Blocks.CUT_SANDSTONE);
        Structures.set(level, center.below(depth + 2), Blocks.CUT_SANDSTONE);
        for (int[] g : new int[][]{{-14, 3}, {10, 3}, {-14, -12}, {10, -12}}) {
            for (int dx = 0; dx < 5; dx++) {
                for (int dz = 0; dz < 4; dz++) {
                    Structures.set(level, center.offset(g[0] + dx, 0, g[1] + dz), Blocks.GRASS_BLOCK);
                }
            }
        }
        for (int i = 0; i < 28; i++) {
            double a = Math.PI * 2 * i / 28;
            if (Math.abs(Math.IEEEremainder(a - Math.PI / 2, Math.PI * 2)) < 0.45) {
                continue;
            }
            Structures.conifer(level, center.offset((int) Math.round(Math.cos(a) * 19), 1, (int) Math.round(Math.sin(a) * 19)));
        }
        for (int dz = 10; dz <= 16; dz += 6) {
            Structures.palm(level, center.offset(-4, 1, dz), 6);
            Structures.palm(level, center.offset(4, 1, dz), 6);
        }
        BlockPos p = center.offset(0, 1, -3);
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -5; dz <= 3; dz++) {
                boolean edge = Math.abs(dx) == 6 || dz == -5 || dz == 3;
                for (int y = 0; y <= 6; y++) {
                    Block block = y == 6 ? white : !edge ? Blocks.AIR : (y == 2 || y == 3) && Math.floorMod(dx + dz, 2) == 0 ? Blocks.STAINED_GLASS.purple() : white;
                    Structures.set(level, p.offset(dx, y, dz), block);
                }
            }
        }
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = 4; dz <= 6; dz++) {
                Structures.set(level, p.offset(dx, 5, dz), white);
            }
        }
        for (int x : new int[]{-6, -3, 3, 6}) {
            for (int y = 0; y < 5; y++) {
                Structures.set(level, p.offset(x, y, 6), Blocks.QUARTZ_PILLAR);
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int y = 0; y < 4; y++) {
                Structures.set(level, p.offset(dx, y, 3), Blocks.AIR);
            }
        }
        Structures.dome(level, p.above(7), 4, Blocks.CONCRETE.yellow());
        Structures.disc(level, p.above(7), 4.2, Blocks.CONCRETE.yellow());
        Structures.set(level, p.above(12), Blocks.CONCRETE.yellow());
        Structures.set(level, p.above(13), Blocks.END_ROD);
        for (int side : new int[]{-1, 1}) {
            BlockPos tower = center.offset(side * 12, 1, -6);
            for (int y = 0; y < 11; y++) {
                Structures.set(level, tower.above(y), Blocks.QUARTZ_PILLAR);
            }
            Structures.disc(level, tower.above(11), 2.6, white);
            Structures.dome(level, tower.above(12), 2, Blocks.CONCRETE.pink());
            Structures.set(level, tower.above(15), Blocks.CONCRETE.yellow());
        }
        BlockPos npc = center.offset(0, 1, 8);
        if (withMaster) {
            master(level, npc, MasterKind.KAMI);
        }
        return npc;
    }
}
