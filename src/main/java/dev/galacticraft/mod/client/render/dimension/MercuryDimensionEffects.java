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
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int camX, int camY) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float skyAngle, float tickDelta) {
        return new float[]{0.0F, 0.0F, 0.0F, 0.0F};
    }
}