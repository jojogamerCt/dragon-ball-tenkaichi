package com.dbtenkaichi.client.render;

import com.dbtenkaichi.mixin.client.RenderPipelinesAccessor;
import com.dbtenkaichi.mixin.client.RenderTypeInvoker;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class TenkaichiRenderTypes {
    private static RenderType aura;

    private TenkaichiRenderTypes() {
    }

    public static RenderType aura() {
        if (aura == null) {
            RenderPipeline pipeline = RenderPipeline.builder(RenderPipelinesAccessor.dbtenkaichi$lightningSnippet())
                    .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("dbtenkaichi", "pipeline/aura"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false))
                    .withCull(false)
                    .build();
            aura = RenderTypeInvoker.dbtenkaichi$create("dbtenkaichi_aura", RenderSetup.builder(pipeline).sortOnUpload().createRenderSetup());
        }
        return aura;
    }
}
