package com.dbtenkaichi.client.render;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.player.HairStyle;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class TenkaichiModels {
    public static final Map<HairStyle, ModelLayerLocation> HAIR = new EnumMap<>(HairStyle.class);
    public static final ModelLayerLocation TAIL = new ModelLayerLocation(Tenkaichi.id("tail"), "main");
    public static final ModelLayerLocation AURA_SHELL = new ModelLayerLocation(Tenkaichi.id("aura_shell"), "main");
    public static final ModelLayerLocation FIGHTER = new ModelLayerLocation(Tenkaichi.id("fighter"), "main");
    private static final float PI = (float) Math.PI;

    static {
        for (HairStyle style : HairStyle.values()) {
            if (style != HairStyle.BALD) {
                HAIR.put(style, new ModelLayerLocation(Tenkaichi.id("hair_" + style.id()), "main"));
            }
        }
    }

    private TenkaichiModels() {
    }

    public static void registerLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar) {
        for (Map.Entry<HairStyle, ModelLayerLocation> entry : HAIR.entrySet()) {
            HairStyle style = entry.getKey();
            registrar.accept(entry.getValue(), () -> hair(style));
        }
        registrar.accept(TAIL, TenkaichiModels::tail);
        registrar.accept(AURA_SHELL, () -> LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.45f), false), 64, 64));
        registrar.accept(FIGHTER, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64));
    }

    private static MeshDefinition emptyPlayerMesh() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        mesh.getRoot().clearRecursively();
        return mesh;
    }

    public static LayerDefinition hair(HairStyle style) {
        MeshDefinition mesh = emptyPlayerMesh();
        PartDefinition head = mesh.getRoot().getChild("head");
        Builder b = new Builder(head);
        switch (style) {
            case WILD_SPIKES -> {
                b.cap(true);
                b.spike(-2.6f, -7.6f, -3.6f, 2.35f, 0.15f, 0.0f, 4.5f, 2.6f);
                b.spike(0.2f, -7.8f, -3.8f, 2.2f, 0.0f, 0.0f, 5.0f, 2.8f);
                b.spike(2.7f, -7.6f, -3.4f, 2.4f, -0.2f, 0.0f, 4.0f, 2.4f);
                b.spike(-3.0f, -8.0f, 0.5f, -0.55f, 0.0f, 0.45f, 7.0f, 3.2f);
                b.spike(-1.0f, -8.2f, 1.0f, -0.35f, 0.0f, 0.12f, 7.5f, 3.4f);
                b.spike(1.2f, -8.2f, 1.0f, -0.4f, 0.0f, -0.15f, 7.2f, 3.4f);
                b.spike(3.0f, -8.0f, 0.5f, -0.6f, 0.0f, -0.5f, 6.5f, 3.0f);
                b.spike(0.0f, -6.5f, 3.6f, -1.25f, 0.0f, 0.0f, 6.0f, 3.2f);
                b.spike(-4.2f, -6.2f, -0.5f, 0.0f, 0.0f, 1.25f, 4.2f, 2.6f);
                b.spike(4.2f, -6.2f, -0.5f, 0.0f, 0.0f, -1.25f, 4.2f, 2.6f);
                b.spike(-2.5f, -4.5f, 3.8f, -2.2f, 0.0f, 0.4f, 4.5f, 2.6f);
                b.spike(2.5f, -4.5f, 3.8f, -2.2f, 0.0f, -0.4f, 4.5f, 2.6f);
            }
            case SUPER_SPIKES -> {
                b.cap(true);
                b.spike(-1.2f, -7.8f, -3.9f, 2.55f, 0.1f, 0.15f, 5.0f, 2.4f);
                b.spike(-3.2f, -8.0f, 0.0f, -0.32f, 0.0f, 0.35f, 9.0f, 3.4f);
                b.spike(-1.2f, -8.3f, 0.6f, -0.18f, 0.0f, 0.1f, 10.0f, 3.6f);
                b.spike(1.2f, -8.3f, 0.6f, -0.2f, 0.0f, -0.12f, 9.6f, 3.6f);
                b.spike(3.2f, -8.0f, 0.0f, -0.34f, 0.0f, -0.38f, 8.6f, 3.4f);
                b.spike(0.0f, -7.5f, 2.8f, -0.6f, 0.0f, 0.0f, 8.4f, 3.4f);
                b.spike(-2.4f, -6.0f, 3.6f, -0.95f, 0.0f, 0.35f, 6.8f, 3.0f);
                b.spike(2.4f, -6.0f, 3.6f, -0.95f, 0.0f, -0.35f, 6.8f, 3.0f);
                b.spike(-4.2f, -6.8f, -0.5f, 0.0f, 0.0f, 0.75f, 5.0f, 2.6f);
                b.spike(4.2f, -6.8f, -0.5f, 0.0f, 0.0f, -0.75f, 5.0f, 2.6f);
                b.spike(-1.8f, -7.6f, -2.0f, -0.12f, 0.0f, 0.25f, 7.0f, 3.0f);
                b.spike(1.8f, -7.6f, -2.0f, -0.12f, 0.0f, -0.25f, 7.0f, 3.0f);
            }
            case ASCENDED_SPIKES -> {
                b.cap(true);
                b.spike(-0.6f, -7.9f, -3.9f, 2.7f, 0.0f, 0.05f, 6.0f, 2.0f);
                for (int i = 0; i < 6; i++) {
                    float x = -3.4f + i * 1.36f;
                    b.spike(x, -8.2f, 0.4f, -0.22f - (i % 2) * 0.1f, 0.0f, -x * 0.11f, 10.0f + (i % 2) * 1.2f, 2.8f);
                }
                b.spike(0.0f, -7.0f, 3.2f, -0.7f, 0.0f, 0.0f, 9.0f, 3.0f);
                b.spike(-2.6f, -5.6f, 3.6f, -1.05f, 0.0f, 0.4f, 7.5f, 2.6f);
                b.spike(2.6f, -5.6f, 3.6f, -1.05f, 0.0f, -0.4f, 7.5f, 2.6f);
                b.spike(-4.2f, -6.8f, -0.8f, 0.0f, 0.0f, 0.7f, 5.5f, 2.2f);
                b.spike(4.2f, -6.8f, -0.8f, 0.0f, 0.0f, -0.7f, 5.5f, 2.2f);
            }
            case FLAME_UP -> {
                b.cap(true);
                b.spike(0.0f, -8.2f, -3.8f, -0.15f, 0.0f, 0.0f, 3.5f, 2.2f);
                for (int i = 0; i < 7; i++) {
                    float x = -3.3f + i * 1.1f;
                    b.spike(x, -8.3f, -1.2f + (i % 2) * 1.4f, -0.18f - (i % 3) * 0.06f, 0.0f, -x * 0.1f, 8.0f + (i % 2) * 1.5f, 3.0f);
                }
                b.spike(-2.0f, -7.8f, 2.4f, -0.55f, 0.0f, 0.2f, 7.5f, 3.0f);
                b.spike(2.0f, -7.8f, 2.4f, -0.55f, 0.0f, -0.2f, 7.5f, 3.0f);
                b.spike(-4.2f, -7.0f, -1.5f, -0.1f, 0.0f, 0.45f, 4.0f, 2.2f);
                b.spike(4.2f, -7.0f, -1.5f, -0.1f, 0.0f, -0.45f, 4.0f, 2.2f);
            }
            case LONG_MANE, LEGENDARY_MANE -> {
                boolean legend = style == HairStyle.LEGENDARY_MANE;
                float length = legend ? 30.0f : 20.0f;
                b.cap(true);
                b.slab("mane", -4.8f, -8.4f, 2.6f, 9.6f, length, 3.4f, 0.12f);
                for (int i = 0; i < 4; i++) {
                    float x = -3.3f + i * 2.2f;
                    b.spike(x, -8.4f + length - 1.0f, 3.6f + (float) Math.sin(length) * 0.05f, PI - 0.25f, 0.0f, (i - 1.5f) * 0.18f, 5.0f, 3.0f);
                }
                if (legend) {
                    b.spike(-1.6f, -8.0f, -3.4f, -0.2f, 0.0f, 0.25f, 6.0f, 3.0f);
                    b.spike(1.6f, -8.0f, -3.4f, -0.2f, 0.0f, -0.25f, 6.0f, 3.0f);
                } else {
                    b.spike(-2.4f, -7.8f, -3.7f, 2.4f, 0.1f, 0.0f, 3.5f, 2.4f);
                    b.spike(1.0f, -8.0f, -3.8f, 2.3f, 0.0f, 0.0f, 4.0f, 2.6f);
                    b.spike(-4.4f, -6.0f, 0.0f, 0.3f, 0.0f, 1.1f, 4.0f, 2.4f);
                    b.spike(4.4f, -6.0f, 0.0f, 0.3f, 0.0f, -1.1f, 4.0f, 2.4f);
                }
            }
            case SHORT_NEAT -> {
                b.cap(false);
                b.slab("fringe", -4.6f, -8.4f, -4.7f, 9.2f, 2.2f, 1.2f, 0.0f);
            }
            case SIDE_SWEPT -> {
                b.cap(false);
                b.spike(-2.8f, -7.9f, -3.9f, 2.0f, 0.0f, -0.75f, 4.5f, 2.4f);
                b.spike(-0.6f, -8.1f, -3.9f, 2.0f, 0.0f, -0.7f, 4.8f, 2.4f);
                b.spike(1.6f, -8.0f, -3.9f, 2.05f, 0.0f, -0.65f, 4.4f, 2.2f);
                b.spike(4.3f, -7.0f, -1.0f, 0.1f, 0.0f, -1.2f, 3.6f, 2.2f);
            }
            case PONYTAIL -> {
                b.cap(false);
                b.slab("fringe", -4.6f, -8.4f, -4.7f, 9.2f, 1.8f, 1.2f, 0.0f);
                b.slab("tie", -1.2f, -7.4f, 4.2f, 2.4f, 2.4f, 1.6f, 0.0f);
                b.spike(0.0f, -6.2f, 5.0f, PI - 0.35f, 0.0f, 0.0f, 11.0f, 3.2f);
            }
            default -> {
            }
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition tail() {
        MeshDefinition mesh = emptyPlayerMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        PartDefinition parent = body;
        float[] pitch = {0.85f, 0.35f, 0.2f, 0.1f, -0.1f, -0.3f};
        for (int i = 0; i < pitch.length; i++) {
            float size = 2.1f - i * 0.18f;
            CubeListBuilder cube = CubeListBuilder.create().texOffs(0, 0).addBox(-size / 2.0f, 0.0f, -size / 2.0f, size, 3.4f, size);
            PartPose pose = i == 0 ? PartPose.offsetAndRotation(0.0f, 10.5f, 2.0f, pitch[i], 0.0f, 0.0f) : PartPose.offsetAndRotation(0.0f, 3.2f, 0.0f, pitch[i], 0.0f, 0.0f);
            parent = parent.addOrReplaceChild("tail_" + i, cube, pose);
        }
        return LayerDefinition.create(mesh, 32, 16);
    }

    private static final class Builder {
        private final PartDefinition head;
        private int counter;

        Builder(PartDefinition head) {
            this.head = head;
        }

        void cap(boolean volume) {
            float lift = volume ? 0.9f : 0.5f;
            head.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-4.55f, -8.0f - lift, -4.55f, 9.1f, 3.0f + lift, 9.1f)
                    .texOffs(0, 12)
                    .addBox(-4.55f, -5.0f, 2.2f, 9.1f, 4.6f, 2.35f)
                    .addBox(-4.6f, -5.2f, -2.8f, 1.1f, 3.4f, 5.4f)
                    .addBox(3.5f, -5.2f, -2.8f, 1.1f, 3.4f, 5.4f), PartPose.ZERO);
        }

        void slab(String name, float x, float y, float z, float w, float h, float d, float tilt) {
            head.addOrReplaceChild(name + "_" + counter++, CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, w, h, d),
                    PartPose.offsetAndRotation(x, y, z, tilt, 0.0f, 0.0f));
        }

        void spike(float x, float y, float z, float xRot, float yRot, float zRot, float length, float base) {
            float l1 = length * 0.42f;
            float l2 = length * 0.33f;
            float l3 = length * 0.25f;
            float b2 = base * 0.66f;
            float b3 = base * 0.34f;
            CubeListBuilder cubes = CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-base / 2.0f, -l1, -base / 2.0f, base, l1, base)
                    .texOffs(16, 0).addBox(-b2 / 2.0f, -l1 - l2, -b2 / 2.0f, b2, l2, b2)
                    .texOffs(28, 0).addBox(-b3 / 2.0f, -l1 - l2 - l3, -b3 / 2.0f, b3, l3, b3);
            head.addOrReplaceChild("spike_" + counter++, cubes, PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot));
        }
    }
}
