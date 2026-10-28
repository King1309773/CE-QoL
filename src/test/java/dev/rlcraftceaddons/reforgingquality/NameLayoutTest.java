package dev.rlcraftceaddons.reforgingquality;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class NameLayoutTest {
    private NameLayout layout(String text) {
        return NameLayout.fit(54,31,9,width -> {
            int chars=Math.max(1,width/6);
            List<String> lines=new ArrayList<>();
            for(int i=0;i<text.length();i+=chars) lines.add(text.substring(i,Math.min(text.length(),i+chars)));
            return lines;
        });
    }
    @Test public void ordinaryNameStaysAtNormalSize() {
        NameLayout p=layout("Legendary");
        assertEquals(1F,p.scale,0);
        assertEquals("Legendary",String.join("",p.lines));
    }
    @Test public void longNameRemainsCompleteInsideCard() {
        String name="Extraordinarily Magnificent Masterwork";
        NameLayout p=layout(name);
        assertEquals(name,String.join("",p.lines));
        assertTrue(p.scale<1);
        assertTrue(p.height()<=31);
        for(String line:p.lines) assertTrue(line.length()*6*p.scale<=54);
    }
    @Test public void translatedNameIsNeverReplacedByEllipsis() {
        String name="经过大师精心锻造的不可思议的传奇品质";
        NameLayout p=NameLayout.fit(54,31,9,width -> {
            List<String> lines=new ArrayList<>();
            int chars=Math.max(1,width/9);
            for(int i=0;i<name.length();i+=chars)lines.add(name.substring(i,Math.min(name.length(),i+chars)));
            return lines;
        });
        assertEquals(name,String.join("",p.lines));
        assertTrue(p.height()<=31);
        assertFalse(String.join("",p.lines).contains("..."));
    }
}
