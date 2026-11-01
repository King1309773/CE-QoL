package dev.rlcraftceaddons.ceqol.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import java.util.Objects;

/** Changes identity when text metrics or translations can change. No world references. */
public final class HudRenderContext {
    private static volatile long generation;
    private static HudRenderContext current;
    private final FontRenderer font;
    private final String language;
    private final boolean unicode,bidi;
    private final long revision;
    private HudRenderContext(FontRenderer font,String language,boolean unicode,boolean bidi,long revision) {
        this.font=font;this.language=language;this.unicode=unicode;this.bidi=bidi;this.revision=revision;
    }
    public static void install(Minecraft minecraft) {
        if(minecraft.getResourceManager() instanceof IReloadableResourceManager)
            ((IReloadableResourceManager)minecraft.getResourceManager()).registerReloadListener(manager->invalidate());
    }
    public static void invalidate() {generation++;}
    public static Object current(Minecraft minecraft) {
        if(minecraft==null)return null;
        FontRenderer font=minecraft.fontRenderer;
        String language=minecraft.getLanguageManager().getCurrentLanguage().getLanguageCode();
        boolean unicode=font.getUnicodeFlag(),bidi=font.getBidiFlag();
        if(current==null || current.font!=font || !Objects.equals(current.language,language)
                || current.unicode!=unicode || current.bidi!=bidi || current.revision!=generation)
            current=new HudRenderContext(font,language,unicode,bidi,generation);
        return current;
    }
}
