package dev.rlcraftceaddons.thirstpreview;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ThirstPreviewConfigPersistenceTest {
    @Rule public final TemporaryFolder temporary = new TemporaryFolder();

    @Test public void savingHudToggleKeepsOwnedDescriptionsAndUnrelatedValues() throws Exception {
        File file = temporary.newFile("thirstpreview.cfg");
        Files.write(file.toPath(), ("client {\n    S:legacySentinel=keep-me\n}\n")
                .getBytes(StandardCharsets.UTF_8));
        boolean tooltip = ThirstPreviewConfig.showTooltip;
        boolean hud = ThirstPreviewConfig.showHud;
        boolean shift = ThirstPreviewConfig.showDetailsWithShift;
        Field minecraftHome = FMLInjectionData.class.getDeclaredField("minecraftHome");
        minecraftHome.setAccessible(true);
        Object previousHome = minecraftHome.get(null);
        Field rememberedFile = ThirstPreviewConfig.class.getDeclaredField("configFile");
        rememberedFile.setAccessible(true);
        Object previousFile = rememberedFile.get(null);
        try {
            minecraftHome.set(null, temporary.getRoot());
            ThirstPreviewConfig.load(file);
            String description = "Show per-use hydration as waterdrop icons";
            assertTrue("Initial load should write the tooltip description", read(file).contains(description));
            ThirstPreviewConfig.showHud = !ThirstPreviewConfig.showHud;
            ThirstPreviewConfig.save();
            String saved = read(file);
            assertTrue("An unrelated value must remain", saved.contains("S:legacySentinel=keep-me"));
            assertTrue("The selected HUD value must be saved", saved.contains("B:showHud=false"));
            assertTrue("Saving a toggle must retain the owned tooltip description", saved.contains(description));
        } finally {
            ThirstPreviewConfig.showTooltip = tooltip;
            ThirstPreviewConfig.showHud = hud;
            ThirstPreviewConfig.showDetailsWithShift = shift;
            rememberedFile.set(null, previousFile);
            minecraftHome.set(null, previousHome);
        }
    }

    private String read(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
