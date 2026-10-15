package com.dbtenkaichi.client.hud;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

final class WorldProjection {
    private WorldProjection() {
    }

    record Point(float x, float y, boolean onScreen, float angle) {
    }

    static Point project(Vec3 world, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        Vec3 rel = world.subtract(camera.position());
        Matrix4f matrix = camera.getViewRotationProjectionMatrix(new Matrix4f());
        Vector4f v = new Vector4f((float) rel.x, (float) rel.y, (float) rel.z, 1.0f).mul(matrix);
        float cx = width / 2.0f;
        float cy = height / 2.0f;
        if (v.w > 0.05f) {
            float sx = (v.x / v.w * 0.5f + 0.5f) * width;
            float sy = (1.0f - (v.y / v.w * 0.5f + 0.5f)) * height;
            boolean inside = sx >= 12 && sx <= width - 12 && sy >= 12 && sy <= height - 12;
            if (inside) {
                return new Point(sx, sy, true, 0.0f);
            }
            float angle = (float) Math.atan2(sy - cy, sx - cx);
            return edge(angle, width, height);
        }
        float dx = -v.x;
        float dy = v.y;
        if (Math.abs(dx) < 1.0e-4f && Math.abs(dy) < 1.0e-4f) {
            dy = 1.0f;
        }
        return edge((float) Math.atan2(dy, dx), width, height);
    }

    private static Point edge(float angle, int width, int height) {
        float cx = width / 2.0f;
        float cy = height / 2.0f;
        float margin = 18.0f;
        float dx = (float) Math.cos(angle);
        float dy = (float) Math.sin(angle);
        float sx = Math.abs(dx) < 1.0e-4f ? Float.MAX_VALUE : (cx - margin) / Math.abs(dx);
        float sy = Math.abs(dy) < 1.0e-4f ? Float.MAX_VALUE : (cy - margin) / Math.abs(dy);
        float s = Math.min(sx, sy);
        return new Point(cx + dx * s, cy + dy * s, false, angle);
    }
}
