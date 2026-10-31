package dev.rlcraftceaddons.ceqol.client;

import java.io.IOException;
import java.util.Arrays;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class ThirstPreviewConfigScreen extends GuiScreen {
    public static final int DONE = 0, TOOLTIP = 1, HUD = 2, SHIFT_DETAILS = 3, CANCEL = 4;
    private final GuiScreen parent;
    private final ThirstSettingsDraft draft = new ThirstSettingsDraft();
    private boolean compact;

    public ThirstPreviewConfigScreen(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        compact = width < 160 || height < 110;
        int buttonWidth = Math.max(1, Math.min(280, width - 24));
        int left = (width - buttonWidth) / 2;
        if (compact) {
            int buttonHeight = Math.max(1, Math.min(20, height));
            addButton(new GuiButton(CANCEL, left, Math.max(0, height - buttonHeight - 4),
                    buttonWidth, buttonHeight, I18n.format("gui.cancel")));
            return;
        }
        int buttonHeight = Math.min(20, Math.max(14, (height - 48) / 4));
        int gap = Math.min(6, Math.max(2, (height - 36 - buttonHeight * 4) / 3));
        int top = Math.max(28, (height - buttonHeight * 4 - gap * 3) / 2);
        addButton(new GuiButton(TOOLTIP, left, top, buttonWidth, buttonHeight,
                label("tooltip", draft.showTooltip())));
        addButton(new GuiButton(HUD, left, top + buttonHeight + gap, buttonWidth, buttonHeight,
                label("hud", draft.showHud())));
        addButton(new GuiButton(SHIFT_DETAILS, left, top + (buttonHeight + gap) * 2,
                buttonWidth, buttonHeight, label("shift", draft.showDetailsWithShift())));
        int halfWidth = (buttonWidth - 6) / 2;
        int footerY = top + (buttonHeight + gap) * 3;
        addButton(new GuiButton(DONE, left, footerY, halfWidth, buttonHeight,
                I18n.format("ceqol.config.save")));
        addButton(new GuiButton(CANCEL, left + halfWidth + 6, footerY, halfWidth, buttonHeight,
                I18n.format("gui.cancel")));
    }

    private String label(String key, boolean value) {
        return I18n.format("ceqol.config.thirst." + key,
                I18n.format(value ? "options.on" : "options.off"));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button.id == DONE) {
            draft.apply();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == CANCEL) { mc.displayGuiScreen(parent); return; }
        if (button.id == TOOLTIP) draft.toggleTooltip();
        else if (button.id == HUD) draft.toggleHud();
        else if (button.id == SHIFT_DETAILS) draft.toggleDetails();
        else return;
        initGui();
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        if (width >= 80 && height >= 30) {
            drawCenteredString(fontRenderer, fontRenderer.trimStringToWidth(
                    I18n.format("ceqol.config.thirst"), Math.max(1, width - 16)),
                    width / 2, 10, 0xFFFFFF);
        }
        if (compact && height >= 70) {
            drawCenteredString(fontRenderer, fontRenderer.trimStringToWidth(
                    I18n.format("ceqol.config.window_small"), Math.max(1, width - 16)),
                    width / 2, height / 2 - 10, 0xCCCCCC);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!compact) {
            for (GuiButton button : buttonList) {
                if (!button.isMouseOver()) continue;
                String hint = button.id == TOOLTIP ? "tooltip" : button.id == HUD ? "hud"
                        : button.id == SHIFT_DETAILS ? "shift" : button.id == DONE ? "save" : "cancel";
                drawHoveringText(Arrays.asList(I18n.format("ceqol.config.thirst." + hint + ".hint")),
                        mouseX, mouseY);
                break;
            }
        }
    }
}
