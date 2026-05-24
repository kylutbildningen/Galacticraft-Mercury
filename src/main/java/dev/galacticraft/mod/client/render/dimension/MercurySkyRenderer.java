package dev.galacticraft.mod.client.render.dimension;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.galacticraft.mod.Constant;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class MercurySkyRenderer extends SpaceSkyRenderer {
    public static final MercurySkyRenderer INSTANCE = new MercurySkyRenderer();

    @Override
    public void render(WorldRenderContext context) {
        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);

        float partialTicks = context.tickCounter().getGameTimeDeltaPartialTick(true);
        PoseStack matrices = new PoseStack();
        matrices.mulPose(context.positionMatrix());

        context.profiler().push("celestial_render");
        matrices.pushPose();
        matrices.mulPose(Axis.ZP.rotationDegrees(context.world().getTimeOfDay(partialTicks) * 360.0F));

        this.celestialBodyRendererManager.updateSolarPosition(0, 0, 0);
        this.celestialBodyRendererManager.render(context);

        context.profiler().pop();
        RenderSystem.setShaderColor(1.0f, 1.0F, 1.0F, 1.0F);

        context.profiler().push("sun");

        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        // Mercury: solen är 3x större än från jorden
        float size = 90.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Constant.Skybox.SUN_MOON);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, -size, 100.0F, -size).setUv(0.0F, 0.0F)
                .addVertex(matrix, size, 100.0F, -size).setUv(1.0F, 0.0F)
                .addVertex(matrix, size, 100.0F, size).setUv(1.0F, 1.0F)
                .addVertex(matrix, -size, 100.0F, size).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // Inner glödande kärna
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        size /= 4.0F;
        buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, -size, 100.0F, -size).setUv(0.375F, 0.375F)
                .addVertex(matrix, size, 100.0F, -size).setUv(0.625F, 0.375F)
                .addVertex(matrix, size, 100.0F, size).setUv(0.625F, 0.625F)
                .addVertex(matrix, -size, 100.0F, size).setUv(0.375F, 0.625F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
        matrices.popPose();
        context.profiler().pop();

        RenderSystem.depthMask(true);
    }
}