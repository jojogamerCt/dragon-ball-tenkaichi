package com.dbtenkaichi.client.render;

import com.dbtenkaichi.Tenkaichi;

import java.util.HashSet;
import java.util.Set;

public final class RenderGuard {
    private static final Set<String> REPORTED = new HashSet<>();

    private RenderGuard() {
    }

    public static void report(String what, Throwable error) {
        if (REPORTED.add(what + ":" + error.getClass().getName())) {
            Tenkaichi.LOG.error("Rendering of {} failed and was skipped", what, error);
        }
    }
}
