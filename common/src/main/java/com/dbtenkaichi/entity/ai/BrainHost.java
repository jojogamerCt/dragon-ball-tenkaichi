package com.dbtenkaichi.entity.ai;

import com.dbtenkaichi.ki.Technique;

public interface BrainHost {
    double moveSpeed(boolean enraged);

    void applyRage();

    Technique signatureBeam();

    int blastColor();

    default boolean canEnrage() {
        return true;
    }
}
