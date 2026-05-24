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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class MercuryDimensionEffects extends DimensionSpecialEffects {
    public static final MercuryDimensionEffects INSTANCE = new MercuryDimensionEffects();

    private MercuryDimensionEffects() {
        super(Float.NaN, false, SkyType.NORMAL, true, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        // Dag: gyllene/varm Mercury-himmel pga närhet till solen
        // Natt: nästan svart
        if (sunHeight > 0.0F) {
            float factor = Math.min(sunHeight * 2.0F, 1.0F);
            double r = 0.95 * factor + 0.02 * (1 - factor);
            double g = 0.75 * factor + 0.02 * (1 - factor);
            double b = 0.45 * factor + 0.02 * (1 - factor);
            return new Vec3(r, g, b);
        }
        return new Vec3(0.02, 0.02, 0.02);
    }

    @Override
    public boolean isFoggyAt(int camX, int camY) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float skyAngle, float tickDelta) {
        // Orange-gyllene sunrise/sunset (kort eftersom dagen är 50% snabbare)
        return new float[]{1.0F, 0.6F, 0.2F, 0.5F};
    }
}