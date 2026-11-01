package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TemperaturePresentationCacheTest {
    @Test public void equalSnapshotsReuseMeasuredCompactButChangedValuesRefresh() {
        int[] widths={0};
        TemperatureHudRenderer renderer=new TemperatureHudRenderer(null,text->{widths[0]++;return text.length();});
        HudSettings settings=HudSettings.of(0,0,1,214);
        Object first=renderer.measureCompact(TemperatureSnapshot.withTarget(11,13),settings);
        int measured=widths[0];
        assertSame(first,renderer.measureCompact(TemperatureSnapshot.withTarget(11,13),settings));
        assertEquals(measured,widths[0]);
        assertNotSame(first,renderer.measureCompact(TemperatureSnapshot.withTarget(12,13),settings));
        assertTrue(widths[0]>measured);
    }
    @Test public void settingsReplacementAndExplicitResourceContextInvalidate() {
        TemperatureHudRenderer renderer=new TemperatureHudRenderer(null,String::length);
        HudSettings settings=HudSettings.of(0,0,1,214);
        TemperatureSnapshot sample=TemperatureSnapshot.bodyOnly(10);
        Object first=renderer.measureCompact(sample,settings);
        assertNotSame(first,renderer.measureCompact(sample,HudSettings.of(0,0,1,100)));
        renderer.invalidatePresentations();
        assertNotSame(first,renderer.measureCompact(sample,settings));
    }
    @Test public void snapshotValuesIncludeAnalysisAndAvailability() {
        TemperatureSnapshot a=TemperatureSnapshot.withAnalysis(10,new TemperatureBreakdown(1,2,3,4,5,15));
        assertEquals(a,TemperatureSnapshot.withAnalysis(10,new TemperatureBreakdown(1,2,3,4,5,15)));
        assertEquals(a.hashCode(),TemperatureSnapshot.withAnalysis(10,new TemperatureBreakdown(1,2,3,4,5,15)).hashCode());
        assertNotEquals(a,TemperatureSnapshot.withAnalysis(10,new TemperatureBreakdown(2,2,3,4,4,15)));
        assertNotEquals(TemperatureSnapshot.bodyOnly(0),TemperatureSnapshot.unavailable());
    }
}
