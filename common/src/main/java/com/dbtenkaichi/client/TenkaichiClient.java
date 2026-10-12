package com.dbtenkaichi.client;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.client.render.FighterNpcRenderer;
import com.dbtenkaichi.client.render.KiBeamRenderer;
import com.dbtenkaichi.client.render.KiBlastRenderer;
import com.dbtenkaichi.client.fx.TenkaichiParticle;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.registry.ModParticles;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import com.dbtenkaichi.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Locale;
import java.util.function.Function;

public final class TenkaichiClient {
    public static final Identifier HUD_ID = Tenkaichi.id("hud");

    private TenkaichiClient() {
    }

    public static void init() {
        ConfigManager.reloadClient();
        Tenkaichi.LOG.info("Dragon Block: Tenkaichi client initialised (graphics quality {})", ConfigManager.client().quality());
    }

    public static void registerRenderers(RendererRegistrar registrar) {
        registrar.register(ModEntities.KI_BEAM.get(), KiBeamRenderer::new);
        registrar.register(ModEntities.KI_BLAST.get(), KiBlastRenderer::new);
        registrar.register(ModEntities.MASTER.get(), FighterNpcRenderer::new);
        registrar.register(ModEntities.RAIDER.get(), FighterNpcRenderer::new);
    }

    public static void registerParticles(ParticleRegistrar registrar) {
        ModParticles.ALL.forEach((name, type) -> {
            TenkaichiParticle.Style style = TenkaichiParticle.Style.valueOf(name.toUpperCase(Locale.ROOT));
            registrar.register(type.get(), sprites -> TenkaichiParticle.provider(sprites, style));
        });
    }

    public interface ParticleRegistrar {
        void register(ParticleType<ColorParticleOption> type, Function<SpriteSet, ParticleProvider<ColorParticleOption>> factory);
    }

    public interface RendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }
}
