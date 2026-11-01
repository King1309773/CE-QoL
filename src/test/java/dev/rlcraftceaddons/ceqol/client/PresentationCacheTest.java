package dev.rlcraftceaddons.ceqol.client;

import org.junit.Test;
import static org.junit.Assert.*;

public class PresentationCacheTest {
    @Test public void unchangedValuesAvoidRepeatingFormatterAndNullIsCached() {
        PresentationCache<String> cache=new PresentationCache<>();int[] calls={0};Object context=new Object();
        assertNull(cache.get("a",10,context,()->{calls[0]++;return null;}));
        assertNull(cache.get(new String("a"),10,context,()->{calls[0]++;return "unexpected";}));
        assertEquals(1,calls[0]);
        assertEquals("b",cache.get("b",10,context,()->{calls[0]++;return "b";}));
        assertEquals("wide",cache.get("b",20,context,()->{calls[0]++;return "wide";}));
        assertEquals("reload",cache.get("b",20,new Object(),()->{calls[0]++;return "reload";}));
        cache.clear();assertEquals("cleared",cache.get("b",20,context,()->{calls[0]++;return "cleared";}));
        assertEquals(5,calls[0]);
    }
}
