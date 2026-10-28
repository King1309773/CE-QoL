package dev.rlcraftceaddons.visualcontrols.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.input.Keyboard.*;

/** Integer-only key geometry; creating a layout never initializes an input device. */
public final class KeyLayout {
    private KeyLayout() { }

    /**
     * Fits an ANSI 104-key keyboard and 5 to 16 mouse buttons inside the given area.
     * The smallest supported area is 20 by 11 pixels, with one-pixel keycaps.
     */
    public static List<KeyBox> create(int x, int y, int width, int height, int mouseButtons) {
        if (width < 20 || height < 11) {
            throw new IllegalArgumentException("Key layout requires at least 20 by 11 pixels");
        }
        int mouseCount = Math.max(5, Math.min(16, mouseButtons));
        Builder keys = new Builder(x, y, width, height, mouseCount > 5 ? 30 : 25);

        keys.add(KEY_ESCAPE, "Esc", 0, 0, 4, 4);
        int[] functionKeys = {KEY_F1, KEY_F2, KEY_F3, KEY_F4, KEY_F5, KEY_F6,
                KEY_F7, KEY_F8, KEY_F9, KEY_F10, KEY_F11, KEY_F12};
        for (int i = 0; i < functionKeys.length; i++) {
            int column = 8 + i * 4 + (i >= 4 ? 2 : 0) + (i >= 8 ? 2 : 0);
            keys.add(functionKeys[i], "F" + (i + 1), column, 0, 4, 4);
        }
        keys.add(KEY_SYSRQ, "PrtSc", 61, 0, 4, 4);
        keys.add(KEY_SCROLL, "ScrLk", 65, 0, 4, 4);
        keys.add(KEY_PAUSE, "Pause", 69, 0, 4, 4);

        keys.row(1, new int[] {KEY_GRAVE, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6,
                        KEY_7, KEY_8, KEY_9, KEY_0, KEY_MINUS, KEY_EQUALS, KEY_BACK},
                new String[] {"`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "Back"},
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 8);
        keys.row(2, new int[] {KEY_TAB, KEY_Q, KEY_W, KEY_E, KEY_R, KEY_T, KEY_Y,
                        KEY_U, KEY_I, KEY_O, KEY_P, KEY_LBRACKET, KEY_RBRACKET, KEY_BACKSLASH},
                new String[] {"Tab", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\"},
                6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6);
        keys.row(3, new int[] {KEY_CAPITAL, KEY_A, KEY_S, KEY_D, KEY_F, KEY_G, KEY_H,
                        KEY_J, KEY_K, KEY_L, KEY_SEMICOLON, KEY_APOSTROPHE, KEY_RETURN},
                new String[] {"Caps", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "Enter"},
                7, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 9);
        keys.row(4, new int[] {KEY_LSHIFT, KEY_Z, KEY_X, KEY_C, KEY_V, KEY_B,
                        KEY_N, KEY_M, KEY_COMMA, KEY_PERIOD, KEY_SLASH, KEY_RSHIFT},
                new String[] {"LShift", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "RShift"},
                9, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 11);
        keys.row(5, new int[] {KEY_LCONTROL, KEY_LMETA, KEY_LMENU, KEY_SPACE,
                        KEY_RMENU, KEY_RMETA, KEY_APPS, KEY_RCONTROL},
                new String[] {"LCtrl", "LWin", "LAlt", "Space", "RAlt", "RWin", "Menu", "RCtrl"},
                5, 5, 5, 25, 5, 5, 5, 5);

        addNavigation(keys);
        addNumpad(keys);
        addMouse(keys, mouseCount);
        return Collections.unmodifiableList(keys.boxes);
    }

    private static void addNavigation(Builder keys) {
        keys.add(KEY_INSERT, "Ins", 61, 5, 4, 4);
        keys.add(KEY_HOME, "Home", 65, 5, 4, 4);
        keys.add(KEY_PRIOR, "PgUp", 69, 5, 4, 4);
        keys.add(KEY_DELETE, "Del", 61, 9, 4, 4);
        keys.add(KEY_END, "End", 65, 9, 4, 4);
        keys.add(KEY_NEXT, "PgDn", 69, 9, 4, 4);
        keys.add(KEY_UP, "Up", 65, 17, 4, 4);
        keys.add(KEY_LEFT, "Left", 61, 21, 4, 4);
        keys.add(KEY_DOWN, "Down", 65, 21, 4, 4);
        keys.add(KEY_RIGHT, "Right", 69, 21, 4, 4);
    }

    private static void addNumpad(Builder keys) {
        keys.add(KEY_NUMLOCK, "Num", 74, 5, 4, 4);
        keys.add(KEY_DIVIDE, "/", 78, 5, 4, 4);
        keys.add(KEY_MULTIPLY, "*", 82, 5, 4, 4);
        keys.add(KEY_SUBTRACT, "-", 86, 5, 4, 4);
        keys.add(KEY_NUMPAD7, "7", 74, 9, 4, 4);
        keys.add(KEY_NUMPAD8, "8", 78, 9, 4, 4);
        keys.add(KEY_NUMPAD9, "9", 82, 9, 4, 4);
        keys.add(KEY_ADD, "+", 86, 9, 4, 8);
        keys.add(KEY_NUMPAD4, "4", 74, 13, 4, 4);
        keys.add(KEY_NUMPAD5, "5", 78, 13, 4, 4);
        keys.add(KEY_NUMPAD6, "6", 82, 13, 4, 4);
        keys.add(KEY_NUMPAD1, "1", 74, 17, 4, 4);
        keys.add(KEY_NUMPAD2, "2", 78, 17, 4, 4);
        keys.add(KEY_NUMPAD3, "3", 82, 17, 4, 4);
        keys.add(KEY_NUMPADENTER, "Enter", 86, 17, 4, 8);
        keys.add(KEY_NUMPAD0, "0", 74, 21, 8, 4);
        keys.add(KEY_DECIMAL, ".", 82, 21, 4, 4);
    }

    private static void addMouse(Builder keys, int count) {
        // LWJGL numbers the right button before the middle button.
        keys.add(-100, "M1", 61, 13, 4, 4);
        keys.add(-98, "M3", 65, 13, 4, 4);
        keys.add(-99, "M2", 69, 13, 4, 4);
        keys.add(-97, "M4", 61, 17, 4, 4);
        keys.add(-96, "M5", 69, 17, 4, 4);
        for (int button = 5; button < count; button++) {
            keys.add(button - 100, "M" + (button + 1), (button - 5) * 4, 26, 4, 4);
        }
    }

    private static final class Builder {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int gridHeight;
        private final List<KeyBox> boxes = new ArrayList<KeyBox>(120);

        private Builder(int x, int y, int width, int height, int gridHeight) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.gridHeight = gridHeight;
        }

        private void row(int row, int[] codes, String[] labels, int... quarterKeyWidths) {
            int column = 0;
            for (int i = 0; i < codes.length; i++) {
                int span = quarterKeyWidths[i];
                add(codes[i], labels[i], column, 1 + row * 4, span, 4);
                column += span;
            }
        }

        private void add(int code, String label, int column, int row, int columns, int rows) {
            // Fewer than 23 pixels cannot fit all 21 keys of a horizontal row.
            // Keep the historic minimum area usable as a compact one-pixel grid.
            if (width < 23) {
                int index = boxes.size();
                boxes.add(new KeyBox(code, x + index % width, y + index / width, 1, 1, label));
                return;
            }
            // Scale shared grid boundaries instead of rounding each key independently.
            int left = (int) ((long) width * column / 90);
            int right = (int) ((long) width * (column + columns) / 90);
            int top = (int) ((long) height * row / gridHeight);
            int bottom = (int) ((long) height * (row + rows) / gridHeight);
            int boxWidth = right - left;
            int boxHeight = bottom - top;
            // Leave a pixel between keycaps only when it cannot erase a tiny key.
            if (boxWidth > 2) {
                boxWidth--;
            }
            if (boxHeight > 2) {
                boxHeight--;
            }
            boxes.add(new KeyBox(code, x + left, y + top, boxWidth, boxHeight, label));
        }
    }

    public static final class KeyBox {
        public final int keyCode;
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final String label;

        private KeyBox(int keyCode, int x, int y, int width, int height, String label) {
            this.keyCode = keyCode;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
        }

        public boolean contains(int pointX, int pointY) {
            return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
        }
    }
}
