package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import net.minecraftforge.fml.client.IModGuiFactory;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class GuiFactoryPolicyTest {
    @Test
    public void factoryAdvertisesConfigGuiAndNoRuntimeCategories() {
        IModGuiFactory factory = new SDTemperatureHudGuiFactory();
        Set<IModGuiFactory.RuntimeOptionCategoryElement> categories =
            factory.runtimeGuiCategories();

        assertTrue(factory.hasConfigGui());
        assertTrue(categories.isEmpty());
    }

    @Test
    public void fixedFooterStaysBelowPageControls() {
        SettingsScreenLayout layout = SettingsScreenLayout.forScreen(320, 240);
        assertTrue(layout.rowY(layout.rows() - 1) + 20 < layout.pagerY);
        assertTrue(layout.pagerY + 20 < layout.footerY);
    }
}
