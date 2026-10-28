package dev.rlcraftceaddons.sdtemperaturehud.config;

import org.junit.Test;
import net.minecraftforge.common.config.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigEventHandlerTest {
    @Test
    public void syncsAndRefreshesOnlyForTheExactModId() {
        assertTrue(ConfigEventHandler.shouldSync("ceqol"));
        assertFalse(ConfigEventHandler.shouldSync("sdtemperaturehud"));
        assertFalse(ConfigEventHandler.shouldSync("SDTemperatureHud"));
        assertFalse(ConfigEventHandler.shouldSync("sdtemperaturehud-extra"));
        assertFalse(ConfigEventHandler.shouldSync(""));
        assertFalse(ConfigEventHandler.shouldSync(null));
    }

    @Test
    public void unifiedOwnerKeepsTheExistingTemperatureConfigFilename() {
        Config registration = HudConfig.class.getAnnotation(Config.class);
        assertEquals("ceqol", registration.modid());
        assertEquals("sdtemperaturehud", registration.name());
    }
}
