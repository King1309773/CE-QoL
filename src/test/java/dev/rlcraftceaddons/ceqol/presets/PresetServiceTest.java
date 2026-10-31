package dev.rlcraftceaddons.ceqol.presets;

import com.google.gson.JsonObject;
import java.lang.reflect.*;
import java.nio.file.*;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class PresetServiceTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private Object service,oldHome;
    private JsonObject original;
    private Field home;
    @Before public void setup() throws Exception {
        home=FMLInjectionData.class.getDeclaredField("minecraftHome");home.setAccessible(true);
        oldHome=home.get(null);home.set(null,temp.getRoot());
        original=new PresetSchema().capture("all",false);
    }
    @After public void cleanup()throws Exception {if(original!=null)new PresetSchema().applyLive(original);home.set(null,oldHome);}
    private Object service()throws Exception {
        if(service==null)try{service=Class.forName("dev.rlcraftceaddons.ceqol.presets.PresetService")
                .getConstructor(Path.class,Runnable.class).newInstance(temp.getRoot().toPath(),(Runnable)()->{});}
        catch(ClassNotFoundException e){throw new AssertionError("Settings presets are missing",e);}
        return service;
    }
    private Object call(String method,Class<?>[] types,Object...args)throws Exception {
        try{return service().getClass().getMethod(method,types).invoke(service(),args);}
        catch(InvocationTargetException e){if(e.getCause() instanceof Exception)throw (Exception)e.getCause();throw e;}
    }
    private JsonObject capture(String scope)throws Exception{return (JsonObject)call("capture",new Class<?>[]{String.class,String.class},"我的布局",scope);}
    @Test public void exportedJsonCanBeImportedWithoutApplyingIt()throws Exception {
        JsonObject doc=capture("hydration");
        call("save",new Class<?>[]{JsonObject.class,boolean.class},doc,false);
        call("export",new Class<?>[]{String.class,boolean.class},"我的布局",false);
        Path root=temp.getRoot().toPath().resolve("ceqol");
        Files.createDirectories(root.resolve("imports"));
        Files.copy(root.resolve("exports/我的布局.json"),root.resolve("imports/分享.json"));
        JsonObject imported=(JsonObject)call("importFile",new Class<?>[]{String.class,boolean.class},"分享",true);
        assertEquals(doc,imported);
        assertFalse(Files.exists(temp.getRoot().toPath().resolve("thirstpreview.cfg")));
    }
    @Test public void unknownSettingIsRejectedBeforeAnyApply()throws Exception {
        JsonObject doc=capture("hydration");doc.getAsJsonObject("settings").getAsJsonObject("hydration").addProperty("arbitrary",true);
        try{call("apply",new Class<?>[]{JsonObject.class},doc);fail("Unknown setting accepted");}catch(java.io.IOException expected){}
        assertFalse(Files.exists(temp.getRoot().toPath().resolve("thirstpreview.cfg")));
    }
    @Test public void outOfRangeFishingReserveIsRejected()throws Exception {
        JsonObject doc=capture("fishing");doc.getAsJsonObject("settings").getAsJsonObject("fishing").addProperty("durabilityReserve",999);
        try{call("save",new Class<?>[]{JsonObject.class,boolean.class},doc,false);fail("Invalid reserve accepted");}catch(java.io.IOException expected){}
    }
    @Test public void unsafeNamesAndAccidentalOverwritesAreRejected()throws Exception {
        JsonObject doc=capture("all");doc.addProperty("name","../escape");
        try{call("save",new Class<?>[]{JsonObject.class,boolean.class},doc,false);fail("Unsafe name accepted");}catch(java.io.IOException expected){}
        doc.addProperty("name","我的布局");call("save",new Class<?>[]{JsonObject.class,boolean.class},doc,false);
        try{call("save",new Class<?>[]{JsonObject.class,boolean.class},doc,false);fail("Overwrite accepted");}catch(java.io.IOException expected){}
    }

    @Test public void scopedDefaultsAndUndoLeaveOtherFeaturesAlone()throws Exception {
        PresetService service=(PresetService)service();
        dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig.showTooltip=false;
        com.northwestwind.forgeautofish.AutoFishSettings.durabilityReserve=23;
        service.apply(service.defaults("hydration"));
        assertTrue(dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig.showTooltip);
        com.northwestwind.forgeautofish.AutoFishSettings.durabilityReserve=31;
        service.undo();
        assertFalse(dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig.showTooltip);
        assertEquals(31,com.northwestwind.forgeautofish.AutoFishSettings.durabilityReserve);
        assertFalse(Files.exists(temp.getRoot().toPath().resolve("options.txt")));
    }
    @Test public void laterFileFailureRollsBackEarlierWritesAndLiveValues()throws Exception {
        PresetService service=(PresetService)service();JsonObject doc=service.defaults("all");
        Path second=temp.getRoot().toPath().resolve("thirstpreview.cfg");
        byte[] malformed="client {\n \"unclosed\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);Files.write(second,malformed);
        try{service.apply(doc);fail("Blocked second file should fail");}catch(java.io.IOException expected){}
        assertFalse(Files.exists(temp.getRoot().toPath().resolve("sdtemperaturehud.cfg")));
        assertArrayEquals(malformed,Files.readAllBytes(second));
        assertEquals(original,new PresetSchema().capture("all",false));
        assertFalse(service.hasUndo());
    }
    @Test public void scopedResetIgnoresInvalidValuesInUnrelatedFeature()throws Exception {
        PresetService service=(PresetService)service();
        dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig.theme.severeCold="invalid";
        service.apply(service.defaults("hydration"));
        assertEquals("invalid",dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig.theme.severeCold);
        assertTrue(dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig.showTooltip);
    }
    @Test public void directoryLinkCannotCreateAnExternalPresetFolder()throws Exception {
        Path external=Files.createTempDirectory("preset-outside");Path link=temp.getRoot().toPath().resolve("ceqol");
        try {
            try{Files.createSymbolicLink(link,external);}catch(java.io.IOException|UnsupportedOperationException e){
                if(!System.getProperty("os.name").startsWith("Windows"))org.junit.Assume.assumeNoException(e);
                // Directory junctions exercise real-path containment without Windows symlink privilege.
                Process process=new ProcessBuilder("cmd.exe","/d","/c","mklink","/J",link.toString(),external.toString()).redirectErrorStream(true).start();
                if(!process.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();fail("Junction setup timed out");}
                assertEquals("Could not create directory junction",0,process.exitValue());
            }
            try{((PresetService)service()).list("presets");fail("External directory link accepted");}catch(java.io.IOException expected){}
            assertFalse(Files.exists(external.resolve("presets")));
        }finally{Files.deleteIfExists(link);Files.deleteIfExists(external);}
    }
    @Test public void refreshFailureRestoresOriginalBytesAndRemainsRetryable()throws Exception {
        java.util.concurrent.atomic.AtomicInteger calls=new java.util.concurrent.atomic.AtomicInteger();
        PresetService service=new PresetService(temp.getRoot().toPath(),()->{if(calls.incrementAndGet()==1)throw new IllegalStateException("cache unavailable");});
        byte[] bytes="general {\n S:unrelated=keep\n}\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Path file=temp.getRoot().toPath().resolve("sdtemperaturehud.cfg");Files.write(file,bytes);
        JsonObject doc=service.defaults("temperature");
        try{service.apply(doc);fail("Refresh should fail");}catch(java.io.IOException expected){}
        assertArrayEquals(bytes,Files.readAllBytes(file));assertEquals(original,new PresetSchema().capture("all",false));
        service.apply(doc);assertTrue(new String(Files.readAllBytes(file),java.nio.charset.StandardCharsets.UTF_8).contains("S:unrelated=keep"));
    }
    @Test public void codecRejectsDuplicatesMissingKeysWrongTypesAndVersions()throws Exception {
        PresetSchema schema=new PresetSchema();JsonObject doc=capture("hydration");
        String text=new String(PresetJson.encode(doc),java.nio.charset.StandardCharsets.UTF_8);
        String duplicate=text.replace("\"version\": 1","\"version\": 1, \"version\": 1");
        try{PresetJson.decode(duplicate.getBytes(java.nio.charset.StandardCharsets.UTF_8),schema);fail("Duplicate accepted");}catch(java.io.IOException expected){}
        doc.getAsJsonObject("settings").getAsJsonObject("hydration").remove("showTooltip");
        reject(doc,schema);
        doc=capture("hydration");doc.getAsJsonObject("settings").getAsJsonObject("hydration").addProperty("showTooltip","true");reject(doc,schema);
        doc=capture("temperature");doc.getAsJsonObject("settings").getAsJsonObject("temperature").addProperty("theme.severeCold","#GG0000");reject(doc,schema);
        doc=capture("temperature");doc.getAsJsonObject("settings").getAsJsonObject("temperature").addProperty("analysis.displayMode","FAKE");reject(doc,schema);
        doc=capture("all");doc.addProperty("version",99);reject(doc,schema);
        try{PresetJson.decode(new byte[PresetJson.MAX_BYTES+1],schema);fail("Size limit ignored");}catch(java.io.IOException expected){}
    }
    private void reject(JsonObject doc,PresetSchema schema)throws Exception {
        try{PresetJson.validate(doc,schema);fail("Invalid preset accepted");}catch(java.io.IOException expected){}
    }
}
