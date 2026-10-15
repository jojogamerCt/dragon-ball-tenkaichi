package com.dbtenkaichi.world;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.movement.FlightSystem;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.training.Structures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class OtherWorld {
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, Tenkaichi.id("other_world"));
    public static final int ROAD_Y = 119;
    public static final int ROAD_START = 34;
    public static final int ROAD_END = 900;
    public static final int CLOUD_TOP = 110;
    public static final BlockPos CHECK_IN = new BlockPos(0, 120, 0);
    private static final int PLANET_RADIUS = 18;
    private static final int RAMP_START = 850;
    private static final int RAMP_END = 918;
    private static final String GENERATION = "3";
    private static final Identifier GRAVITY_ID = Tenkaichi.id("king_kai_gravity");
    private static final Identifier GRAVITY_SLOW_ID = Tenkaichi.id("king_kai_gravity_slow");
    private static final Identifier GRAVITY_JUMP_ID = Tenkaichi.id("king_kai_gravity_jump");
    private static MinecraftServer built;

    private OtherWorld() {
    }

    public static boolean is(Level level) {
        return KEY.equals(level.dimension());
    }

    public static double roadX(double z) {
        double clamped = Math.min(z, ROAD_END);
        return 26.0 * Math.sin(clamped / 70.0) + 10.0 * Math.sin(clamped / 23.0) - 26.0 * Math.sin(ROAD_START / 70.0) - 10.0 * Math.sin(ROAD_START / 23.0) + 14.0;
    }

    public static double roadY(double z) {
        if (z <= RAMP_START) {
            return ROAD_Y;
        }
        double t = Math.min(1.0, (z - RAMP_START) / (double) (RAMP_END - RAMP_START));
        return ROAD_Y + t * (planetCenter().getY() + Math.sqrt(PLANET_RADIUS * PLANET_RADIUS - 144.0) - 1 - ROAD_Y);
    }

    private static BlockPos planetCenter() {
        return new BlockPos((int) Math.round(roadX(ROAD_END)), 128, RAMP_END + 12);
    }

    public static BlockPos planet() {
        return planetCenter().above(PLANET_RADIUS + 1);
    }

    public static ServerLevel level(MinecraftServer server) {
        ServerLevel level = server.getLevel(KEY);
        if (level != null) {
            ensureBuilt(server, level);
        }
        return level;
    }

    private static void ensureBuilt(MinecraftServer server, ServerLevel level) {
        if (built == server) {
            return;
        }
        built = server;
        Path marker = server.getWorldPath(LevelResource.ROOT).resolve("dbtenkaichi_other_world.built");
        String previous = "";
        try {
            if (Files.exists(marker)) {
                previous = Files.readString(marker).trim();
            }
        } catch (IOException e) {
            Tenkaichi.LOG.warn("Could not read the Other World marker", e);
        }
        if (previous.equals(GENERATION)) {
            return;
        }
        if (!previous.isEmpty()) {
            clearOld(level);
        }
        build(level);
        try {
            Files.writeString(marker, GENERATION);
        } catch (IOException e) {
            Tenkaichi.LOG.warn("Could not mark Other World as built", e);
        }
    }

    private static void clear(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                level.getChunk(x >> 4, z >> 4);
                for (int y = y0; y <= y1; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static void clearOld(ServerLevel level) {
        Tenkaichi.LOG.info("Clearing the previous Other World structures");
        clear(level, -40, CLOUD_TOP + 1, -60, 40, 170, 30);
        for (int z = 0; z <= 960; z += 16) {
            clear(level, -70, CLOUD_TOP + 1, z, 70, 170, z + 15);
        }
    }

    public static void build(ServerLevel level) {
        Tenkaichi.LOG.info("Building the Other World: Check-in Station, Snake Way and King Kai's planet");
        RandomSource random = RandomSource.create(42L);
        checkIn(level, random);
        snakeWay(level, random);
        kingKaiPlanet(level, random);
    }

    private static BlockState stairs(Block block, Direction facing) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    private static BlockState slab(Block block, boolean top) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    private static void box(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    Structures.set(level, new BlockPos(x, y, z), state);
                }
            }
        }
    }

    private static void box(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        box(level, x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    private static void cloud(ServerLevel level, BlockPos center, double rx, double ry, double rz, RandomSource random) {
        int ix = (int) Math.ceil(rx);
        int iy = (int) Math.ceil(ry);
        int iz = (int) Math.ceil(rz);
        for (int dx = -ix; dx <= ix; dx++) {
            for (int dz = -iz; dz <= iz; dz++) {
                for (int dy = -iy; dy <= iy; dy++) {
                    double d = dx * dx / (rx * rx) + dy * dy / (ry * ry) + dz * dz / (rz * rz);
                    if (d <= 1.0) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        Block block = dy > iy * 0.4 ? Blocks.WOOL.yellow() : (d > 0.75 || random.nextInt(4) == 0) ? Blocks.WOOL.orange() : Blocks.WOOL.yellow();
                        Structures.set(level, pos, block);
                    }
                }
            }
        }
    }

    private static void roundWindow(ServerLevel level, int x, int y, int z, Direction.Axis plane, double radius) {
        int r = (int) Math.ceil(radius + 1);
        for (int a = -r; a <= r; a++) {
            for (int b = -r; b <= r; b++) {
                double d = Math.sqrt(a * a + b * b);
                BlockPos pos = plane == Direction.Axis.Z ? new BlockPos(x + a, y + b, z) : new BlockPos(x, y + b, z + a);
                if (d <= radius - 0.4) {
                    Structures.set(level, pos, Blocks.CONCRETE.black());
                } else if (d <= radius + 0.6) {
                    Structures.set(level, pos, Blocks.CONCRETE.red());
                }
            }
        }
    }

    private static void sign(ServerLevel level, BlockPos pos, Direction facing, String text) {
        Structures.set(level, pos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing));
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            List<Component> lines = List.of(Component.empty(), Component.literal(text), Component.empty(), Component.empty());
            sign.setText(new SignText(lines, lines, DyeColor.RED, true), SignTextSlot.FRONT);
            sign.setWaxed(true);
            sign.setChanged();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void tieredRoof(ServerLevel level, int x0, int z0, int x1, int z1, int y, int steps) {
        Block tile = Blocks.PRISMARINE_BRICKS;
        Block stair = Blocks.PRISMARINE_BRICK_STAIRS;
        for (int i = 0; i < steps; i++) {
            int ax = x0 + i;
            int bx = x1 - i;
            int az = z0 + i;
            int bz = z1 - i;
            if (ax > bx || az > bz) {
                break;
            }
            for (int x = ax; x <= bx; x++) {
                for (int z = az; z <= bz; z++) {
                    boolean edge = x == ax || x == bx || z == az || z == bz;
                    if (!edge && i < steps - 1) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(x, y + i, z);
                    boolean corner = (x == ax || x == bx) && (z == az || z == bz);
                    if (!edge || corner) {
                        Structures.set(level, pos, tile);
                    } else if (z == az) {
                        Structures.set(level, pos, stairs(stair, Direction.SOUTH));
                    } else if (z == bz) {
                        Structures.set(level, pos, stairs(stair, Direction.NORTH));
                    } else if (x == ax) {
                        Structures.set(level, pos, stairs(stair, Direction.EAST));
                    } else {
                        Structures.set(level, pos, stairs(stair, Direction.WEST));
                    }
                }
            }
        }
        Structures.set(level, new BlockPos(x0, y - 1, z0), Blocks.CONCRETE.red());
        Structures.set(level, new BlockPos(x1, y - 1, z0), Blocks.CONCRETE.red());
        Structures.set(level, new BlockPos(x0, y - 1, z1), Blocks.CONCRETE.red());
        Structures.set(level, new BlockPos(x1, y - 1, z1), Blocks.CONCRETE.red());
    }

    private static void hall(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1, boolean interior) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
                for (int y = y0; y <= y1; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (wall) {
                        boolean pillar = (x == x0 || x == x1) && (z == z0 || z == z1) || Math.floorMod(x - x0, 6) == 0 && (z == z0 || z == z1) || Math.floorMod(z - z0, 6) == 0 && (x == x0 || x == x1);
                        Block block = y == y0 || y == y1 ? Blocks.CONCRETE.red() : pillar ? Blocks.CONCRETE.red() : Blocks.CONCRETE.white();
                        Structures.set(level, pos, block);
                    } else if (interior) {
                        Structures.set(level, pos, Blocks.AIR);
                    } else {
                        Structures.set(level, pos, Blocks.CONCRETE.white());
                    }
                }
            }
        }
    }

    private static void tower(ServerLevel level, BlockPos base, int height) {
        for (int y = 0; y < height; y++) {
            boolean band = y % 7 == 6;
            Structures.disc(level, base.above(y), 3.2, band ? Blocks.CONCRETE.red() : Blocks.CONCRETE.white());
        }
        for (int y = 4; y < height - 2; y += 7) {
            Structures.set(level, base.offset(0, y, -3), Blocks.CONCRETE.black());
            Structures.set(level, base.offset(0, y, 3), Blocks.CONCRETE.black());
            Structures.set(level, base.offset(3, y, 0), Blocks.CONCRETE.black());
            Structures.set(level, base.offset(-3, y, 0), Blocks.CONCRETE.black());
        }
        Structures.disc(level, base.above(height), 4.4, Blocks.CONCRETE.red());
        for (int y = 1; y <= 7; y++) {
            double r = 4.2 * (1.0 - y / 7.5);
            Structures.disc(level, base.above(height + y), Math.max(0.5, r), Blocks.PRISMARINE_BRICKS);
        }
        for (int y = 8; y <= 10; y++) {
            Structures.set(level, base.above(height + y), y == 9 ? Blocks.CONCRETE.red() : Blocks.CONCRETE.yellow());
        }
    }

    private static void horn(ServerLevel level, BlockPos root, int side) {
        double[][] path = {{0, 0}, {1, 1}, {2, 1.6}, {3, 2.6}, {3.6, 3.8}, {3.8, 5.0}, {3.6, 6.0}};
        for (int i = 0; i < path.length; i++) {
            double thick = i < 3 ? 1.0 : 0.5;
            BlockPos p = root.offset((int) Math.round(path[i][0] * side), (int) Math.round(path[i][1]), 0);
            Structures.set(level, p, Blocks.QUARTZ_BLOCK);
            if (thick > 0.6) {
                Structures.set(level, p.below(), Blocks.QUARTZ_BLOCK);
            }
        }
    }

    private static void checkIn(ServerLevel level, RandomSource random) {
        int floor = CHECK_IN.getY() - 1;
        for (int dx = -40; dx <= 40; dx += 5) {
            for (int dz = -58; dz <= 28; dz += 5) {
                if (random.nextInt(3) == 0) {
                    continue;
                }
                cloud(level, new BlockPos(dx + random.nextInt(3), floor - 4 - random.nextInt(4), dz + random.nextInt(3)), 4 + random.nextInt(4), 2 + random.nextInt(3), 4 + random.nextInt(4), random);
            }
        }
        for (int y = CLOUD_TOP; y < floor; y++) {
            double r = 33 - (floor - y) * 0.15;
            Structures.disc(level, new BlockPos(0, y, -14), r, y > floor - 3 ? Blocks.WOOL.yellow() : Blocks.WOOL.orange());
        }
        box(level, -31, floor, -45, 31, floor, 17, Blocks.SMOOTH_QUARTZ);
        box(level, -2, floor, -40, 2, floor, 17, Blocks.CONCRETE.red());
        for (int x = -31; x <= 31; x++) {
            for (int z = -45; z <= 17; z++) {
                if (x != -31 && x != 31 && z != -45 && z != 17) {
                    continue;
                }
                if (z == 17 && Math.abs(x) <= 3) {
                    continue;
                }
                for (int y = 1; y <= 3; y++) {
                    Structures.set(level, new BlockPos(x, floor + y, z), y == 3 ? Blocks.CONCRETE.red() : Blocks.CONCRETE.white());
                }
            }
        }
        for (int y = 1; y <= 6; y++) {
            Structures.set(level, new BlockPos(-4, floor + y, 17), Blocks.CONCRETE.red());
            Structures.set(level, new BlockPos(4, floor + y, 17), Blocks.CONCRETE.red());
        }
        box(level, -5, floor + 7, 17, 5, floor + 7, 17, Blocks.PRISMARINE_BRICKS);
        int[][] corners = {{-31, -45}, {31, -45}, {-31, 17}, {31, 17}};
        for (int[] c : corners) {
            tower(level, new BlockPos(c[0], floor + 1, c[1]), 26);
        }
        int y0 = floor + 1;
        hall(level, -15, y0, -22, 15, y0 + 13, 6, true);
        box(level, -14, floor, -21, 14, floor, 5, Blocks.CONCRETE.red());
        box(level, -14, y0 + 13, -21, 14, y0 + 13, 5, Blocks.CONCRETE.white());
        box(level, -2, y0, 6, 2, y0 + 6, 6, Blocks.AIR);
        box(level, -3, y0 + 7, 6, 3, y0 + 7, 6, Blocks.CONCRETE.red());
        box(level, -6, y0 + 8, 7, 6, y0 + 9, 7, Blocks.CONCRETE.white());
        box(level, -7, y0 + 8, 7, -7, y0 + 9, 7, Blocks.CONCRETE.red());
        box(level, 7, y0 + 8, 7, 7, y0 + 9, 7, Blocks.CONCRETE.red());
        sign(level, new BlockPos(0, y0 + 9, 8), Direction.SOUTH, "WELCOME");
        roundWindow(level, -9, y0 + 8, 6, Direction.Axis.Z, 2.2);
        roundWindow(level, 9, y0 + 8, 6, Direction.Axis.Z, 2.2);
        roundWindow(level, -15, y0 + 8, -8, Direction.Axis.X, 2.2);
        roundWindow(level, 15, y0 + 8, -8, Direction.Axis.X, 2.2);
        for (int[] lamp : new int[][]{{-13, -19}, {13, -19}, {-13, 3}, {13, 3}}) {
            Structures.set(level, new BlockPos(lamp[0], y0 + 12, lamp[1]), Blocks.SEA_LANTERN);
        }
        tieredRoof(level, -18, -25, 18, 9, y0 + 14, 4);
        hall(level, -10, y0 + 18, -18, 10, y0 + 24, 2, false);
        roundWindow(level, -5, y0 + 21, 2, Direction.Axis.Z, 1.6);
        roundWindow(level, 0, y0 + 21, 2, Direction.Axis.Z, 1.6);
        roundWindow(level, 5, y0 + 21, 2, Direction.Axis.Z, 1.6);
        tieredRoof(level, -13, -21, 13, 5, y0 + 25, 4);
        hall(level, -5, y0 + 29, -13, 5, y0 + 33, -3, false);
        roundWindow(level, 0, y0 + 31, -3, Direction.Axis.Z, 1.2);
        tieredRoof(level, -8, -16, 8, 0, y0 + 34, 5);
        horn(level, new BlockPos(-6, y0 + 37, -8), -1);
        horn(level, new BlockPos(6, y0 + 37, -8), 1);
        for (int y = 0; y < 3; y++) {
            Structures.set(level, new BlockPos(0, y0 + 39 + y, -8), y == 2 ? Blocks.CONCRETE.red() : Blocks.CONCRETE.yellow());
        }
        Structures.disc(level, new BlockPos(0, y0 + 42, -8), 1.2, Blocks.CONCRETE.red());
        desk(level, y0);
    }

    private static void desk(ServerLevel level, int y0) {
        BlockState plank = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        box(level, -9, y0, -11, 9, y0 + 4, -7, plank);
        box(level, -10, y0 + 5, -12, 10, y0 + 5, -6, slab(Blocks.DARK_OAK_SLAB, false));
        for (int x = -8; x <= 8; x += 4) {
            box(level, x, y0 + 1, -6, x + 2, y0 + 3, -6, Blocks.SPRUCE_PLANKS);
            Structures.set(level, new BlockPos(x + 1, y0 + 2, -6), Blocks.GOLD_BLOCK);
        }
        box(level, -9, y0, -6, -9, y0 + 4, -6, Blocks.CONCRETE.red());
        box(level, 9, y0, -6, 9, y0 + 4, -6, Blocks.CONCRETE.red());
        box(level, -7, y0 + 6, -10, -4, y0 + 7, -8, Blocks.WOOL.white());
        box(level, -6, y0 + 8, -10, -4, y0 + 8, -9, Blocks.WOOL.white());
        box(level, 4, y0 + 6, -10, 6, y0 + 6, -9, Blocks.WOOL.white());
        box(level, 7, y0 + 6, -11, 8, y0 + 7, -10, Blocks.CONCRETE.black());
        box(level, 8, y0 + 8, -11, 8, y0 + 11, -11, Blocks.QUARTZ_BLOCK);
        box(level, 0, y0 + 6, -9, 1, y0 + 7, -8, Blocks.CONCRETE.red());
        box(level, 0, y0 + 8, -9, 1, y0 + 9, -8, Blocks.DARK_OAK_PLANKS);
        for (int x = -12; x <= 12; x++) {
            for (int z = -20; z <= -14; z++) {
                Structures.set(level, new BlockPos(x, y0 - 1, z), Blocks.CONCRETE.red());
            }
        }
        Structures.disc(level, new BlockPos(0, y0 - 1, -2), 3.5, Blocks.CONCRETE.black());
    }

    private static void snakeWay(ServerLevel level, RandomSource random) {
        double radius = 3.6;
        BlockPos pad = new BlockPos((int) Math.round(roadX(ROAD_START)), ROAD_Y - 1, ROAD_START - 8);
        Structures.disc(level, pad, 8.5, Blocks.CONCRETE.purple());
        Structures.disc(level, pad.below(), 8.5, Blocks.CONCRETE.purple());
        Structures.ring(level, pad, 3.0, 5.2, Blocks.CONCRETE.red());
        for (int y = 2; y <= 6; y++) {
            Structures.disc(level, pad.below(y), 8.5 - y * 1.2, Blocks.WOOL.orange());
        }
        for (int z = 17; z <= pad.getZ(); z++) {
            double t = (z - 17) / (double) Math.max(1, pad.getZ() - 17);
            int x = (int) Math.round(pad.getX() * t);
            box(level, x - 2, ROAD_Y - 1, z, x + 2, ROAD_Y - 1, z, Blocks.CONCRETE.purple());
        }
        snakeHead(level, pad.offset(13, 1, 2));
        for (double z = ROAD_START - 2; z <= RAMP_END + 1; z += 0.5) {
            double x = roadX(z);
            double nx = roadX(z + 0.5) - x;
            double len = Math.sqrt(nx * nx + 0.25);
            double px = 0.5 / len;
            double pz = -nx / len;
            double top = roadY(z);
            double yc = top - radius + 0.5;
            int zi = (int) Math.floor(z);
            level.getChunk(((int) Math.floor(x)) >> 4, zi >> 4);
            for (double s = -radius; s <= radius; s += 0.5) {
                for (double t = -radius; t <= radius; t += 0.5) {
                    if (s * s + t * t > radius * radius) {
                        continue;
                    }
                    double y = yc + t;
                    if (y > top - 0.5) {
                        continue;
                    }
                    BlockPos pos = BlockPos.containing(x + px * s, y, z + pz * s);
                    boolean surface = y > top - 1.5;
                    Block block;
                    if (t < -radius * 0.45) {
                        block = Blocks.CONCRETE.white();
                    } else if (Math.floorMod((int) Math.floor(z * 0.5 + s * 0.5), 2) == Math.floorMod((int) Math.floor(z * 0.5 - s * 0.5), 2)) {
                        block = Blocks.CONCRETE.lightBlue();
                    } else {
                        block = surface ? Blocks.DYED_TERRACOTTA.lightBlue() : Blocks.CONCRETE.blue();
                    }
                    Structures.set(level, pos, block);
                }
            }
            double frac = top - Math.floor(top);
            if (frac >= 0.25 && frac < 0.75) {
                for (double s = -2.0; s <= 2.0; s += 0.5) {
                    BlockPos pos = BlockPos.containing(x + px * s, Math.floor(top), z + pz * s);
                    Structures.set(level, pos, slab(Blocks.SMOOTH_STONE_SLAB, false));
                }
            }
            if (zi % 5 == 0 && z == Math.floor(z) && z < RAMP_START) {
                for (int h = 0; h < 3; h++) {
                    for (int w = 0; w <= 2 - h; w++) {
                        BlockPos fin = BlockPos.containing(x + px * (radius + 0.6), top + h, z + w * 0.9);
                        Structures.set(level, fin, Blocks.CONCRETE.blue());
                    }
                }
            }
        }
        for (int i = 0; i < 260; i++) {
            double z = ROAD_START + random.nextDouble() * (RAMP_END - ROAD_START);
            double side = random.nextBoolean() ? 1 : -1;
            double x = roadX(z) + side * (9 + random.nextDouble() * 30);
            cloud(level, BlockPos.containing(x, CLOUD_TOP - 1, z), 3 + random.nextInt(4), 1.5 + random.nextInt(2), 3 + random.nextInt(4), random);
        }
    }

    private static void ellipsoid(ServerLevel level, BlockPos c, double rx, double ry, double rz, Block block, boolean onlyIfAir) {
        int ix = (int) Math.ceil(rx);
        int iy = (int) Math.ceil(ry);
        int iz = (int) Math.ceil(rz);
        for (int dx = -ix; dx <= ix; dx++) {
            for (int dy = -iy; dy <= iy; dy++) {
                for (int dz = -iz; dz <= iz; dz++) {
                    if (dx * dx / (rx * rx) + dy * dy / (ry * ry) + dz * dz / (rz * rz) <= 1.0) {
                        BlockPos pos = c.offset(dx, dy, dz);
                        if (!onlyIfAir || level.getBlockState(pos).isAir()) {
                            Structures.set(level, pos, block);
                        }
                    }
                }
            }
        }
    }

    private static void snakeHead(ServerLevel level, BlockPos base) {
        Block scale = Blocks.DYED_TERRACOTTA.lightBlue();
        Block light = Blocks.CONCRETE.lightBlue();
        BlockPos skull = base.above(5);
        ellipsoid(level, skull, 5.0, 4.0, 5.5, scale, false);
        ellipsoid(level, skull.offset(0, 2, -6), 4.4, 2.0, 6.0, scale, false);
        ellipsoid(level, skull.offset(0, -3, -6), 4.0, 1.6, 5.5, light, false);
        ellipsoid(level, skull.offset(0, -1, -7), 3.2, 0.9, 4.6, Blocks.AIR, false);
        for (int i = 0; i < 6; i++) {
            int z = -3 - i * 2;
            for (int sx : new int[]{-3, 3}) {
                Structures.set(level, skull.offset(sx, 0, z), Blocks.CALCITE);
                Structures.set(level, skull.offset(sx, -1, z), Blocks.CALCITE);
            }
        }
        Structures.set(level, skull.offset(-1, 1, -12), Blocks.CALCITE);
        Structures.set(level, skull.offset(1, 1, -12), Blocks.CALCITE);
        box(level, -1 + skull.getX(), skull.getY() - 2, skull.getZ() - 9, 1 + skull.getX(), skull.getY() - 2, skull.getZ() - 3, Blocks.CONCRETE.red());
        for (int sx : new int[]{-1, 1}) {
            BlockPos eye = skull.offset(sx * 5, 2, -2);
            Structures.set(level, eye, Blocks.CONCRETE.yellow());
            Structures.set(level, eye.above(), Blocks.CONCRETE.yellow());
            Structures.set(level, eye.offset(0, 0, -1), Blocks.CONCRETE.yellow());
            Structures.set(level, eye.offset(sx, 0, 0), Blocks.CONCRETE.black());
            for (int h = 0; h < 4; h++) {
                for (int w = 0; w <= 3 - h; w++) {
                    Structures.set(level, skull.offset(sx * (5 + w), -1 + h, 3 + h), Blocks.PURPUR_BLOCK);
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            Structures.set(level, skull.offset(0, 4 + i / 2, 2 + i), Blocks.CONCRETE.blue());
        }
        for (int i = 0; i < 12; i++) {
            BlockPos neck = skull.offset((int) Math.round(-i * 1.1), (int) Math.round(-1 - i * 0.6), 4 + (int) Math.round(i * 0.6));
            ellipsoid(level, neck, 3.2, 3.2, 1.6, Blocks.CONCRETE.lightBlue(), true);
        }
    }

    private static void kingKaiPlanet(ServerLevel level, RandomSource random) {
        BlockPos center = planetCenter();
        int r = PLANET_RADIUS;
        double nx = 0.94;
        double nz = 0.34;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                level.getChunk((center.getX() + dx) >> 4, (center.getZ() + dz) >> 4);
                for (int dy = -r; dy <= r; dy++) {
                    double d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 > (r + 0.5) * (r + 0.5)) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    boolean shell = d2 > (r - 1.2) * (r - 1.2);
                    double plane = Math.abs(dx * nx + dz * nz);
                    Block block;
                    if (shell && plane <= 1.6) {
                        block = plane <= 0.5 ? Blocks.SMOOTH_STONE : Blocks.STONE_BRICKS;
                    } else if (shell) {
                        block = random.nextInt(9) == 0 ? Blocks.MOSS_BLOCK : Blocks.CONCRETE.lime();
                    } else {
                        block = Blocks.DIRT;
                    }
                    Structures.set(level, pos, block);
                }
            }
        }
        BlockPos top = planet();
        BlockPos house = surface(level, top.offset(7, 0, -3));
        foundation(level, house.below(), 4.5);
        for (int y = 0; y < 3; y++) {
            Structures.ring(level, house.above(y), 3.0, 4.2, y == 1 ? Blocks.SMOOTH_SANDSTONE : Blocks.SANDSTONE);
        }
        Structures.ring(level, house.above(3), 3.6, 4.8, Blocks.CONCRETE.white());
        Structures.flatDome(level, house.above(4), 4, 3, Blocks.SMOOTH_SANDSTONE);
        Structures.disc(level, house.above(7), 1.4, Blocks.CONCRETE.white());
        for (int y = 0; y < 2; y++) {
            Structures.set(level, house.offset(-4, y, 0), Blocks.AIR);
            Structures.set(level, house.offset(-4, y, 1), Blocks.AIR);
        }
        Structures.set(level, house.offset(0, 1, -4), Blocks.GLASS_PANE);
        Structures.set(level, house.offset(3, 1, -3), Blocks.GLASS_PANE);
        Structures.set(level, house.offset(0, 1, 4), Blocks.GLASS_PANE);
        BlockPos annex = surface(level, top.offset(9, 0, 5));
        foundation(level, annex.below(), 2.6);
        for (int y = 0; y < 2; y++) {
            Structures.ring(level, annex.above(y), 1.6, 2.6, Blocks.SANDSTONE);
        }
        Structures.flatDome(level, annex.above(2), 3, 2, Blocks.SMOOTH_SANDSTONE);
        Structures.set(level, annex.offset(-2, 0, 0), Blocks.AIR);
        BlockPos well = surface(level, top.offset(4, 0, 4));
        Structures.ring(level, well, 0.6, 1.8, Blocks.STONE_BRICKS);
        Structures.set(level, well.below(), Blocks.WATER);
        Structures.set(level, well, Blocks.WATER);
        BlockPos car = surface(level, top.offset(-6, 0, 2));
        box(level, car.getX() - 1, car.getY(), car.getZ() - 3, car.getX() + 1, car.getY(), car.getZ() + 3, Blocks.CONCRETE.red());
        box(level, car.getX() - 1, car.getY() + 1, car.getZ() - 1, car.getX() + 1, car.getY() + 1, car.getZ() + 1, Blocks.CONCRETE.white());
        Structures.set(level, car.offset(0, 1, -2), Blocks.GLASS_PANE);
        for (int[] w : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            Structures.set(level, car.offset(w[0], 0, w[1]), Blocks.CONCRETE.black());
        }
        for (int[] t : new int[][]{{-6, -7}, {-7, 9}, {11, -9}}) {
            Structures.tree(level, surface(level, top.offset(t[0], 0, t[1])));
        }
        for (double[] dir : new double[][]{{1, 0.2, 0.3}, {-0.9, 0.1, -0.5}, {0.2, -0.3, -1}, {-0.4, 0.4, 0.9}}) {
            sideTree(level, center, r, new Vec3(dir[0], dir[1], dir[2]).normalize());
        }
    }

    private static void foundation(ServerLevel level, BlockPos center, double radius) {
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                BlockPos pos = center.offset(dx, 0, dz);
                Structures.set(level, pos, Blocks.SMOOTH_SANDSTONE);
                for (int i = 1; i <= 6 && level.getBlockState(pos.below(i)).isAir(); i++) {
                    Structures.set(level, pos.below(i), Blocks.CONCRETE.lime());
                }
            }
        }
    }

    private static BlockPos surface(ServerLevel level, BlockPos near) {
        BlockPos pos = near.above(3);
        for (int i = 0; i < 12 && level.getBlockState(pos.below()).isAir(); i++) {
            pos = pos.below();
        }
        return pos;
    }

    private static void sideTree(ServerLevel level, BlockPos center, int r, Vec3 dir) {
        Vec3 c = Vec3.atCenterOf(center);
        for (int i = 0; i < 5; i++) {
            Structures.set(level, BlockPos.containing(c.add(dir.scale(r + i))), Blocks.OAK_LOG);
        }
        Vec3 crown = c.add(dir.scale(r + 6));
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= 8) {
                        BlockPos pos = BlockPos.containing(crown.add(dx, dy, dz));
                        if (level.getBlockState(pos).isAir()) {
                            Structures.set(level, pos, Structures.leaves(Blocks.OAK_LEAVES));
                        }
                    }
                }
            }
        }
    }

    public static BlockPos roadPoint(double z) {
        double clamped = Math.max(ROAD_START, Math.min(RAMP_END, z));
        return BlockPos.containing(roadX(clamped), Math.ceil(roadY(clamped)), clamped);
    }

    public static void tick(ServerPlayer player, PlayerData data, long now) {
        boolean here = is(player.level());
        boolean onPlanet = false;
        if (here) {
            if (player.getY() < CLOUD_TOP + 3) {
                BlockPos safe = player.getZ() > RAMP_END ? planet() : player.getZ() < ROAD_START ? CHECK_IN : roadPoint(player.getZ());
                player.teleportTo(safe.getX() + 0.5, safe.getY() + 0.5, safe.getZ() + 0.5);
                player.setDeltaMovement(Vec3.ZERO);
                player.resetFallDistance();
                player.sendSystemMessage(Component.translatable("message.dbtenkaichi.snake_way_fall"));
            }
            BlockPos planet = planetCenter();
            double dx = player.getX() - planet.getX() - 0.5;
            double dy = player.getY() - planet.getY();
            double dz = player.getZ() - planet.getZ() - 0.5;
            onPlanet = dx * dx + dy * dy + dz * dz < (PLANET_RADIUS + 10) * (PLANET_RADIUS + 10);
        }
        boolean heavy = onPlanet && !data.missions().flag("gravity_trained");
        modifier(player, Attributes.GRAVITY, GRAVITY_ID, heavy ? 1.5 : 0.0);
        modifier(player, Attributes.MOVEMENT_SPEED, GRAVITY_SLOW_ID, heavy ? -0.45 : 0.0);
        modifier(player, Attributes.JUMP_STRENGTH, GRAVITY_JUMP_ID, heavy ? -0.45 : 0.0);
        if (heavy && data.runtime().flying && player.getY() < planet().getY() + 6) {
            FlightSystem.stop(player, data);
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.gravity_heavy"));
        }
        if (heavy && now % 40 == 0 && player.getDeltaMovement().horizontalDistanceSqr() > 1.0e-3) {
            PlayerManager.grantTp(player, data, 1.0);
        }
    }

    private static void modifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (Math.abs(amount) < 1.0e-6) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}
