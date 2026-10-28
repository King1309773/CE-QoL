package dev.rlcraftceaddons.reforgingquality;

import java.util.List;
import java.util.function.IntFunction;

/** Complete formatted names fitted into the card, using the active font's wrapping. */
final class NameLayout {
    final List<String> lines;
    final float scale;
    final int lineHeight;
    private NameLayout(List<String> lines, float scale, int lineHeight) {
        this.lines=lines; this.scale=scale; this.lineHeight=lineHeight;
    }
    static NameLayout fit(int width, int height, int fontHeight, IntFunction<List<String>> wrap) {
        float scale=1F;
        int lineHeight=fontHeight+1;
        List<String> lines=wrap.apply(width);
        // Rewrap at the active font's actual metrics as the text shrinks; never cut characters.
        while (lines.size()*lineHeight*scale > height) {
            scale *= .9F;
            lines=wrap.apply(Math.max(1,(int)Math.floor(width/scale)));
        }
        return new NameLayout(lines,scale,lineHeight);
    }
    float height() { return lines.size()*lineHeight*scale; }
}
