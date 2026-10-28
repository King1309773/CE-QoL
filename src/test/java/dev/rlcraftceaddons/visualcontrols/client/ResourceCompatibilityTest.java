package dev.rlcraftceaddons.visualcontrols.client;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResourceCompatibilityTest {
    @Test public void lowercaseLanguagesDeclareThe112ResourcePackFormat() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/pack.mcmeta")) {
            assertNotNull("Without metadata Forge assumes format 2 and misses lowercase language names", stream);
            assertEquals(3, new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("pack").get("pack_format").getAsInt());
        }
    }
    @Test public void bothLanguagesHaveCompleteMatchingLabels() throws Exception {
        Properties english = language("en_us"), chinese = language("zh_cn");
        assertEquals(english.keySet(), chinese.keySet());
        assertEquals("Visual Key Map", english.getProperty("visualcontrols.title"));
        assertEquals("可视化键位图", chinese.getProperty("visualcontrols.title"));
        assertFalse(chinese.getProperty("visualcontrols.recording").isEmpty());
    }
    private Properties language(String name) throws Exception {
        Properties result = new Properties();
        try (InputStream stream = getClass().getResourceAsStream("/assets/visualcontrols/lang/" + name + ".lang")) {
            assertNotNull(stream);
            result.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        return result;
    }
}
