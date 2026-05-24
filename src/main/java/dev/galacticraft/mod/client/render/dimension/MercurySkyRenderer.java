/*
 * Copyright (c) 2019-2026 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.client.render.dimension;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.galacticraft.mod.Constant;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class MercurySkyRenderer extends SpaceSkyRenderer {
    public static final MercurySkyRenderer INSTANCE = new MercurySkyRenderer();

    @Override
    public void render(WorldRenderContext context) {
        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);

        float partialTicks = context.tickCounter().getGameTimeDeltaPartialTick(true);
        float timeOfDay = context.world().getTimeOfDay(partialTicks);
        PoseStack matrices = new PoseStack();
        matrices.mulPose(context.positionMatrix());

        // --- Bakgrund: stjärnor ---
        context.profiler().push("celestial_render");
        matrices.pushPose();
        matrices.mulPose(Axis.ZP.rotationDegrees(timeOfDay * 360.0F));
        this.celestialBodyRendererManager.updateSolarPosition(0, 0, 0);
        this.celestialBodyRendererManager.render(context);
        matrices.popPose();
        context.profiler().pop();
        RenderSystem.setShaderColor(1.0f, 1.0F, 1.0F, 1.0F);

        // --- PLANETER (längst bort först, var och en med astronomiskt realistisk hastighet) ---
        // speedFactor = 1 + (orbital fraction per Mercury solar day)
        renderPlanet(context, matrices, timeOfDay, Constant.CelestialBody.NEPTUNE, 1.5F, 306.0F, 76.0F, 1.003F);
        renderPlanet(context, matrices, timeOfDay, Constant.CelestialBody.URANUS,  2.0F, 255.0F, 68.0F, 1.006F);
        renderPlanet(context, matrices, timeOfDay, Constant.CelestialBody.SATURN,  4.0F, 204.0F, 73.0F, 1.016F);
        renderPlanet(context, matrices, timeOfDay, Constant.CelestialBody.JUPITER, 7.0F, 153.0F, 70.0F, 1.041F);
        renderPlanet(context, matrices, timeOfDay, Constant.CelestialBody.MARS,    2.5F, 102.0F, 78.0F, 1.256F);
        renderPlanet(context, matrices, timeOfDay, Constant.CelestialBody.EARTH,   4.0F,  51.0F, 72.0F, 1.482F);
        renderPlanet(context, matrices, timeOfDay, Constant.CelestialBody.VENUS,  14.0F,   0.0F, 75.0F, 1.782F);

        // --- SOLEN sist ---
        context.profiler().push("sun");
        matrices.pushPose();
        matrices.mulPose(Axis.ZP.rotationDegrees(timeOfDay * 360.0F * 3.0F));
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        float size = 90.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Constant.Skybox.SUN_MOON);
        drawQuad(matrix, size);

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        size /= 4.0F;
        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.addVertex(matrix, -size, 100.0F, -size).setUv(0.375F, 0.375F)
                .addVertex(matrix,  size, 100.0F, -size).setUv(0.625F, 0.375F)
                .addVertex(matrix,  size, 100.0F,  size).setUv(0.625F, 0.625F)
                .addVertex(matrix, -size, 100.0F,  size).setUv(0.375F, 0.625F);
        BufferUploader.drawWithShader(buf.buildOrThrow());

        RenderSystem.disableBlend();
        matrices.popPose();
        context.profiler().pop();

        RenderSystem.depthMask(true);
    }

    private void renderPlanet(WorldRenderContext context, PoseStack matrices, float timeOfDay, ResourceLocation texture, float size, float startYawDeg, float pitchDeg, float speedFactor) {
        context.profiler().push("planet");
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        matrices.pushPose();
        // Z-rotation = daglig himmelsrörelse vid planetens egen hastighet
        matrices.mulPose(Axis.ZP.rotationDegrees(timeOfDay * 360.0F * speedFactor));
        // Y-offset = planetens startposition i himlen
        matrices.mulPose(Axis.YP.rotationDegrees(startYawDeg));
        // X-offset = höjd i himlen
        matrices.mulPose(Axis.XP.rotationDegrees(pitchDeg));
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        drawQuad(matrix, size);

        RenderSystem.disableBlend();
        matrices.popPose();
        context.profiler().pop();
    }

    private void drawQuad(Matrix4f matrix, float halfSize) {
        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.addVertex(matrix, -halfSize, 100.0F, -halfSize).setUv(0.0F, 0.0F)
                .addVertex(matrix,  halfSize, 100.0F, -halfSize).setUv(1.0F, 0.0F)
                .addVertex(matrix,  halfSize, 100.0F,  halfSize).setUv(1.0F, 1.0F)
                .addVertex(matrix, -halfSize, 100.0F,  halfSize).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}