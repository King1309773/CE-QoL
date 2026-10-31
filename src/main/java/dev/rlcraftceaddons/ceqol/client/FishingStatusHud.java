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
        Minecraft mc=Minecraft.getMinecraft();
        String text=mc.fontRenderer.trimStringToWidth(I18n.format("ceqol.fishing.status",
                I18n.format("ceqol.fishing."+state)),Math.max(1,width-16));
        int length=mc.fontRenderer.getStringWidth(text);
        int x=Math.max(4,Math.min(AutoFishSettings.hudX,width-length-4));
        int y=Math.max(4,Math.min(height-AutoFishSettings.hudBottom,height-mc.fontRenderer.FONT_HEIGHT-4));
        try(PreviewRenderState ignored=PreviewRenderState.capture()) {
            Gui.drawRect(x-3,y-3,x+length+3,y+mc.fontRenderer.FONT_HEIGHT+3,0xA0101820);
            mc.fontRenderer.drawStringWithShadow(text,x,y,"low_durability".equals(state)?0xFF7777:0xB7E4EA);
        }
    }
}
