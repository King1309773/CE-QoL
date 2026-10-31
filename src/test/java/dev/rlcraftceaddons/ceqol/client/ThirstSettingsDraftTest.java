package dev.rlcraftceaddons.ceqol.client;

import dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ThirstSettingsDraftTest {
    private boolean tooltip, hud, shift;
    private Object rememberedFile;
    private Field configFile;

    @Before public void remember() throws Exception {
        tooltip = ThirstPreviewConfig.showTooltip;
        hud = ThirstPreviewConfig.showHud;
        shift = ThirstPreviewConfig.showDetailsWithShift;
        configFile = ThirstPreviewConfig.class.getDeclaredField("configFile");
        configFile.setAccessible(true);
        rememberedFile = configFile.get(null);
        configFile.set(null, null);
        ThirstPreviewConfig.showTooltip = true;
        ThirstPreviewConfig.showHud = true;
        ThirstPreviewConfig.showDetailsWithShift = false;
    }

    @After public void restore() throws Exception {
        ThirstPreviewConfig.showTooltip = tooltip;
        ThirstPreviewConfig.showHud = hud;
        ThirstPreviewConfig.showDetailsWithShift = shift;
        configFile.set(null, rememberedFile);
    }

    @Test public void editingAndDiscardingNeverChangesLiveSettings() throws Exception {
        Object draft = create();
        call(draft, "toggleTooltip"); call(draft, "toggleHud"); call(draft, "toggleDetails");
        assertEquals(false, call(draft, "showTooltip"));
        assertEquals(false, call(draft, "showHud"));
        assertEquals(true, call(draft, "showDetailsWithShift"));
        assertTrue(ThirstPreviewConfig.showTooltip);
        assertTrue(ThirstPreviewConfig.showHud);
        assertFalse(ThirstPreviewConfig.showDetailsWithShift);
        Object reopened = create();
        assertEquals(true, call(reopened, "showTooltip"));
        assertEquals(true, call(reopened, "showHud"));
        assertEquals(false, call(reopened, "showDetailsWithShift"));
    }

    @Test public void applyCommitsTheThreeValuesTogether() throws Exception {
        Object draft = create();
        call(draft, "toggleTooltip"); call(draft, "toggleHud"); call(draft, "toggleDetails");
        call(draft, "apply");
        assertFalse(ThirstPreviewConfig.showTooltip);
        assertFalse(ThirstPreviewConfig.showHud);
        assertTrue(ThirstPreviewConfig.showDetailsWithShift);
    }

    @Test public void failedSaveKeepsLiveValuesAndDraftForRetry() throws Exception {
        java.nio.file.Path folder=java.nio.file.Files.createTempDirectory("thirst-save-failure");
        java.nio.file.Path blocker=folder.resolve("not-a-directory");java.nio.file.Files.createFile(blocker);
        try {
            Object draft=create();call(draft,"toggleHud");
            configFile.set(null,blocker.resolve("thirstpreview.cfg").toFile());
            try {call(draft,"apply");fail("Save should fail");}
            catch(java.lang.reflect.InvocationTargetException expected){assertTrue(expected.getCause() instanceof java.io.IOException);}
            assertTrue(ThirstPreviewConfig.showHud);
            assertEquals(false,call(draft,"showHud"));
            configFile.set(null,null);call(draft,"apply");assertFalse(ThirstPreviewConfig.showHud);
        }finally {java.nio.file.Files.deleteIfExists(blocker);java.nio.file.Files.deleteIfExists(folder);}
    }

    private Object create() throws Exception {
        try { return Class.forName("dev.rlcraftceaddons.ceqol.client.ThirstSettingsDraft")
                .getConstructor().newInstance(); }
        catch (ClassNotFoundException missing) { throw new AssertionError("Hydration working copy is missing", missing); }
    }

    private Object call(Object draft, String name) throws Exception {
        return draft.getClass().getMethod(name).invoke(draft);
    }
}
