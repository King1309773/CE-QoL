package dev.rlcraftceaddons.ceqol.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.config.GuiHudConfig;
import dev.rlcraftceaddons.visualcontrols.client.VisualControlsScreen;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class CEQoLConfigScreen extends GuiScreen {
    public static final int DONE = 0, TEMPERATURE = 1, CONTROLS = 2, THIRST = 3, QUALITY = 4, AUTO_FISH = 5, FISHING_SETTINGS=6, PRESETS=7;
    private final GuiScreen parent;
    private boolean compact;

    public CEQoLConfigScreen(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        compact = width < 160 || height < 170;
        int buttonWidth = Math.max(1, Math.min(280, width - 24));
        int left = (width - buttonWidth) / 2;
        if (compact) {
            int buttonHeight = Math.max(1, Math.min(20, height));
            addButton(new GuiButton(DONE, left, Math.max(0, height - buttonHeight - 4),
                    buttonWidth, buttonHeight, I18n.format("gui.done")));
            return;
        }
        int buttonHeight = Math.min(20, Math.max(14, (height - 48) / 8));
        int gap = Math.min(6, Math.max(2, (height - 36 - buttonHeight * 8) / 7));
        int top = Math.max(28, (height - buttonHeight * 8 - gap * 7) / 2);
        addButton(new GuiButton(TEMPERATURE, left, top, buttonWidth, buttonHeight,
                I18n.format("ceqol.config.temperature")));
        addButton(new GuiButton(CONTROLS, left, top + buttonHeight + gap, buttonWidth, buttonHeight,
                I18n.format("ceqol.config.controls")));
        addButton(new GuiButton(THIRST, left, top + (buttonHeight + gap) * 2, buttonWidth, buttonHeight,
                I18n.format("ceqol.config.thirst")));
        addButton(new GuiButton(AUTO_FISH, left, top + (buttonHeight + gap) * 3,
                buttonWidth, buttonHeight, I18n.format("ceqol.config.autofish")));
        addButton(new GuiButton(FISHING_SETTINGS,left,top+(buttonHeight+gap)*4,buttonWidth,buttonHeight,I18n.format("ceqol.config.fishing")));
        GuiButton quality = addButton(new GuiButton(QUALITY, left, top + (buttonHeight + gap) * 5,
                buttonWidth, buttonHeight, I18n.format("ceqol.config.quality")));
        quality.enabled = false;
        addButton(new GuiButton(PRESETS,left,top+(buttonHeight+gap)*6,buttonWidth,buttonHeight,I18n.format("ceqol.presets.title")));
        addButton(new GuiButton(DONE, left, top + (buttonHeight + gap) * 7, buttonWidth, buttonHeight,
                I18n.format("gui.done")));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button.id == DONE) mc.displayGuiScreen(parent);
        else if (button.id == TEMPERATURE) mc.displayGuiScreen(new GuiHudConfig(this));
        else if (button.id == CONTROLS) mc.displayGuiScreen(new VisualControlsScreen(this));
        else if (button.id == THIRST) mc.displayGuiScreen(new ThirstPreviewConfigScreen(this));
        else if(button.id==FISHING_SETTINGS)mc.displayGuiScreen(new AutoFishConfigScreen(this));
        else if(button.id==PRESETS)mc.displayGuiScreen(new PresetConfigScreen(this));
        else if (button.id == AUTO_FISH) mc.displayGuiScreen(new VisualControlsScreen(this,
                I18n.format("key.categories.forgeautofish")));
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        if (width >= 80 && height >= 30) {
            drawCenteredString(fontRenderer, "CEQoL", width / 2, 10, 0xFFFFFF);
        }
        if (compact && height >= 70) {
            drawCenteredString(fontRenderer, fontRenderer.trimStringToWidth(
                    I18n.format("ceqol.config.window_small"), Math.max(1, width - 16)),
                    width / 2, height / 2 - 10, 0xCCCCCC);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
