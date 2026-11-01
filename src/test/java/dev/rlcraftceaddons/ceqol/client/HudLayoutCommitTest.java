package dev.rlcraftceaddons.ceqol.client;

import com.google.gson.JsonObject;
import dev.rlcraftceaddons.ceqol.presets.*;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.*;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import com.northwestwind.forgeautofish.AutoFishSettings;
import java.util.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class HudLayoutCommitTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private JsonObject original;
    @Before public void before()throws Exception {original=new PresetSchema().capture("all",false);}
    @After public void after()throws Exception {new PresetSchema().applyLive(original);}
    private PresetService service(){return new PresetService(temp.getRoot().toPath(),()->{});}
    private Map<HudPanel,HudLayoutState> layouts(){Map<HudPanel,HudLayoutState> m=new EnumMap<>(HudPanel.class);m.put(HudPanel.COMPACT,new HudLayoutState(.2,.3,1));m.put(HudPanel.ANALYSIS,new HudLayoutState(.4,.5,1.2));return m;}
    @Test public void fishOnlyOmitsTemperatureAndCapturesOtherFishingValuesAtSaveTime()throws Exception {
        AutoFishSettings.durabilityReserve=37;
        JsonObject doc=HudLayoutCommit.document(service(),layouts(),Collections.emptySet(),new FishingHudLayout(70,90));
        JsonObject settings=doc.getAsJsonObject("settings");assertEquals(1,settings.size());
        assertFalse(settings.has("temperature"));assertFalse(settings.has("hydration"));
        assertEquals(37,settings.getAsJsonObject("fishing").get("durabilityReserve").getAsInt());
        assertEquals(70,settings.getAsJsonObject("fishing").get("hudX").getAsInt());
    }
    @Test public void firstLegacyEditMigratesBothPanelsWhileModernEditPreservesUntouchedPanel()throws Exception {
        HudConfig.layout.version=0;
        JsonObject old=HudLayoutCommit.document(service(),layouts(),EnumSet.of(HudPanel.COMPACT),null).getAsJsonObject("settings").getAsJsonObject("temperature");
        assertEquals(.4,old.get("layout.analysisX").getAsDouble(),0);assertEquals(1,old.get("layout.version").getAsInt());
        HudConfig.layout.version=1;HudConfig.layout.analysisX=.7;HudConfig.backgroundAlpha=99;
        JsonObject modern=HudLayoutCommit.document(service(),layouts(),EnumSet.of(HudPanel.COMPACT),null).getAsJsonObject("settings").getAsJsonObject("temperature");
        assertEquals(.7,modern.get("layout.analysisX").getAsDouble(),0);assertEquals(99,modern.get("backgroundAlpha").getAsInt());
        assertEquals(.2,modern.get("layout.compactX").getAsDouble(),0);
    }
    @Test public void untouchedEditorDoesNotCreateDocumentOrTouchFiles()throws Exception {
        assertNull(HudLayoutCommit.document(service(),layouts(),Collections.emptySet(),null));
        assertEquals(0,temp.getRoot().list().length);
    }
}
