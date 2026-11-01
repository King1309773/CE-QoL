package dev.rlcraftceaddons.ceqol.client;

import com.northwestwind.forgeautofish.AutoFishSettings;
import com.northwestwind.forgeautofish.handler.AutoFishHandler;
import dev.rlcraftceaddons.thirstpreview.client.PreviewRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class FishingStatusHud {
    private static final PresentationCache<Line> textCache=new PresentationCache<>();
    private final AutoFishHandler handler;
    public FishingStatusHud(AutoFishHandler handler) {this.handler=handler;}
    @SubscribeEvent public void render(RenderGameOverlayEvent.Post event) {
        if(event.getType()!=RenderGameOverlayEvent.ElementType.ALL || !AutoFishSettings.showStatus)return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc.world==null || mc.player==null || mc.gameSettings.hideGUI || mc.currentScreen!=null
                || mc.player.isSpectator() || mc.player.capabilities.isCreativeMode)return;
        String state=handler.statusKey();if("off".equals(state))return;
        draw(state,event.getResolution().getScaledWidth(),event.getResolution().getScaledHeight());
    }
    public static void draw(String state,int width,int height) {
        draw(state,new FishingHudLayout(AutoFishSettings.hudX,AutoFishSettings.hudBottom),width,height);
    }
    public static Line measure(String state,int width) {
        Minecraft mc=Minecraft.getMinecraft();
        return textCache.get(state,width,HudRenderContext.current(mc),()->{
            String text=mc.fontRenderer.trimStringToWidth(I18n.format("ceqol.fishing.status",
                    I18n.format("ceqol.fishing."+state)),Math.max(1,width-16));
            return new Line(text,mc.fontRenderer.getStringWidth(text));
        });
    }
    public static void draw(String state,FishingHudLayout layout,int width,int height) {
        Minecraft mc=Minecraft.getMinecraft();Line line=measure(state,width);
        dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds bounds=layout.bounds(width,height,line.width,mc.fontRenderer.FONT_HEIGHT);
        int x=bounds.getX()+3,y=bounds.getY()+3;
        try(PreviewRenderState ignored=PreviewRenderState.capture()) {
            Gui.drawRect(bounds.getX(),bounds.getY(),bounds.getX()+bounds.getWidth(),bounds.getY()+bounds.getHeight(),0xA0101820);
            mc.fontRenderer.drawStringWithShadow(line.text,x,y,"low_durability".equals(state)?0xFF7777:0xB7E4EA);
        }
    }
    public static final class Line {
        public final String text;
        public final int width;
        private Line(String text,int width){this.text=text;this.width=width;}
    }
}
