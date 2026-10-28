package dev.rlcraftceaddons.ceqol.client;

import java.util.Collections;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CEQoLGuiFactory implements IModGuiFactory {
    @Override public void initialize(Minecraft minecraftInstance) { }

    @Override public boolean hasConfigGui() { return true; }

    @Override public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new CEQoLConfigScreen(parentScreen);
    }

    @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return Collections.emptySet();
    }
}
