package dev.rlcraftceaddons.visualcontrols.client;

import dev.rlcraftceaddons.visualcontrols.binding.BindingEditor;
import dev.rlcraftceaddons.visualcontrols.binding.BindingIndex;
import dev.rlcraftceaddons.visualcontrols.layout.KeyLayout;
import dev.rlcraftceaddons.visualcontrols.layout.ControlsLayout;
import dev.rlcraftceaddons.visualcontrols.layout.KeyLayout.KeyBox;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** No GuiControls inheritance: Controlling would replace that screen. */
@SideOnly(Side.CLIENT)
public final class VisualControlsScreen extends GuiScreen {
    private static final int DONE = 1, RESET_ALL = 2, FILTER_ALL = 3, FILTER_CONFLICT = 4, FILTER_UNBOUND = 5;
    private static final int TEXT = 0xEEEEDF, MUTED = 0xB8BCAC;
    private static final int ROW_HEIGHT = ControlsLayout.ROW_HEIGHT;
    private final GuiScreen parent;
    private final CaptureSession capture = new CaptureSession();
    private BindingIndex index;
    private BindingEditor editor;
    private GuiTextField search;
    private String searchText = "";
    private BindingIndex.Filter filter = BindingIndex.Filter.ALL;
    private Integer selectedKey;
    private KeyBinding editing;
    private List<KeyBinding> visible = Collections.emptyList();
    private List<KeyBox> keys = Collections.emptyList();
    private int listX, listY, listWidth, listBottom, boardX, boardY, boardWidth, boardHeight;
    private int scroll;
    private GuiButton done;
    private ControlsLayout layout;
    private boolean keyboardRepeatBefore;
    private boolean repeatCaptured;

    public VisualControlsScreen(GuiScreen parent) { this(parent, ""); }

    public VisualControlsScreen(GuiScreen parent, String initialSearch) {
        this.parent = parent;
        this.searchText = initialSearch == null ? "" : initialSearch;
    }

    private static String tr(String key, Object... args) { return I18n.format("visualcontrols." + key, args); }

    @Override public void initGui() {
        if (!repeatCaptured) {
            keyboardRepeatBefore = Keyboard.areRepeatEventsEnabled(); repeatCaptured = true;
        }
        Keyboard.enableRepeatEvents(true);
        capture.cancel(); editing = null;
        if (index == null) {
            index = new BindingIndex(mc.gameSettings.keyBindings, value -> I18n.format(value));
            editor = new BindingEditor(mc.gameSettings.keyBindings, () -> mc.gameSettings.saveOptions());
        } else index.refresh();
        buttonList.clear();
        layout = ControlsLayout.create(width, height);
        if (layout.compact) {
            search = null; keys = Collections.emptyList(); visible = Collections.emptyList();
            done = addButton(new GuiButton(DONE, layout.doneX, layout.footerY, layout.doneWidth,
                    Math.min(20, height), I18n.format("gui.done")));
            return;
        }
        search = new GuiTextField(0, fontRenderer, layout.searchX, layout.searchY, layout.searchWidth, 16);
        search.setMaxStringLength(160); search.setText(searchText);
        search.setFocused(false);
        addButton(new GuiButton(FILTER_ALL, 8, layout.footerY, layout.filterWidth, 20, tr("all")));
        addButton(new GuiButton(FILTER_CONFLICT, 12 + layout.filterWidth, layout.footerY, layout.filterWidth, 20, tr("conflicts")));
        addButton(new GuiButton(FILTER_UNBOUND, 16 + 2 * layout.filterWidth, layout.footerY, layout.filterWidth, 20, tr("unbound")));
        boardX = layout.boardX; boardY = layout.boardY;
        boardWidth = layout.boardWidth; boardHeight = layout.boardHeight;
        listX = layout.listX; listY = layout.listY; listWidth = layout.listWidth; listBottom = layout.listBottom;
        int mouseButtons = Mouse.isCreated() ? Math.min(16, Math.max(5, Mouse.getButtonCount())) : 5;
        keys = KeyLayout.create(boardX + 3, boardY + 3, boardWidth - 6, boardHeight - 6, mouseButtons);
        done = addButton(new GuiButton(DONE, layout.doneX, layout.footerY, layout.doneWidth, 20, I18n.format("gui.done")));
        addButton(new GuiButton(RESET_ALL, layout.resetAllX, layout.footerY, layout.resetAllWidth, 20, tr("reset_all")));
        refresh();
    }

    private void refresh() {
        index.refresh();
        visible = index.find(searchText, filter, selectedKey);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, visible.size() - pageSize())));
        for (GuiButton button : buttonList) {
            if (button.id >= FILTER_ALL && button.id <= FILTER_UNBOUND) {
                boolean active = button.id == FILTER_ALL && filter == BindingIndex.Filter.ALL
                        || button.id == FILTER_CONFLICT && filter == BindingIndex.Filter.CONFLICTS
                        || button.id == FILTER_UNBOUND && filter == BindingIndex.Filter.UNBOUND;
                button.packedFGColour = active ? 0xB6E1E4 : 0;
            }
        }
        if (done != null) done.displayString = capture.isRecording() ? tr("cancel") : I18n.format("gui.done");
    }

    private int pageSize() { return layout.pageSize(); }

    @Override public void updateScreen() { if (search != null) search.updateCursorCounter(); }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc.world == null) drawDefaultBackground();
        else drawRect(0, 0, width, height, 0x66000000);
        if (layout.compact) {
            if (width >= 100 && height >= 70) {
                drawCenteredString(fontRenderer, tr("title"), width / 2, 8, TEXT);
                drawCenteredString(fontRenderer, fontRenderer.trimStringToWidth(tr("window_small"), width - 12),
                        width / 2, height / 2 - 10, TEXT);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }
        drawRect(0, listY - 7, width, height - 32, 0x66000000);
        drawRect(0, listY - 7, width, listY - 6, 0xAA989898);
        drawRect(0, height - 32, width, height, 0x55000000);
        drawRect(0, height - 32, width, height - 31, 0xAA989898);
        String title = capture.isRecording() && editing != null ? tr("recording", index.name(editing)) : tr("title");
        drawCenteredString(fontRenderer, fontRenderer.trimStringToWidth(title, width - 16), width / 2, 10,
                capture.isRecording() ? 0xFFD14B : TEXT);
        search.drawTextBox();
        if (search.getText().isEmpty() && !search.isFocused())
            drawString(fontRenderer, fontRenderer.trimStringToWidth(tr("search"), search.getWidth() - 8), search.x + 4, search.y + 5, MUTED);
        drawBoard(mouseX, mouseY);
        drawList(mouseX, mouseY);
        if (visible.size() > pageSize()) {
            String counter = (scroll + 1) + "–" + Math.min(scroll + pageSize(), visible.size()) + " / " + visible.size();
            int counterX = 16 + 3 * (layout.filterWidth + 4);
            if (counterX + fontRenderer.getStringWidth(counter) < layout.resetAllX - 6)
                drawString(fontRenderer, counter, counterX, layout.footerY + 6, MUTED);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawDetails(mouseX, mouseY);
        GlStateManager.color(1, 1, 1, 1);
    }

    private void drawBoard(int mouseX, int mouseY) {
        for (KeyBox key : keys) {
            BindingIndex.State state = index.state(key.keyCode);
            boolean hover = key.contains(mouseX, mouseY);
            int border = hover ? 0xFFBBBBBB : 0xFF080808;
            int fill = state == BindingIndex.State.CONFLICT ? 0xFF990808
                    : state == BindingIndex.State.SHARED ? 0xFFA17B1D
                    : state == BindingIndex.State.BOUND ? 0xFF006500 : 0xFF484848;
            boolean selected = selectedKey != null && selectedKey == key.keyCode;
            if (selected) border = 0xFFFFFFFF;
            drawRect(key.x, key.y, key.x + key.width, key.y + key.height, border);
            int inset = selected && key.width > 8 && key.height > 8 ? 2 : 1;
            if (key.width > 2 && key.height > 2)
                drawRect(key.x + inset, key.y + inset, key.x + key.width - inset, key.y + key.height - inset, hover ? brighten(fill) : fill);
            fitLabel(keyLabel(key), key.x, key.y, key.width, key.height, TEXT);
        }
    }

    private String keyLabel(KeyBox key) {
        switch (key.keyCode) {
            case -100: return tr("mouse_left");
            case -98: return tr("mouse_middle");
            case -99: return tr("mouse_right");
            case Keyboard.KEY_SPACE: return tr("space");
            case Keyboard.KEY_APPS: return tr("menu");
            case Keyboard.KEY_LSHIFT: case Keyboard.KEY_RSHIFT: return "Shift";
            case Keyboard.KEY_LCONTROL: case Keyboard.KEY_RCONTROL: return "Ctrl";
            case Keyboard.KEY_LMENU: case Keyboard.KEY_RMENU: return "Alt";
            case Keyboard.KEY_CAPITAL: return "Caps Lock";
            case Keyboard.KEY_BACK: return "Backspace";
            case Keyboard.KEY_UP: return "↑";
            case Keyboard.KEY_DOWN: return "↓";
            case Keyboard.KEY_LEFT: return "←";
            case Keyboard.KEY_RIGHT: return "→";
            default: return key.label;
        }
    }

    private static int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 255) + 17);
        int g = Math.min(255, ((color >> 8) & 255) + 17);
        int b = Math.min(255, (color & 255) + 17);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private void fitLabel(String text, int x, int y, int w, int h, int color) {
        int textWidth = Math.max(1, fontRenderer.getStringWidth(text));
        float scale = Math.min(1F, Math.min(Math.max(1, w - 3) / (float) textWidth, Math.max(1, h - 2) / 9F));
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + w / 2F, y + h / 2F, 0);
        GlStateManager.scale(scale, scale, 1);
        fontRenderer.drawString(text, -textWidth / 2F, -4F, color, false);
        GlStateManager.popMatrix();
    }

    private void drawList(int mouseX, int mouseY) {
        if (visible.isEmpty()) {
            String empty = selectedKey != null && index.onKey(selectedKey).isEmpty() ? tr("empty_key") : tr("empty");
            drawString(fontRenderer, fontRenderer.trimStringToWidth(empty, listWidth), listX, listY + 7, MUTED);
            return;
        }
        int end = Math.min(visible.size(), scroll + pageSize());
        for (int i = scroll; i < end; i++) {
            KeyBinding binding = visible.get(i);
            int y = rowY(i), controlsY = y + 2;
            String name = index.category(binding) + " - " + index.name(binding);
            if (index.isConflict(binding)) name = "! " + name;
            drawString(fontRenderer, fontRenderer.trimStringToWidth(name, layout.bindingX - listX - 7), listX + 2, y + 7, TEXT);
            drawSmallButton(layout.bindingX, controlsY, layout.bindingWidth, 20,
                    editing == binding && capture.isRecording() ? "> ... <" : binding.getDisplayName(), mouseX, mouseY,
                    !capture.isRecording() || editing == binding, index.isConflict(binding) ? 0xFFD14B : TEXT);
            drawSmallButton(layout.clearX, controlsY, layout.clearWidth, 20, "×", mouseX, mouseY,
                    !capture.isRecording() && binding.getKeyCode() != 0, TEXT);
            drawSmallButton(layout.resetX, controlsY, layout.resetWidth, 20, tr("reset"), mouseX, mouseY,
                    !capture.isRecording() && !binding.isSetToDefaultValue(), TEXT);
        }
        if (visible.size() > pageSize()) {
            int trackY = listY, trackHeight = Math.max(1, listBottom - trackY);
            int thumbHeight = Math.max(5, trackHeight * pageSize() / visible.size());
            int thumbY = trackY + (trackHeight - thumbHeight) * scroll / Math.max(1, visible.size() - pageSize());
            drawRect(listX + listWidth - 2, trackY, listX + listWidth, listBottom, 0xFF3A4232);
            drawRect(listX + listWidth - 2, thumbY, listX + listWidth, thumbY + thumbHeight, 0xFFBCC3AE);
        }
    }

    private int rowY(int indexInList) { return layout.rowTop(indexInList - scroll); }

    private void drawSmallButton(int x, int y, int w, int h, String label, int mx, int my, boolean enabled, int color) {
        boolean hover = inside(mx, my, x, y, w, h);
        drawRect(x, y, x + w, y + h, 0xFF080808);
        drawGradientRect(x + 1, y + 1, x + w - 1, y + h - 1,
                enabled && hover ? 0xFF919191 : enabled ? 0xFF7B7B7B : 0xFF343434,
                enabled ? 0xFF555555 : 0xFF202020);
        if (enabled) drawRect(x + 1, y + 1, x + w - 1, y + 2, 0xFFAFAFAF);
        fitLabel(label, x + 2, y, w - 4, h, enabled ? color : 0xFF777777);
    }

    private void drawDetails(int mouseX, int mouseY) {
        if (capture.isRecording()) return;
        for (KeyBox key : keys) if (key.contains(mouseX, mouseY)) {
            List<String> tip = new ArrayList<>();
            tip.add(GameSettings.getKeyDisplayString(key.keyCode));
            BindingIndex.State state = index.state(key.keyCode);
            tip.add(tr(state == BindingIndex.State.FREE ? "free" : state == BindingIndex.State.CONFLICT ? "conflict"
                    : state == BindingIndex.State.SHARED ? "shared" : "bound"));
            List<KeyBinding> bound = index.onKey(key.keyCode);
            int max = Math.max(1, Math.min(8, (height - 74) / 10));
            for (int i = 0; i < Math.min(max, bound.size()); i++) {
                KeyBinding b = bound.get(i); tip.add(index.name(b) + " — " + index.category(b));
            }
            if (bound.size() > max) tip.add(tr("more", bound.size() - max));
            tip.add(tr("key_hint"));
            drawBoundedTooltip(tip, mouseX, mouseY); return;
        }
        int end = Math.min(visible.size(), scroll + pageSize());
        for (int i = scroll; i < end; i++) if (inside(mouseX, mouseY, listX, rowY(i), listWidth, ROW_HEIGHT)) {
            KeyBinding b = visible.get(i);
            List<String> tip = new ArrayList<>();
            tip.add(index.name(b)); tip.add(index.category(b)); tip.add(b.getDisplayName());
            if (inside(mouseX, mouseY, layout.clearX, rowY(i) + 2, layout.clearWidth, 20)) tip.add(tr("clear"));
            if (index.isConflict(b)) {
                tip.add(tr("conflict_hint"));
                int count = 0;
                for (KeyBinding other : index.all()) {
                    if (other != b && other.getKeyCode() != 0 && (b.conflicts(other) || other.conflicts(b)
                            || b.hasKeyCodeModifierConflict(other) || other.hasKeyCodeModifierConflict(b))) {
                        if (++count <= 3) tip.add("! " + index.name(other) + " — " + index.category(other));
                    }
                }
                if (count > 3) tip.add(tr("more", count - 3));
            }
            tip.add(tr("edit_hint"));
            drawBoundedTooltip(tip, mouseX, mouseY); return;
        }
    }

    private void drawBoundedTooltip(List<String> text, int mouseX, int mouseY) {
        List<String> wrapped = new ArrayList<>();
        for (String line : text) wrapped.addAll(fontRenderer.listFormattedStringToWidth(line, Math.max(100, Math.min(280, width - 24))));
        int max = Math.max(1, (height - 28) / 10);
        if (wrapped.size() > max) {
            wrapped = new ArrayList<>(wrapped.subList(0, max)); wrapped.set(max - 1, "...");
        }
        drawHoveringText(wrapped, mouseX, mouseY);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (layout.compact) { super.mouseClicked(mouseX, mouseY, button); return; }
        if (capture.isRecording()) {
            mouseHandled = true;
            if (button == 0 && inside(mouseX, mouseY, done.x, done.y, done.width, done.height)) { cancelCapture(); return; }
            if (button == 0) for (KeyBox key : keys) if (key.contains(mouseX, mouseY)) {
                apply(capture.virtualKey(key.keyCode, KeyModifier.getActiveModifier())); return;
            }
            apply(capture.mouseDown(button, KeyModifier.getActiveModifier())); return;
        }
        search.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            for (KeyBox key : keys) if (key.contains(mouseX, mouseY)) {
                selectedKey = selectedKey != null && selectedKey == key.keyCode ? null : key.keyCode;
                filter = BindingIndex.Filter.ALL; scroll = 0; search.setFocused(false); refresh(); return;
            }
            int end = Math.min(visible.size(), scroll + pageSize());
            for (int i = scroll; i < end; i++) if (inside(mouseX, mouseY, listX, rowY(i) + 2, listWidth, 20)) {
                KeyBinding binding = visible.get(i);
                if (inside(mouseX, mouseY, layout.bindingX, rowY(i) + 2, layout.bindingWidth, 20)) {
                    editing = binding; capture.begin(); search.setFocused(false); refresh();
                } else if (inside(mouseX, mouseY, layout.clearX, rowY(i) + 2, layout.clearWidth, 20)) {
                    editor.clear(binding); refresh();
                } else if (inside(mouseX, mouseY, layout.resetX, rowY(i) + 2, layout.resetWidth, 20)) {
                    editor.reset(binding); refresh();
                }
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == DONE) {
            if (capture.isRecording()) cancelCapture(); else mc.displayGuiScreen(parent);
        } else if (button.id == RESET_ALL) {
            mc.displayGuiScreen(new GuiYesNo(this, tr("reset_title"), tr("reset_message"), RESET_ALL));
        } else if (button.id >= FILTER_ALL && button.id <= FILTER_UNBOUND) {
            filter = button.id == FILTER_ALL ? BindingIndex.Filter.ALL : button.id == FILTER_CONFLICT ? BindingIndex.Filter.CONFLICTS : BindingIndex.Filter.UNBOUND;
            selectedKey = null; scroll = 0; refresh();
        }
    }

    @Override public void confirmClicked(boolean result, int id) {
        if (result && id == RESET_ALL) editor.resetAll();
        mc.displayGuiScreen(this);
    }

    @Override protected void keyTyped(char character, int keyCode) throws IOException {
        if (capture.isRecording()) { apply(capture.keyDown(keyCode, KeyModifier.getActiveModifier())); return; }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            // A held Escape that just canceled recording must not also leave the screen.
            if (!Keyboard.isRepeatEvent()) mc.displayGuiScreen(parent);
            return;
        }
        if (layout.compact) return;
        if (search.textboxKeyTyped(character, keyCode)) {
            searchText = search.getText(); selectedKey = null; scroll = 0; refresh(); return;
        }
        if (!search.isFocused()) {
            if (keyCode == Keyboard.KEY_DOWN) scroll = Math.min(Math.max(0, visible.size() - pageSize()), scroll + 1);
            if (keyCode == Keyboard.KEY_UP) scroll = Math.max(0, scroll - 1);
            if (keyCode == Keyboard.KEY_NEXT) scroll = Math.min(Math.max(0, visible.size() - pageSize()), scroll + pageSize());
            if (keyCode == Keyboard.KEY_PRIOR) scroll = Math.max(0, scroll - pageSize());
        }
    }

    @Override public void handleKeyboardInput() throws IOException {
        if (capture.isRecording()) {
            // Do not let Minecraft's global F11/F2 handler also consume a recorded key.
            keyHandled = true;
            apply(Keyboard.getEventKeyState() ? capture.keyDown(Keyboard.getEventKey(), KeyModifier.getActiveModifier()) : capture.keyUp(Keyboard.getEventKey()));
            return;
        }
        super.handleKeyboardInput();
    }

    @Override public void handleMouseInput() throws IOException {
        boolean wasRecording = capture.isRecording();
        super.handleMouseInput();
        if (layout.compact) return;
        if (wasRecording) { mouseHandled = true; return; }
        int wheel = Mouse.getEventDWheel();
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (wheel != 0 && inside(mouseX, mouseY, listX, listY, listWidth, listBottom - listY)) {
            scroll = Math.max(0, Math.min(Math.max(0, visible.size() - pageSize()), scroll + (wheel > 0 ? -3 : 3)));
            mouseHandled = true;
        }
    }

    private void apply(CaptureSession.Result result) {
        if (result.kind == CaptureSession.Kind.APPLY && editing != null) {
            editor.bind(editing, result.keyCode, result.modifier);
            editing = null; refresh();
        } else if (result.kind == CaptureSession.Kind.CANCEL) cancelCapture();
    }

    private void cancelCapture() { capture.cancel(); editing = null; refresh(); }

    @Override public void onGuiClosed() {
        capture.cancel(); editing = null;
        if (repeatCaptured) Keyboard.enableRepeatEvents(keyboardRepeatBefore);
        repeatCaptured = false;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
