package dev.rlcraftceaddons.ceqol.client;

import org.junit.Test;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import static org.junit.Assert.*;

public class FishingHudLayoutTest {
    @Test public void originalTextOriginAndBottomAnchorRoundTripThroughOuterBounds() {
        FishingHudLayout layout=new FishingHudLayout(8,58);
        HudBounds bounds=layout.bounds(320,180,100,9);
        assertEquals(new HudBounds(5,119,106,15),bounds);
        FishingHudLayout moved=layout.moveTo(35,69,320,180,100,9);
        assertEquals(38,moved.x());assertEquals(108,moved.bottom());
        assertEquals(new HudBounds(35,69,106,15),moved.bounds(320,180,100,9));
        assertEquals(219,layout.bounds(320,280,100,9).getY());
    }
    @Test public void draggingOutsideKeepsWholeFittingPanelVisibleAndSettingsBounded() {
        FishingHudLayout layout=new FishingHudLayout(8,58).moveTo(900,-100,320,180,100,9);
        HudBounds bounds=layout.bounds(320,180,100,9);
        assertEquals(213,bounds.getX());assertEquals(1,bounds.getY());
        assertTrue(layout.x()>=0 && layout.x()<=4096);assertTrue(layout.bottom()>=0 && layout.bottom()<=4096);
        assertEquals(1,new FishingHudLayout(0,0).bounds(1,1,1,9).getWidth()>0?1:0);
    }
    @Test public void alignmentSnapsNearEdgesAndCentreButLeavesOtherPositionsAlone() {
        assertEquals(4,HudSnap.position(6,40,320));
        assertEquals(140,HudSnap.position(138,40,320));
        assertEquals(276,HudSnap.position(279,40,320));
        assertEquals(90,HudSnap.position(90,40,320));
    }
}
