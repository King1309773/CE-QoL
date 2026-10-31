package dev.rlcraftceaddons.ceqol.config;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ConfigFileStoreTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void verifiedWriteKeepsOtherValuesAndBacksUpOriginal() throws Exception {
        File file=temp.newFile("settings.cfg");byte[] old="client {\n B:enabled=true\n S:other=keep\n}\n".getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(),old);
        write(file,c->c.get("client","enabled",true).set(false),c->!c.get("client","enabled",true).getBoolean());
        assertTrue(new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8).contains("S:other=keep"));
        assertArrayEquals(old,Files.readAllBytes(new File(file+".bak").toPath()));
    }
    @Test public void failedValidationNeverReplacesOriginal() throws Exception {
        File file=temp.newFile("settings.cfg");byte[] old="client {\n B:enabled=true\n}\n".getBytes(StandardCharsets.UTF_8);Files.write(file.toPath(),old);
        try {write(file,c->c.get("client","enabled",true).set(false),c->false);fail("Must reject invalid serialization");}
        catch(java.io.IOException expected){}
        assertArrayEquals(old,Files.readAllBytes(file.toPath()));
    }
    @Test public void unchangedWritePreservesTimestamp() throws Exception {
        File file=temp.newFile("settings.cfg");Files.write(file.toPath(),"client {\n B:enabled=true\n}\n".getBytes(StandardCharsets.UTF_8));
        Files.setLastModifiedTime(file.toPath(),java.nio.file.attribute.FileTime.fromMillis(1000000));
        write(file,c->c.get("client","enabled",true),c->true);
        assertEquals(1000000,file.lastModified());
    }
    @Test public void malformedOriginalIsPreservedWithoutRecoveryArtifacts() throws Exception {
        File file=temp.newFile("broken.cfg");byte[] old="client {\n S:other=keep\n \"unclosed\n".getBytes(StandardCharsets.UTF_8);Files.write(file.toPath(),old);
        try {write(file,c->c.get("client","enabled",true).set(false),c->true);fail("Malformed input must fail");}
        catch(java.io.IOException expected){}
        assertArrayEquals(old,Files.readAllBytes(file.toPath()));
        assertEquals("No staging or errored files may remain",1,temp.getRoot().list().length);
    }
    @Test public void defaultedMissingPropertiesCannotPassReadbackVerification() throws Exception {
        File file=temp.newFile("partial.cfg");byte[] old="client {\n S:other=keep\n}\n".getBytes(StandardCharsets.UTF_8);Files.write(file.toPath(),old);
        try {
            write(file,c->c.get("client","other","keep").set("changed"),c->c.get("client","missing",true).getBoolean());
            fail("A missing defaulted key must not verify");
        }catch(java.io.IOException expected){}
        assertArrayEquals(old,Files.readAllBytes(file.toPath()));
    }
    private void write(File file,Consumer<Configuration> edit,Predicate<Configuration> verify)throws Exception {
        java.lang.reflect.Field home=FMLInjectionData.class.getDeclaredField("minecraftHome");home.setAccessible(true);Object previous=home.get(null);home.set(null,temp.getRoot());
        try {
            Class<?> type;
            try{type=Class.forName("dev.rlcraftceaddons.ceqol.config.ConfigFileStore");}
            catch(ClassNotFoundException e){throw new AssertionError("Verified writer missing",e);}
            try{type.getMethod("write",File.class,Consumer.class,Predicate.class).invoke(null,file,edit,verify);}
            catch(InvocationTargetException e){throw (Exception)e.getCause();}
        }finally{home.set(null,previous);}
    }
}
