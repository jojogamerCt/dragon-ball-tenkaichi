package com.dbtenkaichi.training;

import com.dbtenkaichi.world.Landmarks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum Arena {
    MOUNTAIN(22),
    KAME_ISLAND(14),
    KORIN_FOREST(18),
    TIME_CHAMBER(26),
    NAMEK(20),
    CELL_RING(15),
    CANYON(18),
    TOURNAMENT_OF_POWER(26),
    BEERUS_PLANET(22),
    SACRED_WORLD(22),
    DEMON_REALM(20),
    RED_RIBBON_BASE(22),
    PLANET_CEREAL(22),
    RUINED_CITY(20),
    GLACIER(22),
    LOOKOUT(9);

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
        RandomSource random = RandomSource.create(center.asLong() + ordinal());
        switch (this) {
            case MOUNTAIN -> mountain(level, center, random);
            case KAME_ISLAND -> kameIsland(level, center);
            case KORIN_FOREST -> korinForest(level, center, random);
            case TIME_CHAMBER -> timeChamber(level, center);
            case NAMEK -> namek(level, center, random);
            case CELL_RING -> cellRing(level, center, random);
            case CANYON -> canyon(level, center, random);
            case TOURNAMENT_OF_POWER -> tournamentOfPower(level, center, random);
            case BEERUS_PLANET -> beerusPlanet(level, center, random);
            case SACRED_WORLD -> sacredWorld(level, center, random);
            case DEMON_REALM -> demonRealm(level, center, random);
            case RED_RIBBON_BASE -> redRibbonBase(level, center, random);
            case PLANET_CEREAL -> planetCereal(level, center, random);
            case RUINED_CITY -> ruinedCity(level, center, random);
            case GLACIER -> glacier(level, center, random);
            case LOOKOUT -> lookout(level, center);
        }
    }

    public static Arena byId(String id, Arena fallback) {
        if (id == null || id.isEmpty()) {
            return fallback;
        }
        for (Arena arena : values()) {
            if (arena.name().equalsIgnoreCase(id)) {
                return arena;
            }
        }
        return fallback;
    }

    private static void hills(ServerLevel level, BlockPos center, int r, Block surface, Block fill, double amp) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r) {
                    continue;
                }
                BlockPos top = center.offset(dx, -1, dz);
                for (int up = 1; up <= 16; up++) {
                    Structures.set(level, top.above(up), Blocks.AIR);
                }
                int lift = d < 24 ? 0 : (int) Math.round(Math.max(0.0, (Math.sin(dx * 0.11) + Math.cos(dz * 0.13)) * amp * (d - 24) / (r - 24.0)));
                for (int y = 0; y <= lift; y++) {
                    Structures.set(level, top.above(y), y == lift ? surface : fill);
                }
                Structures.column(level, top.below(), fill, fill, 2);
            }
        }
    }

    private void tournamentOfPower(ServerLevel level, BlockPos center, RandomSource random) {
        int stage = 30;
        for (int dx = -stage; dx <= stage; dx++) {
            for (int dz = -stage; dz <= stage; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > stage + 0.5) {
                    continue;
                }
                BlockPos top = center.offset(dx, -1, dz);
                for (int up = 1; up <= 18; up++) {
                    Structures.set(level, top.above(up), Blocks.AIR);
                }
                boolean ring = Math.abs(d - 12) < 0.8 || Math.abs(d - 22) < 0.8;
                boolean spoke = Math.floorMod((int) Math.round(Math.toDegrees(Math.atan2(dz, dx))), 45) < 3 && d > 4;
                Structures.set(level, top, d > stage - 1.2 ? Blocks.POLISHED_DEEPSLATE : ring || spoke ? Blocks.CONCRETE.lightGray() : Blocks.SMOOTH_STONE);
                int depth = (int) Math.round((stage - d) * 0.45) + 1;
                for (int y = 1; y <= depth; y++) {
                    Structures.set(level, top.below(y), y == depth ? Blocks.POLISHED_DEEPSLATE : Blocks.DEEPSLATE_TILES);
                }
            }
        }
        for (int i = 0; i < 18; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = stage + 10 + random.nextDouble() * 24;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), random.nextInt(20) - 12, (int) Math.round(Math.sin(a) * r));
            int h = 4 + random.nextInt(14);
            for (int y = 0; y < h; y++) {
                Structures.disc(level, base.above(y), 1.6, y == h - 1 ? Blocks.CHISELED_QUARTZ_BLOCK : Blocks.QUARTZ_PILLAR);
            }
        }
        for (int i = 0; i < 4; i++) {
            double a = Math.PI * 2 * i / 4 + Math.PI / 4;
            BlockPos seat = center.offset((int) Math.round(Math.cos(a) * 46), 10, (int) Math.round(Math.sin(a) * 46));
            Structures.disc(level, seat, 5.0, Blocks.SMOOTH_QUARTZ);
            Structures.ring(level, seat.above(), 4.2, 5.0, Blocks.QUARTZ_BLOCK);
        }
    }

    private static void bigTree(ServerLevel level, BlockPos base, int height, int canopy, RandomSource random) {
        for (int y = 0; y < height; y++) {
            Structures.disc(level, base.above(y), y < 2 ? 2.2 : 1.4, Blocks.DARK_OAK_LOG);
        }
        BlockPos top = base.above(height);
        for (int dx = -canopy; dx <= canopy; dx++) {
            for (int dy = -2; dy <= canopy / 2; dy++) {
                for (int dz = -canopy; dz <= canopy; dz++) {
                    if (dx * dx + dy * dy * 4 + dz * dz <= canopy * canopy && random.nextFloat() < 0.92f) {
                        Structures.set(level, top.offset(dx, dy, dz), Structures.leaves(Blocks.JUNGLE_LEAVES));
                    }
                }
            }
        }
    }

    private void beerusPlanet(ServerLevel level, BlockPos center, RandomSource random) {
        hills(level, center, 60, Blocks.GRASS_BLOCK, Blocks.DIRT, 6.0);
        for (int i = 0; i < 9; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius + 6 + random.nextDouble() * 26;
            bigTree(level, center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r)), 9 + random.nextInt(6), 5 + random.nextInt(3), random);
        }
        BlockPos temple = center.offset(0, 0, 46);
        for (int y = 0; y < 14; y++) {
            int half = 9 - y * 9 / 14;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    boolean edge = Math.abs(dx) == half || Math.abs(dz) == half;
                    if (edge || y == 0) {
                        Structures.set(level, temple.offset(dx, y, dz), y % 4 == 3 ? Blocks.CONCRETE.purple() : Blocks.SMOOTH_SANDSTONE);
                    }
                }
            }
        }
        Structures.set(level, temple.above(14), Blocks.CONCRETE.orange());
        Structures.set(level, temple.above(15), Blocks.END_ROD);
        for (int i = 0; i < 6; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            BlockPos rock = center.offset((int) Math.round(Math.cos(a) * 40), 14 + random.nextInt(10), (int) Math.round(Math.sin(a) * 40));
            Structures.flatDome(level, rock, 3, 2, Blocks.MOSSY_COBBLESTONE);
        }
    }

    private void sacredWorld(ServerLevel level, BlockPos center, RandomSource random) {
        hills(level, center, 58, Blocks.GRASS_BLOCK, Blocks.DIRT, 4.0);
        for (int i = 0; i < 16; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius + 4 + random.nextDouble() * 30;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), -1, (int) Math.round(Math.sin(a) * r));
            int h = 14 + random.nextInt(18);
            for (int y = 0; y < h; y++) {
                double w = 2.6 - y * 1.6 / h;
                Structures.disc(level, base.above(y), w, y > h - 3 ? Blocks.MOSS_BLOCK : (y / 4) % 2 == 0 ? Blocks.CALCITE : Blocks.DIORITE);
            }
        }
        BlockPos pond = center.offset(-30, -1, 18);
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (dx * dx / 49.0 + dz * dz / 25.0 <= 1.0) {
                    Structures.set(level, pond.offset(dx, 0, dz), Blocks.WATER);
                }
            }
        }
        BlockPos hut = center.offset(28, 0, -26);
        Structures.disc(level, hut.below(), 5.0, Blocks.SMOOTH_STONE);
        for (int y = 0; y < 5; y++) {
            Structures.ring(level, hut.above(y), 3.2, 4.2, y == 1 || y == 2 ? Blocks.GLASS : Blocks.CONCRETE.white());
        }
        Structures.dome(level, hut.above(5), 4, Blocks.CONCRETE.lightBlue());
    }

    private void demonRealm(ServerLevel level, BlockPos center, RandomSource random) {
        floor(level, center, 56, Blocks.CRIMSON_NYLIUM, Blocks.NETHERRACK, 3);
        for (int i = 0; i < 220; i++) {
            int dx = random.nextInt(113) - 56;
            int dz = random.nextInt(113) - 56;
            if (dx * dx + dz * dz <= 56 * 56) {
                Structures.set(level, center.offset(dx, -1, dz), random.nextBoolean() ? Blocks.WARPED_NYLIUM : Blocks.BLACKSTONE);
            }
        }
        for (int i = 0; i < 12; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius + 6 + random.nextDouble() * 26;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
            int h = 5 + random.nextInt(7);
            for (int y = 0; y < h; y++) {
                Structures.set(level, base.above(y), Blocks.WARPED_STEM);
            }
            Structures.flatDome(level, base.above(h), 3, 2, random.nextBoolean() ? Blocks.SHROOMLIGHT : Blocks.WARPED_WART_BLOCK);
        }
        for (int i = 0; i < 5; i++) {
            double a = Math.PI * 2 * i / 5 + 0.2;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * 46), 0, (int) Math.round(Math.sin(a) * 46));
            int h = 12 + random.nextInt(10);
            for (int y = 0; y < h; y++) {
                Structures.disc(level, base.above(y), 2.5 - y * 1.8 / h, Blocks.BLACKSTONE);
            }
            Structures.set(level, base.above(h), Blocks.STAINED_GLASS.magenta());
            Structures.set(level, base.above(h + 1), Blocks.STAINED_GLASS.magenta());
        }
    }

    private void redRibbonBase(ServerLevel level, BlockPos center, RandomSource random) {
        floor(level, center, 58, Blocks.CONCRETE.gray(), Blocks.STONE, 3);
        for (int dx = -58; dx <= 58; dx += 6) {
            Structures.set(level, center.offset(dx, -1, 0), Blocks.CONCRETE.yellow());
        }
        int[][] halls = {{-36, -30, 10, 8, 9}, {34, -28, 12, 9, 11}, {-38, 26, 9, 10, 8}, {36, 30, 11, 8, 10}};
        for (int[] h : halls) {
            BlockPos base = center.offset(h[0], 0, h[1]);
            for (int dx = -h[2]; dx <= h[2]; dx++) {
                for (int dz = -h[3]; dz <= h[3]; dz++) {
                    for (int y = 0; y < h[4]; y++) {
                        boolean wall = Math.abs(dx) == h[2] || Math.abs(dz) == h[3] || y == h[4] - 1;
                        if (!wall) {
                            continue;
                        }
                        Block block = y == h[4] - 3 ? Blocks.CONCRETE.red() : y == 2 && Math.floorMod(dx + dz, 4) == 0 ? Blocks.GLASS : Blocks.CONCRETE.lightGray();
                        Structures.set(level, base.offset(dx, y, dz), block);
                    }
                }
            }
            BlockPos chimney = base.offset(h[2] - 2, h[4], h[3] - 2);
            for (int y = 0; y < 10; y++) {
                Structures.disc(level, chimney.above(y), 1.3, y > 7 ? Blocks.CONCRETE.red() : Blocks.BRICKS);
            }
        }
        for (int i = 0; i < 14; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            BlockPos crate = center.offset((int) Math.round(Math.cos(a) * (radius + 4)), 0, (int) Math.round(Math.sin(a) * (radius + 4)));
            Structures.set(level, crate, Blocks.IRON_BLOCK);
            if (random.nextBoolean()) {
                Structures.set(level, crate.above(), Blocks.BARREL);
            }
        }
    }

    private void planetCereal(ServerLevel level, BlockPos center, RandomSource random) {
        hills(level, center, 58, Blocks.STONE, Blocks.STONE, 7.0);
        for (int i = 0; i < 160; i++) {
            int dx = random.nextInt(117) - 58;
            int dz = random.nextInt(117) - 58;
            if (dx * dx + dz * dz <= 24 * 24) {
                Structures.set(level, center.offset(dx, -1, dz), random.nextBoolean() ? Blocks.TUFF : Blocks.CALCITE);
            }
        }
        for (int i = 0; i < 16; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius + 5 + random.nextDouble() * 28;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
            int h = 6 + random.nextInt(12);
            double tilt = random.nextDouble() - 0.5;
            for (int y = 0; y < h; y++) {
                Structures.disc(level, base.offset((int) Math.round(y * tilt * 0.4), y, 0), 1.6 - y * 0.06, y % 3 == 0 ? Blocks.STAINED_GLASS.lightBlue() : Blocks.AMETHYST_BLOCK);
            }
        }
    }

    private void ruinedCity(ServerLevel level, BlockPos center, RandomSource random) {
        floor(level, center, 58, Blocks.CONCRETE.gray(), Blocks.STONE, 3);
        for (int i = 0; i < 300; i++) {
            int dx = random.nextInt(117) - 58;
            int dz = random.nextInt(117) - 58;
            if (dx * dx + dz * dz <= 58 * 58) {
                Structures.set(level, center.offset(dx, -1, dz), random.nextInt(3) == 0 ? Blocks.CONCRETE.black() : random.nextBoolean() ? Blocks.COBBLESTONE : Blocks.GRAVEL);
            }
        }
        for (int i = 0; i < 10; i++) {
            double a = Math.PI * 2 * i / 10 + random.nextDouble() * 0.3;
            double r = radius + 10 + random.nextDouble() * 20;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
            int w = 4 + random.nextInt(3);
            int h = 8 + random.nextInt(16);
            for (int dx = -w; dx <= w; dx++) {
                for (int dz = -w; dz <= w; dz++) {
                    if (Math.abs(dx) != w && Math.abs(dz) != w) {
                        continue;
                    }
                    int top = h - random.nextInt(Math.max(1, h / 2));
                    for (int y = 0; y < top; y++) {
                        if (y % 4 == 2 && Math.floorMod(dx + dz, 3) == 0) {
                            continue;
                        }
                        Structures.set(level, base.offset(dx, y, dz), random.nextInt(9) == 0 ? Blocks.CRACKED_STONE_BRICKS : Blocks.CONCRETE.lightGray());
                    }
                }
            }
            for (int d = 0; d < 6; d++) {
                Structures.set(level, base.offset(random.nextInt(w * 2 + 5) - w - 2, 0, random.nextInt(w * 2 + 5) - w - 2), Blocks.COBBLESTONE);
            }
        }
    }

    private void glacier(ServerLevel level, BlockPos center, RandomSource random) {
        hills(level, center, 58, Blocks.SNOW_BLOCK, Blocks.PACKED_ICE, 8.0);
        for (int i = 0; i < 120; i++) {
            int dx = random.nextInt(117) - 58;
            int dz = random.nextInt(117) - 58;
            if (dx * dx + dz * dz <= 22 * 22) {
                Structures.set(level, center.offset(dx, -1, dz), random.nextBoolean() ? Blocks.BLUE_ICE : Blocks.PACKED_ICE);
            }
        }
        for (int i = 0; i < 14; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius + 6 + random.nextDouble() * 26;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
            int h = 8 + random.nextInt(14);
            for (int y = 0; y < h; y++) {
                Structures.disc(level, base.above(y), 2.2 - y * 2.0 / h, y % 5 == 4 ? Blocks.BLUE_ICE : Blocks.PACKED_ICE);
            }
        }
    }

    private void lookout(ServerLevel level, BlockPos center) {
        for (int dx = -26; dx <= 26; dx++) {
            for (int dz = -40; dz <= 14; dz++) {
                for (int up = -18; up <= 18; up++) {
                    Structures.set(level, center.offset(dx, up, dz), Blocks.AIR);
                }
            }
        }
        Landmarks.lookout(level, center.offset(0, -1, -12), false);
    }

    private static void floor(ServerLevel level, BlockPos center, int r, Block surface, Block fill, int depth) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r + r) {
                    continue;
                }
                BlockPos top = center.offset(dx, -1, dz);
                for (int up = 1; up <= 14; up++) {
                    Structures.set(level, top.above(up), Blocks.AIR);
                }
                Structures.column(level, top, surface, fill, depth);
            }
        }
    }

    private void mountain(ServerLevel level, BlockPos center, RandomSource random) {
        floor(level, center, radius, Blocks.STONE, Blocks.STONE, 4);
        for (int i = 0; i < 90; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            if (dx * dx + dz * dz <= radius * radius) {
                Structures.set(level, center.offset(dx, -1, dz), random.nextBoolean() ? Blocks.GRAVEL : Blocks.ANDESITE);
            }
        }
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6 + 0.3;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * (radius - 3)), 0, (int) Math.round(Math.sin(a) * (radius - 3)));
            int h = 3 + random.nextInt(5);
            for (int y = 0; y < h; y++) {
                Structures.set(level, base.above(y), ((y + i) & 1) == 0 ? Blocks.STONE : Blocks.ANDESITE);
                Structures.set(level, base.above(y).east(), Blocks.STONE);
            }
        }
    }

    private void kameIsland(ServerLevel level, BlockPos center) {
        int sea = 56;
        for (int dx = -sea; dx <= sea; dx++) {
            for (int dz = -sea; dz <= sea; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > sea) {
                    continue;
                }
                BlockPos top = center.offset(dx, -1, dz);
                boolean house = Math.abs(dx) <= 10 && dz >= 18 && dz <= 34;
                if (d <= radius + 0.5 || house) {
                    Structures.column(level, top, d <= 6 && !house ? Blocks.GRASS_BLOCK : Blocks.SAND, Blocks.SANDSTONE, 3);
                } else {
                    Structures.set(level, top, Blocks.WATER);
                    Structures.set(level, top.below(), Blocks.WATER);
                    Structures.set(level, top.below(2), Blocks.SAND);
                }
                for (int up = 1; up <= 14; up++) {
                    Structures.set(level, top.above(up), Blocks.AIR);
                }
            }
        }
        Landmarks.kameHouseBuilding(level, center.offset(0, 0, 25));
        Structures.palm(level, center.offset(-10, 0, -6), 7);
        Structures.palm(level, center.offset(11, 0, 3), 6);
    }

    private void korinForest(ServerLevel level, BlockPos center, RandomSource random) {
        floor(level, center, 52, Blocks.GRASS_BLOCK, Blocks.DIRT, 3);
        BlockPos pillar = center.offset(0, 0, -38);
        BlockPos npc = Landmarks.korinTower(level, pillar, center.getY() - 1, 92, false);
        Landmarks.lookout(level, new BlockPos(npc.getX(), Math.min(level.getMaxY() - 18, npc.getY() + 46), npc.getZ() - 6), false);
        for (int i = 0; i < 70; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius + 4 + random.nextDouble() * 28;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
            if (base.distSqr(pillar) < 64) {
                continue;
            }
            if (random.nextInt(3) == 0) {
                Structures.conifer(level, base);
            } else {
                Structures.tree(level, base);
            }
        }
    }

    private void timeChamber(ServerLevel level, BlockPos center) {
        floor(level, center, 60, Blocks.CONCRETE.white(), Blocks.CONCRETE.white(), 2);
        BlockPos e = center.offset(0, 0, -radius - 8);
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int y = 0; y < 7; y++) {
                    boolean edge = Math.abs(dx) == 6 || Math.abs(dz) == 4 || y == 6;
                    Structures.set(level, e.offset(dx, y, dz), edge ? Blocks.QUARTZ_BLOCK : Blocks.AIR);
                }
            }
        }
        for (int x : new int[]{-5, -2, 2, 5}) {
            for (int y = 0; y < 6; y++) {
                Structures.set(level, e.offset(x, y, 5), Blocks.QUARTZ_PILLAR);
            }
        }
        for (int dx = -6; dx <= 6; dx++) {
            Structures.set(level, e.offset(dx, 6, 5), Blocks.QUARTZ_BLOCK);
        }
        for (int y = 0; y < 3; y++) {
            Structures.set(level, e.offset(0, y, 4), Blocks.AIR);
        }
        for (int side : new int[]{-1, 1}) {
            BlockPos glass = e.offset(side * 9, 0, 4);
            Structures.set(level, glass, Blocks.CONCRETE.yellow());
            Structures.set(level, glass.above(), Blocks.GLASS);
            Structures.set(level, glass.above(2), Blocks.SAND);
            Structures.set(level, glass.above(3), Blocks.GLASS);
            Structures.set(level, glass.above(4), Blocks.CONCRETE.yellow());
        }
    }

    private void namek(ServerLevel level, BlockPos center, RandomSource random) {
        int outer = 58;
        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > outer) {
                    continue;
                }
                BlockPos top = center.offset(dx, -1, dz);
                for (int up = 1; up <= 14; up++) {
                    Structures.set(level, top.above(up), Blocks.AIR);
                }
                double islands = Math.sin(dx * 0.21) * Math.cos(dz * 0.17);
                if (d <= radius + 0.5 || islands > 0.55) {
                    Structures.column(level, top, Blocks.MOSS_BLOCK, Blocks.DIRT, 3);
                } else {
                    Structures.set(level, top, Blocks.WATER);
                    Structures.set(level, top.below(), Blocks.WATER);
                    Structures.set(level, top.below(2), Blocks.SAND);
                }
            }
        }
        for (int i = 0; i < 14; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = radius + 3 + random.nextDouble() * 12;
            ajisa(level, center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r)), 5 + random.nextInt(3));
        }
        for (int i = 0; i < 5; i++) {
            double a = Math.PI * 2 * i / 5 + 0.4;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * 44), -1, (int) Math.round(Math.sin(a) * 44));
            int h = 10 + random.nextInt(10);
            for (int y = 0; y < h; y++) {
                Structures.disc(level, base.above(y), y > h - 4 ? 4.5 : 3.0, y > h - 4 ? Blocks.MOSS_BLOCK : Blocks.CALCITE);
            }
        }
    }

    private static void ajisa(ServerLevel level, BlockPos base, int height) {
        for (int y = 0; y < height; y++) {
            Structures.set(level, base.above(y), Blocks.STRIPPED_BIRCH_LOG);
        }
        BlockPos top = base.above(height);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx * dx + dy * dy * 2 + dz * dz <= 5) {
                        Structures.set(level, top.offset(dx, dy, dz), Structures.leaves(Blocks.AZALEA_LEAVES));
                    }
                }
            }
        }
    }

    private void cellRing(ServerLevel level, BlockPos center, RandomSource random) {
        floor(level, center, 54, Blocks.COARSE_DIRT, Blocks.DIRT, 3);
        for (int i = 0; i < 160; i++) {
            int dx = random.nextInt(109) - 54;
            int dz = random.nextInt(109) - 54;
            if (dx * dx + dz * dz < 26 * 26 || dx * dx + dz * dz > 54 * 54) {
                continue;
            }
            Structures.set(level, center.offset(dx, -1, dz), random.nextBoolean() ? Blocks.GRAVEL : Blocks.TERRACOTTA);
        }
        int half = 14;
        for (int dx = -half - 1; dx <= half + 1; dx++) {
            for (int dz = -half - 1; dz <= half + 1; dz++) {
                boolean edge = Math.abs(dx) > half || Math.abs(dz) > half;
                boolean grid = Math.floorMod(dx, 4) == 2 || Math.floorMod(dz, 4) == 2;
                BlockPos top = center.offset(dx, edge ? -2 : -1, dz);
                Structures.set(level, top, edge ? Blocks.SMOOTH_STONE : grid ? Blocks.CONCRETE.lightGray() : Blocks.SMOOTH_QUARTZ);
                Structures.set(level, top.below(), Blocks.STONE_BRICKS);
                Structures.set(level, top.below(2), Blocks.STONE_BRICKS);
                if (edge) {
                    Structures.set(level, top.above(), Blocks.AIR);
                }
            }
        }
        for (int[] c : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            BlockPos base = center.offset(c[0] * (half + 3), -1, c[1] * (half + 3));
            for (int y = 0; y < 3; y++) {
                Structures.disc(level, base.above(y), 1.5, Blocks.SMOOTH_QUARTZ);
            }
            Structures.disc(level, base.above(3), 1.0, Blocks.CONCRETE.white());
            for (int y = 4; y < 17; y++) {
                Structures.set(level, base.above(y), Blocks.QUARTZ_PILLAR);
            }
            Structures.set(level, base.above(17), Blocks.END_ROD);
        }
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            BlockPos base = center.offset((int) Math.round(Math.cos(a) * 42), -1, (int) Math.round(Math.sin(a) * 42));
            int h = 8 + random.nextInt(8);
            for (int y = 0; y < h; y++) {
                Structures.disc(level, base.above(y), 3.5 - y * 0.12, (y & 3) == 0 ? Blocks.DYED_TERRACOTTA.orange() : Blocks.TERRACOTTA);
            }
        }
    }

    private void canyon(ServerLevel level, BlockPos center, RandomSource random) {
        floor(level, center, 50, Blocks.RED_SAND, Blocks.RED_SANDSTONE, 3);
        for (int dx = -50; dx <= 50; dx++) {
            for (int dz = -50; dz <= 50; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d < radius + 6 || d > 50) {
                    continue;
                }
                double noise = Math.sin(dx * 0.31) * 3 + Math.cos(dz * 0.27) * 3;
                int h = (int) Math.round(Math.min(26, (d - radius - 6) * 1.6 + 6 + noise));
                for (int y = 0; y < h; y++) {
                    Block layer = switch ((y / 3) % 4) {
                        case 0 -> Blocks.TERRACOTTA;
                        case 1 -> Blocks.DYED_TERRACOTTA.orange();
                        case 2 -> Blocks.DYED_TERRACOTTA.red();
                        default -> Blocks.DYED_TERRACOTTA.brown();
                    };
                    Structures.set(level, center.offset(dx, y, dz), layer);
                }
            }
        }
    }
}
