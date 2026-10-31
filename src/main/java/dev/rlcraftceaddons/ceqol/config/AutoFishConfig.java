package dev.rlcraftceaddons.ceqol.config;

import com.northwestwind.forgeautofish.AutoFishSettings;
import java.io.File;
import java.io.IOException;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/** CE QoL's new fishing preferences; legacy temperature/hydration files remain separate. */
public final class AutoFishConfig {
    private static File file;
    private AutoFishConfig() { }
    public static void load(File path) throws IOException {
        file=path;
        Configuration config=ConfigFileStore.read(path);
        boolean status=config.getBoolean("showStatus","fishing",true,"Show a small fishing status HUD.");
        boolean protect=config.getBoolean("protectRod","fishing",true,"Stop automatic actions at the remaining durability reserve.");
        int reserve=config.getInt("durabilityReserve","fishing",10,1,100,"Stop at or below this remaining durability. First cast is manual.");
        int x=config.getInt("hudX","fishing",8,0,4096,"Status HUD distance from the left edge (scaled pixels).");
        int bottom=config.getInt("hudBottom","fishing",58,0,4096,"Status HUD distance from the bottom edge (scaled pixels).");
        apply(status,protect,reserve,x,bottom);
    }
    public static void apply(boolean status,boolean protect,int reserve,int x,int bottom)throws IOException {
        int r=Math.max(1,Math.min(100,reserve)),px=Math.max(0,Math.min(4096,x)),py=Math.max(0,Math.min(4096,bottom));
        if(file!=null)ConfigFileStore.write(file,c->{
            bool(c,"showStatus",status,true,"Show a small fishing status HUD.");
            bool(c,"protectRod",protect,true,"Stop automatic actions at the remaining durability reserve.");
            number(c,"durabilityReserve",r,10,"Stop at or below this remaining durability. First cast is manual.");
            number(c,"hudX",px,8,"Status HUD distance from the left edge (scaled pixels).");
            number(c,"hudBottom",py,58,"Status HUD distance from the bottom edge (scaled pixels).");
        },c->c.get("fishing","showStatus",true).getBoolean()==status
                && c.get("fishing","protectRod",true).getBoolean()==protect
                && c.get("fishing","durabilityReserve",10).getInt()==r
                && c.get("fishing","hudX",8).getInt()==px && c.get("fishing","hudBottom",58).getInt()==py);
        AutoFishSettings.showStatus=status;AutoFishSettings.protectRod=protect;
        AutoFishSettings.durabilityReserve=r;AutoFishSettings.hudX=px;AutoFishSettings.hudBottom=py;
    }
    private static void bool(Configuration c,String key,boolean v,boolean def,String comment) {
        Property p=c.get("fishing",key,def,comment);if(p.getBoolean()!=v)p.set(v);
    }
    private static void number(Configuration c,String key,int v,int def,String comment) {
        Property p=c.get("fishing",key,def,comment);if(p.getInt()!=v)p.set(v);
    }
}
