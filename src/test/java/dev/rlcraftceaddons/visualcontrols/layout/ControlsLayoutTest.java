package dev.rlcraftceaddons.visualcontrols.layout;

import org.junit.Test;
import static org.junit.Assert.*;

public class ControlsLayoutTest {
    private final int[][] sizes = {{320,240},{640,360},{854,480},{1280,720},{1280,300}};
    @Test public void keyboardAndSearchAreCenteredAboveTheListAtEverySize() {
        for (int[] size : sizes) {
            ControlsLayout l = ControlsLayout.create(size[0], size[1]);
            assertTrue(l.boardWidth > 200);
            assertTrue(Math.abs(l.boardX * 2 + l.boardWidth - size[0]) <= 1);
            assertTrue(Math.abs(l.searchX * 2 + l.searchWidth - size[0]) <= 1);
            assertTrue(l.boardY > l.searchY + 16);
            assertTrue(l.listY > l.boardY + l.boardHeight);
        }
    }
    @Test public void rowActionsNeverOverlapTheNameOrEachOther() {
        for (int[] size : sizes) {
            ControlsLayout l = ControlsLayout.create(size[0], size[1]);
            assertTrue(l.bindingX > l.listX + 75);
            assertTrue(l.bindingWidth >= 60);
            assertTrue(l.clearX >= l.bindingX + l.bindingWidth + 2);
            assertTrue(l.resetX >= l.clearX + l.clearWidth + 2);
            assertTrue(l.resetX + l.resetWidth <= l.listX + l.listWidth);
        }
    }
    @Test public void smallestGuiKeepsTwoCompleteRowsAndVisibleFooter() {
        ControlsLayout l = ControlsLayout.create(320, 240);
        assertTrue(l.pageSize() >= 2);
        assertTrue(l.rowTop(l.pageSize() - 1) + ControlsLayout.ROW_HEIGHT <= l.listBottom);
        assertTrue(l.listBottom <= l.footerY - 7);
        assertTrue(8 + 3 * (l.filterWidth + 4) <= l.resetAllX);
        assertTrue(l.resetAllX + l.resetAllWidth + 4 <= l.doneX);
        assertTrue(l.doneX + l.doneWidth <= 312);
        assertTrue(l.footerY + 20 <= 236);
    }
    @Test public void wideButShortWindowsKeepTheKeyboardWithinTheContent() {
        ControlsLayout l = ControlsLayout.create(1280,300);
        assertTrue(l.boardHeight > 0);
        assertTrue(l.boardY + l.boardHeight < l.listY);
        assertTrue(l.pageSize() >= 2);
        assertTrue(l.rowTop(l.pageSize() - 1) + ControlsLayout.ROW_HEIGHT <= l.listBottom);
    }
    @Test public void validTinyWindowsAlwaysKeepAnExitWithoutThrowing() {
        for (int[] size : new int[][] {{640,200},{280,240},{640,120},{16,16},{1,1}}) {
            try {
                ControlsLayout l = ControlsLayout.create(size[0], size[1]);
                assertTrue(l.doneWidth > 0);
                assertTrue(l.doneX >= 0 && l.doneX + l.doneWidth <= size[0]);
                assertTrue(l.footerY >= 0 && l.footerY < size[1]);
            } catch (IllegalArgumentException failure) {
                fail("A valid Minecraft window must not crash: " + size[0] + "x" + size[1]);
            }
        }
    }
    @Test public void normalReferenceViewShowsFiveRowsWithoutAnExtraListHeading() {
        ControlsLayout l = ControlsLayout.create(640, 360);
        assertEquals(l.listY, l.rowTop(0));
        assertTrue(l.pageSize() >= 5);
    }
}
