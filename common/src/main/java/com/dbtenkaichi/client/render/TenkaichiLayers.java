package com.dbtenkaichi.client.render;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.config.ClientConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.player.HairStyle;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.race.RacePassive;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TenkaichiLayers {
    public static final Identifier HAIR_TEXTURE = Tenkaichi.id("textures/entity/hair.png");
    public static final Identifier TAIL_TEXTURE = Tenkaichi.id("textures/entity/tail.png");
    public static final Identifier AURA_TEXTURE = Tenkaichi.id("textures/entity/aura_swirl.png");
    public static final Identifier FEATURE_TEXTURE = Tenkaichi.id("textures/entity/feature.png");
    public static final Identifier THICK_TAIL_TEXTURE = Tenkaichi.id("textures/entity/thick_tail.png");
    public static final Identifier ARMOR_PAD_TEXTURE = Tenkaichi.id("textures/entity/armor_pad.png");

    private TenkaichiLayers() {
    }

    public static List<RenderLayer<AvatarRenderState, PlayerModel>> create(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet models) {
        return List.of(new TailLayer(parent, models), new FeatureLayer(parent, models), new GearLayer(parent, models), new HairLayer(parent, models),
                new AuraLayer(parent, models));
    }

    public static @Nullable EntityVisual visual(AvatarRenderState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        Entity entity = mc.level.getEntity(state.id);
        if (entity == null) {
            return null;
        }
        EntityVisual visual = ClientState.forEntity(entity);
        return visual.created ? visual : null;
    }

    public static final class HairModel extends PlayerModel {
        public HairModel(ModelPart root) {
            super(root, false);
        }
    }

    public static final class TailModel extends PlayerModel {
        private final ModelPart[] segments;
        private final float sway;

        public TailModel(ModelPart root, int count, float sway) {
            super(root, false);
            this.segments = new ModelPart[count];
            this.sway = sway;
            ModelPart current = root.getChild("body");
            for (int i = 0; i < segments.length; i++) {
                current = current.getChild("tail_" + i);
                segments[i] = current;
            }
        }

        @Override
        public void setupAnim(AvatarRenderState state) {
            super.setupAnim(state);
            float age = state.ageInTicks;
            float speed = Math.min(1.0f, state.walkAnimationSpeed);
            for (int i = 0; i < segments.length; i++) {
                ModelPart part = segments[i];
                part.zRot = (float) Math.sin(age * 0.11f + i * 0.65f) * (0.12f + i * 0.04f) * sway;
                part.xRot += ((float) Math.sin(age * 0.07f + i * 0.4f) * 0.06f + speed * 0.12f) * sway;
            }
        }
    }

    static final class HairLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        private final Map<HairStyle, HairModel> models = new EnumMap<>(HairStyle.class);

        HairLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet set) {
            super(parent);
            TenkaichiModels.HAIR.forEach((style, layer) -> models.put(style, new HairModel(set.bakeLayer(layer))));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
            try {
                if (state.isInvisible) {
                    return;
                }
                EntityVisual visual = visual(state);
                if (visual == null) {
                    return;
                }
                HairStyle style = visual.hairStyle();
                HairModel model = models.get(style);
                if (model == null) {
                    return;
                }
                int color = 0xFF000000 | visual.hairColor();
                int useLight = visual.hairGlow() ? LightCoordsUtil.FULL_BRIGHT : light;
                collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(HAIR_TEXTURE), useLight, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
            } catch (RuntimeException e) {
                RenderGuard.report("hair", e);
            }
        }
    }

    static final class TailLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        private final TailModel model;
        private final TailModel thick;

        TailLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet set) {
            super(parent);
            this.model = new TailModel(set.bakeLayer(TenkaichiModels.TAIL), TenkaichiModels.TAIL_SEGMENTS, 1.0f);
            this.thick = new TailModel(set.bakeLayer(TenkaichiModels.THICK_TAIL), TenkaichiModels.THICK_TAIL_SEGMENTS, 0.6f);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
            try {
                if (state.isInvisible) {
                    return;
                }
                EntityVisual visual = visual(state);
                if (visual == null) {
                    return;
                }
                Race race = visual.race;
                if (race == Race.FRIEZA_RACE || race == Race.BIO_ANDROID && visual.appearance.tail) {
                    int skin = visual.appearance.skinColor;
                    int color = 0xFF000000 | (race == Race.BIO_ANDROID ? EntityVisual.mix(skin, visual.appearance.featureColor, 0.35f) : skin);
                    collector.submitModel(thick, state, poseStack, RenderTypes.entityCutout(THICK_TAIL_TEXTURE), light, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
                    return;
                }
                if (!visual.appearance.tail || !race.has(RacePassive.TAIL) || !race.hasHair()) {
                    return;
                }
                int color = 0xFF000000 | (visual.formDefinition() == null ? 0x5A3418 : visual.hairColor());
                collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(TAIL_TEXTURE), light, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
            } catch (RuntimeException e) {
                RenderGuard.report("tail", e);
            }
        }
    }

    static final class FeatureLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        private final Map<Race, Map<TenkaichiModels.FeatureTint, HairModel>> models = new EnumMap<>(Race.class);

        FeatureLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet set) {
            super(parent);
            TenkaichiModels.FEATURES.forEach((race, byTint) -> {
                Map<TenkaichiModels.FeatureTint, HairModel> baked = new EnumMap<>(TenkaichiModels.FeatureTint.class);
                byTint.forEach((tint, location) -> baked.put(tint, new HairModel(set.bakeLayer(location))));
                models.put(race, baked);
            });
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
            try {
                if (state.isInvisible) {
                    return;
                }
                EntityVisual visual = visual(state);
                if (visual == null || visual.appearance.useOwnSkin) {
                    return;
                }
                Map<TenkaichiModels.FeatureTint, HairModel> byTint = models.get(visual.race);
                if (byTint == null) {
                    return;
                }
                for (Map.Entry<TenkaichiModels.FeatureTint, HairModel> entry : byTint.entrySet()) {
                    int color = switch (entry.getKey()) {
                        case SKIN -> visual.appearance.skinColor;
                        case ACCENT -> visual.appearance.featureColor;
                        case HORN -> visual.race == Race.FRIEZA_RACE ? 0xE8E2D4 : visual.appearance.featureColor;
                    };
                    collector.submitModel(entry.getValue(), state, poseStack, RenderTypes.entityCutout(FEATURE_TEXTURE), light, OverlayTexture.NO_OVERLAY,
                            0xFF000000 | color, null, state.outlineColor);
                }
            } catch (RuntimeException e) {
                RenderGuard.report("feature", e);
            }
        }
    }

    static final class GearLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        private final HairModel pads;
        private final HairModel beard;

        GearLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet set) {
            super(parent);
            this.pads = new HairModel(set.bakeLayer(TenkaichiModels.SHOULDER_PADS));
            this.beard = new HairModel(set.bakeLayer(TenkaichiModels.BEARD));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
            try {
                if (state.isInvisible) {
                    return;
                }
                EntityVisual visual = visual(state);
                if (visual == null) {
                    return;
                }
                if (visual.appearance.outfit == 1 && !visual.appearance.useOwnSkin) {
                    collector.submitModel(pads, state, poseStack, RenderTypes.entityCutout(ARMOR_PAD_TEXTURE), light, OverlayTexture.NO_OVERLAY, 0xFFEDE7D6, null, state.outlineColor);
                }
                if (visual.beard) {
                    collector.submitModel(beard, state, poseStack, RenderTypes.entityCutout(HAIR_TEXTURE), light, OverlayTexture.NO_OVERLAY, 0xFFEDEDED, null, state.outlineColor);
                }
            } catch (RuntimeException e) {
                RenderGuard.report("gear", e);
            }
        }
    }

    static final class AuraLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        private final PlayerModel shell;

        AuraLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet set) {
            super(parent);
            this.shell = new PlayerModel(set.bakeLayer(TenkaichiModels.AURA_SHELL), false);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
            try {
                EntityVisual visual = visual(state);
                if (visual == null) {
                    return;
                }
                float partial = com.dbtenkaichi.client.anim.PoseApplier.partial();
                float level = visual.auraO + (visual.aura - visual.auraO) * partial;
                if (level < 0.02f) {
                    return;
                }
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && state.id == mc.player.getId() && mc.options.getCameraType().isFirstPerson() && !ConfigManager.client().auraInFirstPerson) {
                    return;
                }
                int primary = visual.auraColor();
                int secondary = visual.auraSecondary();
                float time = state.ageInTicks;
                float swirlAlpha = Math.min(1.0f, level) * 0.3f;
                int swirlColor = argb(swirlAlpha, primary);
                collector.order(1).submitModel(shell, state, poseStack, RenderTypes.energySwirl(AURA_TEXTURE, (time * 0.004f) % 1.0f, (time * -0.02f) % 1.0f),
                        LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, swirlColor, null, 0);
                ClientConfig.Quality quality = ConfigManager.client().quality();
                float scale = visual.auraScale();
                long seed = state.id * 31L;
                collector.order(2).submitCustomGeometry(poseStack, RenderTypes.lightning(),
                        (pose, buffer) -> AuraMesh.flames(pose.pose(), buffer, quality.auraFlames, quality.auraLayers, level, scale, time, primary, secondary, seed));
            } catch (RuntimeException e) {
                RenderGuard.report("aura", e);
            }
        }

        static int argb(float alpha, int rgb) {
            int a = Math.max(0, Math.min(255, Math.round(alpha * 255)));
            return a << 24 | (rgb & 0xFFFFFF);
        }
    }

    public static final class AuraMesh {
        private AuraMesh() {
        }

        public static void flames(Matrix4fc pose, VertexConsumer buffer, int flames, int layers, float level, float scale, float time, int primary, int secondary, long seed) {
            float intensity = Math.min(1.25f, level);
            int segments = 6;
            for (int layer = 0; layer < layers; layer++) {
                float layerScale = 1.0f + layer * 0.22f;
                float layerAlpha = (0.2f - layer * 0.035f) * intensity;
                int count = flames + layer * 2;
                for (int i = 0; i < count; i++) {
                    float phase = (float) ((seed + i * 977L + layer * 131L) % 1000) / 1000.0f;
                    double angle = Math.PI * 2 * i / count + time * (0.015 + layer * 0.006) + phase;
                    float radius = (0.42f + phase * 0.12f) * scale * layerScale;
                    float height = (2.2f + intensity * 0.9f + (float) Math.sin(time * 0.2f + phase * 6.0f) * 0.25f) * scale * (0.85f + layer * 0.1f);
                    float width = (0.24f + phase * 0.1f) * scale * layerScale;
                    float bottom = 1.62f * scale;
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double tx = -sin;
                    double tz = cos;
                    for (int s = 0; s < segments; s++) {
                        float t0 = (float) s / segments;
                        float t1 = (float) (s + 1) / segments;
                        float y0 = bottom - height * t0;
                        float y1 = bottom - height * t1;
                        float bulge0 = (float) Math.sin(t0 * Math.PI) * 0.18f * scale;
                        float bulge1 = (float) Math.sin(t1 * Math.PI) * 0.18f * scale;
                        float wave0 = (float) Math.sin(time * 0.35f + phase * 9.0f + t0 * 5.0f) * 0.09f * scale * t0;
                        float wave1 = (float) Math.sin(time * 0.35f + phase * 9.0f + t1 * 5.0f) * 0.09f * scale * t1;
                        float r0 = radius + bulge0 - t0 * t0 * radius * 0.55f;
                        float r1 = radius + bulge1 - t1 * t1 * radius * 0.55f;
                        float w0 = width * (1.0f - t0 * 0.85f);
                        float w1 = width * (1.0f - t1 * 0.85f);
                        float a0 = layerAlpha * (1.0f - t0 * 0.9f);
                        float a1 = layerAlpha * (1.0f - t1 * 0.9f);
                        int c0 = lerp(primary, secondary, t0 * 0.7f);
                        int c1 = lerp(primary, secondary, t1 * 0.7f);
                        float cx0 = (float) (cos * r0 + tx * wave0);
                        float cz0 = (float) (sin * r0 + tz * wave0);
                        float cx1 = (float) (cos * r1 + tx * wave1);
                        float cz1 = (float) (sin * r1 + tz * wave1);
                        quad(pose, buffer,
                                cx0 - (float) tx * w0, y0, cz0 - (float) tz * w0,
                                cx0 + (float) tx * w0, y0, cz0 + (float) tz * w0,
                                cx1 + (float) tx * w1, y1, cz1 + (float) tz * w1,
                                cx1 - (float) tx * w1, y1, cz1 - (float) tz * w1,
                                c0, a0, c1, a1);
                    }
                }
            }
        }

        static int lerp(int a, int b, float t) {
            return EntityVisual.mix(a, b, Math.max(0.0f, Math.min(1.0f, t)));
        }

        static void quad(Matrix4fc pose, VertexConsumer buffer, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3,
                         int c0, float a0, int c1, float a1) {
            vertex(pose, buffer, x0, y0, z0, c0, a0);
            vertex(pose, buffer, x1, y1, z1, c0, a0);
            vertex(pose, buffer, x2, y2, z2, c1, a1);
            vertex(pose, buffer, x3, y3, z3, c1, a1);
            vertex(pose, buffer, x3, y3, z3, c1, a1);
            vertex(pose, buffer, x2, y2, z2, c1, a1);
            vertex(pose, buffer, x1, y1, z1, c0, a0);
            vertex(pose, buffer, x0, y0, z0, c0, a0);
        }

        static void vertex(Matrix4fc pose, VertexConsumer buffer, float x, float y, float z, int rgb, float alpha) {
            buffer.addVertex(pose, x, y, z).setColor((rgb >> 16 & 255) / 255.0f, (rgb >> 8 & 255) / 255.0f, (rgb & 255) / 255.0f, Math.max(0.0f, Math.min(1.0f, alpha)));
        }
    }
}
