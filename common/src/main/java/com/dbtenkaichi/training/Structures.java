package com.dbtenkaichi.training;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class Structures {
    private Structures() {
    }

    public static void set(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.isInWorldBounds(pos) && level.getBlockState(pos) != state) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void set(ServerLevel level, BlockPos pos, Block block) {
        set(level, pos, block.defaultBlockState());
    }

    public static void column(ServerLevel level, BlockPos top, Block surface, Block fill, int depth) {
        set(level, top, surface);
        for (int i = 1; i < depth; i++) {
            set(level, top.below(i), fill);
        }
    }

    public static int ground(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    public static void disc(ServerLevel level, BlockPos center, double radius, Block block) {
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    set(level, center.offset(dx, 0, dz), block);
                }
            }
        }
    }

    public static void ring(ServerLevel level, BlockPos center, double inner, double outer, Block block) {
        int r = (int) Math.ceil(outer);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d2 = dx * dx + dz * dz;
                if (d2 <= outer * outer && d2 > inner * inner) {
                    set(level, center.offset(dx, 0, dz), block);
                }
            }
        }
    }

    public static void dome(ServerLevel level, BlockPos base, int radius, Block block) {
        for (int y = 0; y <= radius; y++) {
            double r = Math.sqrt(Math.max(0, radius * radius - y * y));
            ring(level, base.above(y), Math.max(0, r - 1.2), r, block);
        }
    }

    public static void flatDome(ServerLevel level, BlockPos base, int radius, int height, Block block) {
        for (int y = 0; y <= height; y++) {
            double t = y / (height + 0.5);
            double r = radius * Math.sqrt(Math.max(0.0, 1.0 - t * t));
            disc(level, base.above(y), r, block);
        }
    }

    public static void palm(ServerLevel level, BlockPos base, int height) {
        for (int y = 0; y < height; y++) {
            set(level, base.offset(y / 3, y, 0), Blocks.JUNGLE_LOG);
        }
        BlockPos top = base.offset((height - 1) / 3, height, 0);
        set(level, top, leaves(Blocks.JUNGLE_LEAVES));
        for (int[] o : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            set(level, top.offset(o[0], 0, o[1]), leaves(Blocks.JUNGLE_LEAVES));
            set(level, top.offset(o[0] * 2, -1, o[1] * 2), leaves(Blocks.JUNGLE_LEAVES));
            set(level, top.offset(o[0] * 3, -2, o[1] * 3), leaves(Blocks.JUNGLE_LEAVES));
        }
    }

    public static BlockState leaves(Block block) {
        BlockState state = block.defaultBlockState();
        return state.hasProperty(LeavesBlock.PERSISTENT) ? state.setValue(LeavesBlock.PERSISTENT, true) : state;
    }

    public static void tree(ServerLevel level, BlockPos base) {
        for (int y = 0; y < 5; y++) {
            set(level, base.above(y), Blocks.OAK_LOG);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int y = 3; y <= 6; y++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.max(0, y - 4) <= 3 && !(dx == 0 && dz == 0 && y < 5)) {
                        set(level, base.offset(dx, y, dz), leaves(Blocks.OAK_LEAVES));
                    }
                }
            }
        }
    }

    public static void conifer(ServerLevel level, BlockPos base) {
        set(level, base, Blocks.SPRUCE_LOG);
        set(level, base.above(), Blocks.SPRUCE_LOG);
        for (int y = 1; y <= 6; y++) {
            double r = y < 3 ? 1.5 : y < 5 ? 1.0 : 0.0;
            int ri = (int) Math.ceil(r);
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    if (dx * dx + dz * dz <= r * r + 0.01 && !(dx == 0 && dz == 0 && y < 2)) {
                        set(level, base.offset(dx, y, dz), leaves(Blocks.SPRUCE_LEAVES));
                    }
                }
            }
        }
    }
}
