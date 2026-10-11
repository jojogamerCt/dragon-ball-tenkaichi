package com.dbtenkaichi.client.anim;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.player.RuntimeState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PoseApplier {
    private PoseApplier() {
    }

    public static float partial() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static float now() {
        return ClientState.clientTicks + partial();
    }

    public static void tickVisual(Entity entity, EntityVisual visual) {
        int f = visual.flags;
        boolean[] active = new boolean[AnimPlayer.BASE_STATES];
        Vec3 motion = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        boolean moving = motion.lengthSqr() > 0.004;
        boolean flying = (f & RuntimeState.FLAG_FLYING) != 0;
        if ((f & RuntimeState.FLAG_TRANSFORMING) != 0) {
            active[AnimPlayer.BaseState.TRANSFORM.ordinal()] = true;
        } else if ((f & RuntimeState.FLAG_BEAM_FIRING) != 0 || (f & RuntimeState.FLAG_CLASHING) != 0) {
            active[AnimPlayer.BaseState.BEAM_FIRE.ordinal()] = true;
        } else if ((f & RuntimeState.FLAG_SKILL_CHARGING) != 0) {
            active[AnimPlayer.BaseState.SKILL_CHARGE.ordinal()] = true;
        } else if ((f & RuntimeState.FLAG_CHARGING) != 0) {
            active[AnimPlayer.BaseState.CHARGE.ordinal()] = true;
        } else if ((f & RuntimeState.FLAG_GUARDING) != 0) {
            active[AnimPlayer.BaseState.GUARD.ordinal()] = true;
        } else if ((f & RuntimeState.FLAG_STUNNED) != 0) {
            active[AnimPlayer.BaseState.STUNNED.ordinal()] = true;
        } else if ((f & RuntimeState.FLAG_TRAINING) != 0 && !moving && !flying) {
            active[AnimPlayer.BaseState.MEDITATE.ordinal()] = true;
        } else {
            if ((f & RuntimeState.FLAG_COMBAT_STANCE) != 0) {
                active[AnimPlayer.BaseState.STANCE.ordinal()] = true;
            }
        }
        if (flying && !active[AnimPlayer.BaseState.TRANSFORM.ordinal()] && !active[AnimPlayer.BaseState.MEDITATE.ordinal()]) {
            boolean fast = (f & RuntimeState.FLAG_FAST_FLIGHT) != 0 && motion.lengthSqr() > 0.25;
            if (fast) {
                active[AnimPlayer.BaseState.FLY_FAST.ordinal()] = true;
            } else if (moving) {
                active[AnimPlayer.BaseState.FLY.ordinal()] = true;
            } else {
                active[AnimPlayer.BaseState.HOVER.ordinal()] = true;
            }
        }
        visual.anim.tickWeights(active);
        visual.auraO = visual.aura;
        float target = visual.auraTarget(ClientState.clientTicks);
        visual.aura += (target - visual.aura) * (target > visual.aura ? 0.25f : 0.08f);
        visual.flightO = visual.flight;
        float flightTarget = flying ? 1.0f : 0.0f;
        visual.flight += (flightTarget - visual.flight) * 0.2f;
    }

    public static void apply(HumanoidModel<?> model, EntityVisual visual, float ageInTicks) {
        float partial = partial();
        float now = ClientState.clientTicks + partial;
        AnimPlayer anim = visual.anim;
        for (AnimPlayer.BaseState state : AnimPlayer.BaseState.values()) {
            float w = anim.weight(state, partial);
            if (w > 0.001f) {
                blend(model, Clips.base(state), w);
            }
        }
        float tremble = anim.weight(AnimPlayer.BaseState.CHARGE, partial) * 0.035f + anim.weight(AnimPlayer.BaseState.TRANSFORM, partial) * 0.07f;
        if (tremble > 0.0f) {
            model.rightArm.zRot += (float) Math.sin(ageInTicks * 2.7f) * tremble;
            model.leftArm.zRot -= (float) Math.sin(ageInTicks * 3.1f) * tremble;
            model.head.yRot += (float) Math.sin(ageInTicks * 3.7f) * tremble * 0.5f;
            model.body.zRot += (float) Math.sin(ageInTicks * 4.3f) * tremble * 0.25f;
        }
        float hover = anim.weight(AnimPlayer.BaseState.HOVER, partial);
        if (hover > 0.0f) {
            model.rightLeg.xRot += (float) Math.sin(ageInTicks * 0.09f) * 0.08f * hover;
            model.leftLeg.xRot += (float) Math.sin(ageInTicks * 0.09f + 1.3f) * 0.06f * hover;
        }
        float fast = anim.weight(AnimPlayer.BaseState.FLY_FAST, partial);
        float fly = anim.weight(AnimPlayer.BaseState.FLY, partial);
        float pitchComp = fast * 1.2f + fly * 0.55f;
        if (pitchComp > 0.0f) {
            model.head.xRot -= pitchComp;
        }
        if (anim.active(now)) {
            LimbPose pose = Clips.sample(anim.clip, anim.progress(now));
            if (pose != null) {
                blend(model, pose, anim.envelope(now));
            }
        }
        float by = model.body.yRot;
        if (Math.abs(by) > 1.0e-3f) {
            model.rightArm.z = (float) Math.sin(by) * 5.0f;
            model.rightArm.x = -(float) Math.cos(by) * 5.0f;
            model.leftArm.z = -(float) Math.sin(by) * 5.0f;
            model.leftArm.x = (float) Math.cos(by) * 5.0f;
            model.rightArm.yRot += by;
            model.leftArm.yRot += by;
        }
    }

    private static void blend(HumanoidModel<?> model, LimbPose pose, float weight) {
        if (pose == null || weight <= 0.0f) {
            return;
        }
        float w = Math.min(1.0f, weight);
        blendPart(model.head, pose, LimbPose.HEAD, w);
        blendPart(model.body, pose, LimbPose.BODY, w);
        blendPart(model.rightArm, pose, LimbPose.RIGHT_ARM, w);
        blendPart(model.leftArm, pose, LimbPose.LEFT_ARM, w);
        blendPart(model.rightLeg, pose, LimbPose.RIGHT_LEG, w);
        blendPart(model.leftLeg, pose, LimbPose.LEFT_LEG, w);
    }

    private static void blendPart(ModelPart part, LimbPose pose, int index, float w) {
        float x = pose.get(index * 3);
        float y = pose.get(index * 3 + 1);
        float z = pose.get(index * 3 + 2);
        if (!Float.isNaN(x)) {
            part.xRot = part.xRot + (x - part.xRot) * w;
        }
        if (!Float.isNaN(y)) {
            part.yRot = part.yRot + (y - part.yRot) * w;
        }
        if (!Float.isNaN(z)) {
            part.zRot = part.zRot + (z - part.zRot) * w;
        }
    }

    public static void applyBodyTransform(PoseStack poseStack, Entity entity, EntityVisual visual, float bodyRotDegrees, float height) {
        float partial = partial();
        AnimPlayer anim = visual.anim;
        float fast = anim.weight(AnimPlayer.BaseState.FLY_FAST, partial);
        float fly = anim.weight(AnimPlayer.BaseState.FLY, partial);
        float hover = anim.weight(AnimPlayer.BaseState.HOVER, partial);
        float meditate = anim.weight(AnimPlayer.BaseState.MEDITATE, partial);
        if (fast + fly + hover + meditate < 0.001f) {
            return;
        }
        Vec3 motion = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        double yaw = Math.toRadians(bodyRotDegrees);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double forward = motion.x * fx + motion.z * fz;
        double lateral = motion.x * fz - motion.z * fx;
        float speed = (float) Math.min(1.0, motion.horizontalDistance() / 1.2);
        float pitch = fast * 78.0f + fly * (forward >= 0 ? 34.0f : -14.0f) * Math.max(0.35f, speed * 2.0f);
        float roll = (float) Math.max(-25.0, Math.min(25.0, -lateral * 40.0)) * (fly + fast);
        float age = entity.tickCount + partial;
        float bob = (float) Math.sin(age * 0.1f) * 0.06f * hover + meditate * (0.35f + (float) Math.sin(age * 0.08f) * 0.05f);
        float pivot = height * 0.55f;
        poseStack.translate(0.0f, pivot + bob, 0.0f);
        if (Math.abs(pitch) > 0.01f) {
            poseStack.rotateDegrees(Axis.XP, -pitch);
        }
        if (Math.abs(roll) > 0.01f) {
            poseStack.rotateDegrees(Axis.ZP, roll);
        }
        poseStack.translate(0.0f, -pivot, 0.0f);
    }
}
