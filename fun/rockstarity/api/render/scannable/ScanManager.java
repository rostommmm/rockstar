/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fun.rockstarity.api.render.scannable;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;

public enum ScanManager implements IAccess
{
    INSTANCE;


    private static float computeTargetRadius() {
        return ScanManager.mc.gameRenderer.getFarPlaneDistance();
    }

    public static int computeScanGrowthDuration() {
        return 12000 * ScanManager.mc.gameSettings.renderDistanceChunks / 12;
    }

    public static float computeRadius(long start, float duration) {
        float targetRadius = ScanManager.computeTargetRadius();
        float timeOffset = 200.0f;
        float denominator = 1.0f / ((duration + 200.0f) * (duration + 200.0f) - 40000.0f);
        float coefficientA = -targetRadius * 200.0f * 200.0f * denominator;
        float coefficientC = targetRadius * denominator;
        float currentTime = System.currentTimeMillis() - start;
        return 10.0f + coefficientA + (currentTime + 200.0f) * (currentTime + 200.0f) * coefficientC;
    }
}

