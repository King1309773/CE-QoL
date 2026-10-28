package dev.rlcraftceaddons.reforgingquality;

import org.junit.Test;
import static org.junit.Assert.*;

public class PanelLayoutTest {
    @Test public void wideScreenKeepsBookmarksAreaFree() {
        PanelLayout p = PanelLayout.create(640, 360, 232, 61, 176, 238);
        assertEquals(232+42, p.x);
        assertEquals(126, p.width);
        assertEquals(120, p.height);
        assertTrue(p.x+p.width<=232+176);
    }
    @Test public void narrowScreenUsesBlankAreaWithoutCoveringToolOrButton() {
        assertEquals(238,PanelLayout.stationHeight(240));
        assertEquals(178,PanelLayout.stationHeight(180));
        PanelLayout p = PanelLayout.create(320, 240, 72, 1, 176, 238);
        assertEquals(114, p.x);
        assertFalse(p.contains(72+14,1+20));
        assertFalse(p.contains(72+14,1+40));
        assertFalse(p.contains(72+14,1+60));
        assertTrue(p.y+p.height<1+238-94); // player inventory label
    }
    @Test public void supportedResolutionsStayOnScreen() {
        for (int w = 176; w <= 1400; w += 7) {
            for (int h : new int[]{166, 180, 240, 360, 720}) {
                int guiHeight=PanelLayout.stationHeight(h);
                PanelLayout p = PanelLayout.create(w, h, (w-176)/2, (h-guiHeight)/2, 176, guiHeight);
                assertTrue(p.x >= 0 && p.y >= 0);
                assertTrue(p.x + p.width <= w);
                assertTrue(p.y + p.height <= h);
                assertTrue(p.x >= (w-176)/2);
            }
        }
    }
    @Test public void hoverUsesOnlyPanelBounds() {
        PanelLayout p = PanelLayout.create(320, 240, 72, 1, 176, 238);
        assertTrue(p.contains(p.x, p.y));
        assertFalse(p.contains(72+14,1+20)); // new tool slot
        assertFalse(p.contains(p.x + p.width, p.y));
        assertFalse(p.contains(p.x, p.y + p.height));
    }
}
