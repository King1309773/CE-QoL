package dev.rlcraftceaddons.reforgingquality;

final class PanelLayout {
    final int x, y, width, height;
    final boolean compact;
    final int footerHeight;
    static int stationHeight(int screenHeight) { return Math.max(166,Math.min(238,screenHeight-2)); }
    PanelLayout(int x, int y, int width, int height, boolean compact) {
        this(x,y,width,height,compact,0);
    }
    private PanelLayout(int x, int y, int width, int height, boolean compact, int footerHeight) {
        this.x=x; this.y=y; this.width=width; this.height=height; this.compact=compact;
        this.footerHeight=footerHeight;
    }
    PanelLayout withFooter(int footer) { return new PanelLayout(x,y,width,height,compact,footer); }
    int textHeight() { return Math.max(1,height-10-footerHeight); }
    static PanelLayout create(int screenWidth, int screenHeight, int left, int top, int guiWidth, int guiHeight) {
        // All effects stay inside the 176-wide station; inventory occupies the bottom 96 pixels.
        return new PanelLayout(left+42, top+18, 126, guiHeight-118, true);
    }
    boolean contains(int px, int py) {
        return px >= x && py >= y && px < x+width && py < y+height;
    }
}
