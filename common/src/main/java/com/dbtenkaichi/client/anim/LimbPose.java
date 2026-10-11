package com.dbtenkaichi.client.anim;

import java.util.Arrays;

public final class LimbPose {
    public static final int HEAD = 0;
    public static final int BODY = 1;
    public static final int RIGHT_ARM = 2;
    public static final int LEFT_ARM = 3;
    public static final int RIGHT_LEG = 4;
    public static final int LEFT_LEG = 5;
    public static final int PARTS = 6;

    final float[] rot = new float[PARTS * 3];

    public LimbPose() {
        Arrays.fill(rot, Float.NaN);
    }

    public LimbPose set(int part, float x, float y, float z) {
        rot[part * 3] = x;
        rot[part * 3 + 1] = y;
        rot[part * 3 + 2] = z;
        return this;
    }

    public LimbPose head(float x, float y, float z) {
        return set(HEAD, x, y, z);
    }

    public LimbPose body(float x, float y, float z) {
        return set(BODY, x, y, z);
    }

    public LimbPose rightArm(float x, float y, float z) {
        return set(RIGHT_ARM, x, y, z);
    }

    public LimbPose leftArm(float x, float y, float z) {
        return set(LEFT_ARM, x, y, z);
    }

    public LimbPose rightLeg(float x, float y, float z) {
        return set(RIGHT_LEG, x, y, z);
    }

    public LimbPose leftLeg(float x, float y, float z) {
        return set(LEFT_LEG, x, y, z);
    }

    public float get(int index) {
        return rot[index];
    }

    public static LimbPose lerp(LimbPose a, LimbPose b, float t) {
        LimbPose out = new LimbPose();
        for (int i = 0; i < a.rot.length; i++) {
            float va = a.rot[i];
            float vb = b.rot[i];
            if (Float.isNaN(va)) {
                out.rot[i] = vb;
            } else if (Float.isNaN(vb)) {
                out.rot[i] = va;
            } else {
                out.rot[i] = va + (vb - va) * t;
            }
        }
        return out;
    }

    public LimbPose copy() {
        LimbPose out = new LimbPose();
        System.arraycopy(rot, 0, out.rot, 0, rot.length);
        return out;
    }
}
