package com.dbtenkaichi.combat;

import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.stats.StatBlock;
import org.jspecify.annotations.Nullable;

public interface TenkaichiFighter {
    StatBlock fighterStats();

    Race fighterRace();

    @Nullable String fighterForm();

    float fighterMastery();

    double fighterKiRatio();

    RuntimeState fighterRuntime();

    void onFighterHit(net.minecraft.world.entity.LivingEntity attacker, double damage);

    boolean spendFighterKi(double amount);

    default double outgoingDamageScale() {
        return 1.0;
    }
}
