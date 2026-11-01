/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;

public final class TemperatureSnapshot {
    private static final TemperatureSnapshot UNAVAILABLE =
        new TemperatureSnapshot(false, 0, null, null);

    private final boolean available;
    private final int bodyTemperature;
    private final Integer targetTemperature;
    private final TemperatureBreakdown breakdown;

    private TemperatureSnapshot(
        boolean available,
        int bodyTemperature,
        Integer targetTemperature,
        TemperatureBreakdown breakdown
    ) {
        this.available = available;
        this.bodyTemperature = bodyTemperature;
        this.targetTemperature = targetTemperature;
        this.breakdown = breakdown;
    }

    public static TemperatureSnapshot withTarget(int bodyTemperature, int targetTemperature) {
        return new TemperatureSnapshot(true, bodyTemperature, targetTemperature, null);
    }

    public static TemperatureSnapshot withAnalysis(int bodyTemperature, TemperatureBreakdown breakdown) {
        if (breakdown == null) {
            throw new IllegalArgumentException("Temperature breakdown must not be null");
        }
        return new TemperatureSnapshot(true, bodyTemperature, breakdown.getTarget(), breakdown);
    }

    public static TemperatureSnapshot bodyOnly(int bodyTemperature) {
        return new TemperatureSnapshot(true, bodyTemperature, null, null);
    }

    public static TemperatureSnapshot unavailable() {
        return UNAVAILABLE;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public int getBodyTemperature() {
        if (!this.available) {
            throw new IllegalStateException("Body temperature is unavailable");
        }
        return this.bodyTemperature;
    }

    public boolean hasTargetTemperature() {
        return this.targetTemperature != null;
    }

    public int getTargetTemperature() {
        if (this.targetTemperature == null) {
            throw new IllegalStateException("Target temperature is unavailable");
        }
        return this.targetTemperature;
    }

    public boolean hasBreakdown() {
        return this.breakdown != null;
    }

    public TemperatureBreakdown getBreakdown() {
        if (this.breakdown == null) {
            throw new IllegalStateException("Temperature breakdown is unavailable");
        }
        return this.breakdown;
    }

    @Override public boolean equals(Object other) {
        if(this==other)return true;
        if(!(other instanceof TemperatureSnapshot))return false;
        TemperatureSnapshot that=(TemperatureSnapshot)other;
        return available==that.available && bodyTemperature==that.bodyTemperature
                && java.util.Objects.equals(targetTemperature,that.targetTemperature)
                && java.util.Objects.equals(breakdown,that.breakdown);
    }
    @Override public int hashCode() {return java.util.Objects.hash(available,bodyTemperature,targetTemperature,breakdown);}
}
