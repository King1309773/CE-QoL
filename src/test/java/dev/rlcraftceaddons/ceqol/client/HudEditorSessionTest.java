package dev.rlcraftceaddons.ceqol.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.*;
import dev.rlcraftceaddons.ceqol.presets.*;
import com.google.gson.JsonObject;
import java.nio.file.*;
import java.lang.reflect.Field;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class HudEditorSessionTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private Field home;private Object oldHome;private JsonObject original;
    @Before public void before()throws Exception {home=FMLInjectionData.class.getDeclaredField("minecraftHome");home.setAccessible(true);oldHome=home.get(null);home.set(null,temp.getRoot());original=new PresetSchema().capture("all",false);}
    @After public void after()throws Exception {new PresetSchema().applyLive(original);home.set(null,oldHome);}
    private HudEditorSession session(){return new HudEditorSession(HudSettings.of(0,0,1,214),new FishingHudLayout(8,58),320,180,40,10,100,70);}
    @Test public void cancelAndReplacementCannotPersistEvenAfterDragging()throws Exception {
        HudEditorSession draft=session();draft.fishing(new FishingHudLayout(70,90));draft.cancel();draft.cancel();
        draft.save(new PresetService(temp.getRoot().toPath(),()->{}),320,180,40,10,100,70);
        assertTrue(draft.isClosed());assertEquals(0,temp.getRoot().list().length);
    }
    @Test public void failedSaveKeepsDraftForRetryAndSuccessfulSaveClosesIt()throws Exception {
        HudEditorSession draft=session();draft.fishing(new FishingHudLayout(70,90));
        Path blocked=temp.getRoot().toPath().resolve("ceqol.cfg");Files.createDirectory(blocked);
        PresetService service=new PresetService(temp.getRoot().toPath(),()->{});
        try{draft.save(service,320,180,40,10,100,70);fail("Expected blocked save");}catch(java.io.IOException expected){}
        assertFalse(draft.isClosed());assertEquals(70,draft.fishing().x());
        Files.delete(blocked);draft.save(service,320,180,40,10,100,70);
        assertTrue(draft.isClosed());assertEquals(70,com.northwestwind.forgeautofish.AutoFishSettings.hudX);
        assertFalse(Files.exists(temp.getRoot().toPath().resolve("sdtemperaturehud.cfg")));
    }
    @Test public void resetUsesCurrentResolutionAndResizeKeepsDraftCoordinates() {
        HudEditorSession draft=session();HudLayoutState old=draft.temperature().state(HudPanel.COMPACT);
        draft.fishing(new FishingHudLayout(70,90));
        draft.reset(HudPanel.COMPACT,640,360,40,10,100,70);
        assertNotEquals(old,draft.temperature().state(HudPanel.COMPACT));
        assertEquals((360-65-10)/360D,draft.temperature().state(HudPanel.COMPACT).getNormalizedY(),.00001);
        assertEquals(70,draft.fishing().x());assertEquals(90,draft.fishing().bottom());
    }
}
