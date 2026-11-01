package dev.rlcraftceaddons.ceqol.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;

/** Existing config coordinates locate the text; the background has three pixels of padding. */
public final class FishingHudLayout {
    private final int x,bottom;
    public FishingHudLayout(int x,int bottom) {this.x=clamp(x,0,4096);this.bottom=clamp(bottom,0,4096);}
    public int x(){return x;}
    public int bottom(){return bottom;}
    public HudBounds bounds(int width,int height,int textWidth,int fontHeight) {
        int textX=clamp(x,4,Math.max(4,width-textWidth-4));
        int textY=clamp(height-bottom,4,Math.max(4,height-fontHeight-4));
        return new HudBounds(textX-3,textY-3,Math.max(1,textWidth)+6,Math.max(1,fontHeight)+6);
    }
    public FishingHudLayout moveTo(int outerX,int outerY,int width,int height,int textWidth,int fontHeight) {
        int textX=clamp(outerX+3,4,Math.max(4,width-textWidth-4));
        int textY=clamp(outerY+3,4,Math.max(4,height-fontHeight-4));
        return new FishingHudLayout(textX,height-textY);
    }
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
    @Override public boolean equals(Object other){return other instanceof FishingHudLayout && x==((FishingHudLayout)other).x && bottom==((FishingHudLayout)other).bottom;}
    @Override public int hashCode(){return 31*x+bottom;}
}
