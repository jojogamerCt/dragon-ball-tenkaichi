package com.dbtenkaichi.training;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum Arena {
    MOUNTAIN(22),
    ISLAND(16),
    TOWER(14),
    TIME_CHAMBER(40);

    private final int radius;

    Arena(int radius) {
        this.radius = radius;
    }

    public int radius() {
        return radius;
    }

    public String translationKey() {
        return "arena.dbtenkaichi." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public void build(ServerLevel level, BlockPos center) {
        RandomSource random = RandomSource.create(center.asLong());
        int r = radius;
        int outer = this == ISLAND ? r + 7 : r;
        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > outer + 0.5) {
                    continue;
                }
                BlockPos top = center.offset(dx, -1, dz);
                for (int up = 1; up <= 12; up++) {
                    Structures.set(level, top.above(up), Blocks.AIR.defaultBlockState());
                }
                switch (this) {
                    case MOUNTAIN -> Structures.column(level, top, random.nextInt(5) == 0 ? Blocks.GRAVEL : random.nextInt(3) == 0 ? Blocks.ANDESITE : Blocks.STONE, Blocks.STONE, 4);
                    case ISLAND -> {
                        if (d <= r) {
                            Structures.column(level, top, Blocks.SAND, Blocks.SANDSTONE, 4);
                        } else {
                            Structures.column(level, top.below(), Blocks.SAND, Blocks.SANDSTONE, 2);
                            Structures.set(level, top, Blocks.WATER.defaultBlockState());
                        }
                    }
                    case TOWER -> Structures.column(level, top, d > r - 1.5 ? Blocks.CHISELED_SANDSTONE : ((dx + dz) & 1) == 0 ? Blocks.SMOOTH_SANDSTONE : Blocks.CUT_SANDSTONE, Blocks.SANDSTONE, 3);
                    case TIME_CHAMBER -> Structures.column(level, top, Blocks.CONCRETE.white(), Blocks.CONCRETE.white(), 2);
                }
            }
        }
        decorate(level, center, random);
    }

    private void decorate(ServerLevel level, BlockPos center, RandomSource random) {
        switch (this) {
            case MOUNTAIN -> {
                for (int i = 0; i < 6; i++) {
                    double a = Math.PI * 2 * i / 6 + 0.3;
                    BlockPos base = center.offset((int) Math.round(Math.cos(a) * (radius - 3)), 0, (int) Math.round(Math.sin(a) * (radius - 3)));
                    int h = 3 + random.nextInt(5);
                    for (int y = 0; y < h; y++) {
                        Structures.set(level, base.above(y), ((y + i) & 1) == 0 ? Blocks.STONE.defaultBlockState() : Blocks.ANDESITE.defaultBlockState());
                        Structures.set(level, base.above(y).east(), Blocks.STONE.defaultBlockState());
                    }
                }
            }
            case ISLAND -> {
                Structures.palm(level, center.offset(-9, 0, -7), 5);
                Structures.palm(level, center.offset(10, 0, 6), 6);
            }
            case TOWER -> {
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8;
                    BlockPos base = center.offset((int) Math.round(Math.cos(a) * (radius - 1)), 0, (int) Math.round(Math.sin(a) * (radius - 1)));
                    for (int y = 0; y < 4; y++) {
                        Structures.set(level, base.above(y), Blocks.SANDSTONE_WALL.defaultBlockState());
                    }
                }
            }
            default -> {
            }
        }
    }
}
