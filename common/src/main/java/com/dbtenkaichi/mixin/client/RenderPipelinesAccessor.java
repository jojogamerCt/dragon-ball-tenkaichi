package com.dbtenkaichi.mixin.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("LIGHTNING_SNIPPET")
    static RenderPipeline.Snippet dbtenkaichi$lightningSnippet() {
        throw new AssertionError();
    }
}
