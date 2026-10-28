package dev.rlcraftceaddons.visualcontrols.layout;

import org.junit.Test;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class KeyLayoutTest {
    @Test
    public void includesExactlyThe104AnsiKeysWithoutAnUnboundKey() {
        Set<Integer> actual = new HashSet<Integer>();
        for (KeyLayout.KeyBox box : KeyLayout.create(0, 0, 600, 240, 5)) {
            if (box.keyCode >= 0) {
                assertTrue("Duplicate key " + box.label, actual.add(box.keyCode));
                assertNotNull(box.label);
                assertFalse(box.label.isEmpty());
            }
        }
        assertEquals(ansi104(), actual);
        assertEquals(104, actual.size());
        assertFalse(actual.contains(Keyboard.KEY_NONE));
    }

    @Test
    public void includesAllRecognizedMouseButtonsAndAtLeastFive() {
        for (int requested : new int[] {0, 1, 3, 5, 8, 16, 24}) {
            Set<Integer> expected = new HashSet<Integer>();
            int count = Math.max(5, Math.min(16, requested));
            for (int button = 0; button < count; button++) {
                expected.add(button - 100);
            }
            Set<Integer> actual = new HashSet<Integer>();
            for (KeyLayout.KeyBox box : KeyLayout.create(10, 20, 304, 88, requested)) {
                if (box.keyCode < 0) {
                    assertTrue("Duplicate mouse button", actual.add(box.keyCode));
                }
            }
            assertEquals(expected, actual);
        }
    }

    @Test
    public void keepsEveryKeyInBoundsWithoutOverlapsAtNormalAndSmallSizes() {
        for (int[] size : new int[][] {{304, 88}, {600, 240}, {921, 333}, {80, 22},
                {23, 11}, {22, 11}, {21, 11}, {20, 11}}) {
            List<KeyLayout.KeyBox> boxes = KeyLayout.create(13, -7, size[0], size[1], 16);
            assertEquals(120, boxes.size());
            for (int i = 0; i < boxes.size(); i++) {
                KeyLayout.KeyBox a = boxes.get(i);
                assertTrue(a.label + " width", a.width >= 1);
                assertTrue(a.label + " height", a.height >= 1);
                assertTrue(a.label + " left", a.x >= 13);
                assertTrue(a.label + " top", a.y >= -7);
                assertTrue(a.label + " right", a.x + a.width <= 13 + size[0]);
                assertTrue(a.label + " bottom", a.y + a.height <= -7 + size[1]);
                for (int j = i + 1; j < boxes.size(); j++) {
                    KeyLayout.KeyBox b = boxes.get(j);
                    boolean overlaps = a.x < b.x + b.width && b.x < a.x + a.width
                            && a.y < b.y + b.height && b.y < a.y + a.height;
                    assertFalse(a.label + " overlaps " + b.label, overlaps);
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAnAreaTooSmallToHoldTheCompleteKeyboard() {
        KeyLayout.create(0, 0, 19, 10, 5);
    }

    @Test
    public void putsTheNavigationClusterBetweenTheMainKeyboardAndNumpad() {
        List<KeyLayout.KeyBox> boxes = KeyLayout.create(0, 0, 900, 250, 5);
        KeyLayout.KeyBox rightControl = key(boxes, Keyboard.KEY_RCONTROL);
        KeyLayout.KeyBox insert = key(boxes, Keyboard.KEY_INSERT);
        KeyLayout.KeyBox pageUp = key(boxes, Keyboard.KEY_PRIOR);
        KeyLayout.KeyBox numlock = key(boxes, Keyboard.KEY_NUMLOCK);
        assertTrue("Navigation must be right of the main keyboard",
                rightControl.x + rightControl.width < insert.x);
        assertTrue("Numpad must be right of the navigation cluster",
                pageUp.x + pageUp.width < numlock.x);
        assertEquals(key(boxes, Keyboard.KEY_GRAVE).y, insert.y);
        assertEquals(key(boxes, Keyboard.KEY_TAB).y, key(boxes, Keyboard.KEY_DELETE).y);
        assertEquals(key(boxes, Keyboard.KEY_ESCAPE).y, key(boxes, Keyboard.KEY_SYSRQ).y);
    }

    @Test
    public void putsTheCompleteNumpadToTheRightOfTheMainKeyboard() {
        List<KeyLayout.KeyBox> boxes = KeyLayout.create(0, 0, 900, 250, 5);
        KeyLayout.KeyBox backspace = key(boxes, Keyboard.KEY_BACK);
        KeyLayout.KeyBox numlock = key(boxes, Keyboard.KEY_NUMLOCK);
        assertTrue("Numpad must be right of the main keyboard",
                backspace.x + backspace.width < numlock.x);
        assertEquals(key(boxes, Keyboard.KEY_GRAVE).y, numlock.y);
        assertEquals(key(boxes, Keyboard.KEY_SPACE).y, key(boxes, Keyboard.KEY_NUMPAD0).y);
        assertTrue(key(boxes, Keyboard.KEY_UP).y < key(boxes, Keyboard.KEY_DOWN).y);
        assertTrue(key(boxes, Keyboard.KEY_NUMPAD0).width > key(boxes, Keyboard.KEY_NUMPAD1).width);
        assertTrue(key(boxes, Keyboard.KEY_ADD).height > key(boxes, Keyboard.KEY_NUMPAD9).height);
        assertTrue(key(boxes, Keyboard.KEY_NUMPADENTER).height > key(boxes, Keyboard.KEY_NUMPAD3).height);
    }

    @Test
    public void putsThreeMouseButtonsAcrossTheNavigationClusterBelowTheEditKeys() {
        List<KeyLayout.KeyBox> boxes = KeyLayout.create(0, 0, 900, 250, 5);
        KeyLayout.KeyBox left = key(boxes, -100);
        KeyLayout.KeyBox middle = key(boxes, -98);
        KeyLayout.KeyBox right = key(boxes, -99);
        assertEquals("Left mouse aligns with navigation's first column",
                key(boxes, Keyboard.KEY_INSERT).x, left.x);
        assertEquals(key(boxes, Keyboard.KEY_HOME).x, middle.x);
        assertEquals(key(boxes, Keyboard.KEY_PRIOR).x, right.x);
        assertEquals(left.y, middle.y);
        assertEquals(left.y, right.y);
        assertEquals(key(boxes, Keyboard.KEY_CAPITAL).y, left.y);
        assertTrue(key(boxes, Keyboard.KEY_DELETE).y < left.y);
        assertEquals(left.x, key(boxes, -97).x);
        assertEquals(middle.x, key(boxes, Keyboard.KEY_UP).x);
        assertEquals(right.x, key(boxes, -96).x);
        assertEquals(key(boxes, Keyboard.KEY_LSHIFT).y, key(boxes, -97).y);
        assertEquals(key(boxes, Keyboard.KEY_UP).y, key(boxes, -96).y);
        assertEquals(key(boxes, Keyboard.KEY_LCONTROL).y, key(boxes, Keyboard.KEY_LEFT).y);
        assertEquals(key(boxes, Keyboard.KEY_DOWN).y, key(boxes, Keyboard.KEY_RIGHT).y);
    }

    @Test
    public void addsAnExtraBottomRowOnlyForMouseButtonsBeyondFive() {
        List<KeyLayout.KeyBox> fiveButtons = KeyLayout.create(0, 0, 900, 300, 5);
        List<KeyLayout.KeyBox> sixteenButtons = KeyLayout.create(0, 0, 900, 300, 16);
        assertEquals(109, fiveButtons.size());
        int baseBottom = 0;
        for (KeyLayout.KeyBox box : sixteenButtons) {
            if (box.keyCode >= 0 || box.keyCode <= -96) {
                baseBottom = Math.max(baseBottom, box.y + box.height);
            }
        }
        int extraY = key(sixteenButtons, -95).y;
        assertTrue("Extra buttons must follow the complete keyboard", extraY > baseBottom);
        for (int button = 5; button < 16; button++) {
            assertEquals(extraY, key(sixteenButtons, button - 100).y);
        }
        KeyLayout.KeyBox baseLastRow = key(fiveButtons, Keyboard.KEY_RCONTROL);
        assertTrue("Five mouse buttons must not reserve an extra row",
                baseLastRow.y + baseLastRow.height >= 299);
    }

    @Test
    public void hitTestingUsesHalfOpenRectangleBounds() {
        for (KeyLayout.KeyBox box : KeyLayout.create(10, 20, 304, 88, 5)) {
            assertTrue(box.contains(box.x, box.y));
            assertTrue(box.contains(box.x + box.width - 1, box.y + box.height - 1));
            assertFalse(box.contains(box.x - 1, box.y));
            assertFalse(box.contains(box.x, box.y - 1));
            assertFalse(box.contains(box.x + box.width, box.y));
            assertFalse(box.contains(box.x, box.y + box.height));
        }
        assertFalse(KeyLayout.create(10, 20, 304, 88, 5).isEmpty());
    }

    private static KeyLayout.KeyBox key(List<KeyLayout.KeyBox> boxes, int code) {
        for (KeyLayout.KeyBox box : boxes) {
            if (box.keyCode == code) {
                return box;
            }
        }
        throw new AssertionError("Missing key code " + code);
    }

    private static Set<Integer> ansi104() {
        return new HashSet<Integer>(Arrays.asList(
                Keyboard.KEY_ESCAPE, Keyboard.KEY_F1, Keyboard.KEY_F2, Keyboard.KEY_F3,
                Keyboard.KEY_F4, Keyboard.KEY_F5, Keyboard.KEY_F6, Keyboard.KEY_F7,
                Keyboard.KEY_F8, Keyboard.KEY_F9, Keyboard.KEY_F10, Keyboard.KEY_F11,
                Keyboard.KEY_F12, Keyboard.KEY_SYSRQ, Keyboard.KEY_SCROLL, Keyboard.KEY_PAUSE,
                Keyboard.KEY_GRAVE, Keyboard.KEY_1, Keyboard.KEY_2, Keyboard.KEY_3,
                Keyboard.KEY_4, Keyboard.KEY_5, Keyboard.KEY_6, Keyboard.KEY_7,
                Keyboard.KEY_8, Keyboard.KEY_9, Keyboard.KEY_0, Keyboard.KEY_MINUS,
                Keyboard.KEY_EQUALS, Keyboard.KEY_BACK,
                Keyboard.KEY_TAB, Keyboard.KEY_Q, Keyboard.KEY_W, Keyboard.KEY_E,
                Keyboard.KEY_R, Keyboard.KEY_T, Keyboard.KEY_Y, Keyboard.KEY_U,
                Keyboard.KEY_I, Keyboard.KEY_O, Keyboard.KEY_P, Keyboard.KEY_LBRACKET,
                Keyboard.KEY_RBRACKET, Keyboard.KEY_BACKSLASH,
                Keyboard.KEY_CAPITAL, Keyboard.KEY_A, Keyboard.KEY_S, Keyboard.KEY_D,
                Keyboard.KEY_F, Keyboard.KEY_G, Keyboard.KEY_H, Keyboard.KEY_J,
                Keyboard.KEY_K, Keyboard.KEY_L, Keyboard.KEY_SEMICOLON, Keyboard.KEY_APOSTROPHE,
                Keyboard.KEY_RETURN, Keyboard.KEY_LSHIFT, Keyboard.KEY_Z, Keyboard.KEY_X,
                Keyboard.KEY_C, Keyboard.KEY_V, Keyboard.KEY_B, Keyboard.KEY_N,
                Keyboard.KEY_M, Keyboard.KEY_COMMA, Keyboard.KEY_PERIOD, Keyboard.KEY_SLASH,
                Keyboard.KEY_RSHIFT, Keyboard.KEY_LCONTROL, Keyboard.KEY_LMETA, Keyboard.KEY_LMENU,
                Keyboard.KEY_SPACE, Keyboard.KEY_RMENU, Keyboard.KEY_RMETA, Keyboard.KEY_APPS,
                Keyboard.KEY_RCONTROL,
                Keyboard.KEY_INSERT, Keyboard.KEY_HOME, Keyboard.KEY_PRIOR,
                Keyboard.KEY_DELETE, Keyboard.KEY_END, Keyboard.KEY_NEXT,
                Keyboard.KEY_UP, Keyboard.KEY_LEFT, Keyboard.KEY_DOWN, Keyboard.KEY_RIGHT,
                Keyboard.KEY_NUMLOCK, Keyboard.KEY_DIVIDE, Keyboard.KEY_MULTIPLY, Keyboard.KEY_SUBTRACT,
                Keyboard.KEY_NUMPAD7, Keyboard.KEY_NUMPAD8, Keyboard.KEY_NUMPAD9, Keyboard.KEY_ADD,
                Keyboard.KEY_NUMPAD4, Keyboard.KEY_NUMPAD5, Keyboard.KEY_NUMPAD6,
                Keyboard.KEY_NUMPAD1, Keyboard.KEY_NUMPAD2, Keyboard.KEY_NUMPAD3, Keyboard.KEY_NUMPADENTER,
                Keyboard.KEY_NUMPAD0, Keyboard.KEY_DECIMAL));
    }
}
