package dev.rlcraftceaddons.ceqol.client;

/** Snap the outer panel edge/centre to screen guides within four scaled pixels. */
public final class HudSnap {
    private HudSnap(){}
    public static int position(int proposed,int panelSize,int screenSize) {
        int result=proposed,distance=5;
        for(int target:new int[]{4,(screenSize-panelSize)/2,screenSize-panelSize-4}) {
            int delta=Math.abs(target-proposed);
            if(target>=0 && delta<distance){result=target;distance=delta;}
        }
        return result;
    }
}
