package dev.rlcraftceaddons.visualcontrols.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Works on Controlling's GuiControls subclass without replacing it. */
@SideOnly(Side.CLIENT)
public final class ControlsEntry {
    public static final class EntryButton extends GuiButton {
        EntryButton() { super(77912, 4, 1, 96, 14, I18n.format("visualcontrols.entry")); }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void init(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiControls)) return;
        event.getButtonList().removeIf(button -> button instanceof EntryButton);
        event.getButtonList().add(new EntryButton());
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void action(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        GuiScreen screen = event.getGui();
        if (!(screen instanceof GuiControls) || !(event.getButton() instanceof EntryButton)) return;
        event.setCanceled(true);
        screen.mc.displayGuiScreen(new VisualControlsScreen(screen));
    }
}
