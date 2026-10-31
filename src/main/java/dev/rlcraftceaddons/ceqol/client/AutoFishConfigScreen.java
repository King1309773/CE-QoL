package dev.rlcraftceaddons.ceqol.client;

import com.northwestwind.forgeautofish.AutoFishSettings;
import dev.rlcraftceaddons.ceqol.config.AutoFishConfig;
import dev.rlcraftceaddons.visualcontrols.client.VisualControlsScreen;
import java.io.IOException;
import java.util.Collections;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

/** Working copy survives resizing and opening the binding editor. */
public final class AutoFishConfigScreen extends GuiScreen {
    private final GuiScreen parent;
    private boolean status=AutoFishSettings.showStatus,protect=AutoFishSettings.protectRod,failed;
    private int reserve=AutoFishSettings.durabilityReserve,x=AutoFishSettings.hudX,bottom=AutoFishSettings.hudBottom;
    private boolean compact;
    public AutoFishConfigScreen(GuiScreen parent) {this.parent=parent;}
    private String tr(String key,Object... args) {return I18n.format("ceqol.fishing.config."+key,args);}
    private String state(boolean value) {return I18n.format(value?"options.on":"options.off");}
    @Override public void initGui() {
        buttonList.clear();compact=width<240 || height<200;
        int w=Math.max(1,Math.min(280,width-24)),left=(width-w)/2;
        if(compact) {addButton(new GuiButton(9,left,Math.max(0,height-24),w,20,I18n.format("gui.cancel")));return;}
        int top=Math.max(30,(height-164)/2),h=20,gap=4;
        addButton(new GuiButton(1,left,top,w,h,tr("status",state(status))));
        addButton(new GuiButton(2,left,top+24,w,h,tr("protect",state(protect))));
        addButton(new GuiButton(3,left,top+48,24,h,"-"));
        GuiButton amount=addButton(new GuiButton(30,left+28,top+48,w-56,h,tr("reserve",reserve)));amount.enabled=false;
        addButton(new GuiButton(4,left+w-24,top+48,24,h,"+"));
        addButton(new GuiButton(5,left,top+72,w/2-2,h,tr("left",x)));
        addButton(new GuiButton(6,left+w/2+2,top+72,w/2-2,h,tr("up",bottom)));
        addButton(new GuiButton(7,left,top+96,w,h,tr("keys")));
        addButton(new GuiButton(8,left,top+120,w,h,tr("position_reset")));
        addButton(new GuiButton(0,left,top+144,w/2-3,h,I18n.format("ceqol.config.save")));
        addButton(new GuiButton(9,left+w/2+3,top+144,w/2-3,h,I18n.format("gui.cancel")));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if(!button.enabled)return;
        switch(button.id) {
            case 0:
                try {AutoFishConfig.apply(status,protect,reserve,x,bottom);mc.displayGuiScreen(parent);}
                catch(IOException failure) {failed=true;}
                return;
            case 9:mc.displayGuiScreen(parent);return;
            case 1:status=!status;break;
            case 2:protect=!protect;break;
            case 3:reserve=Math.max(1,reserve-1);break;
            case 4:reserve=Math.min(100,reserve+1);break;
            case 5:x=Math.max(0,Math.min(4096,x+(isShiftKeyDown()?-8:8)));break;
            case 6:bottom=Math.max(0,Math.min(4096,bottom+(isShiftKeyDown()?-8:8)));break;
            case 7:mc.displayGuiScreen(new VisualControlsScreen(this,I18n.format("key.categories.forgeautofish")));return;
            case 8:x=8;bottom=58;break;
            default:return;
        }
        initGui();
    }
    @Override protected void keyTyped(char character,int key)throws IOException {
        if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);else super.keyTyped(character,key);
    }
    @Override public void drawScreen(int mx,int my,float partial) {
        drawDefaultBackground();
        if(width>=80 && height>=30)drawCenteredString(fontRenderer,fontRenderer.trimStringToWidth(
                I18n.format(failed?"ceqol.config.save_failed":"ceqol.config.fishing"),Math.max(1,width-16)),width/2,10,failed?0xFF7777:0xFFFFFF);
        if(compact && height>=70)drawCenteredString(fontRenderer,fontRenderer.trimStringToWidth(
                I18n.format("ceqol.config.window_small"),Math.max(1,width-16)),width/2,height/2-10,0xCCCCCC);
        super.drawScreen(mx,my,partial);
        if(!compact)for(GuiButton b:buttonList)if(b.isMouseOver()) {
            String key=b.id==5 || b.id==6?"position_hint":b.id==2 || b.id==3 || b.id==4 || b.id==30?"protect_hint":"save_hint";
            drawHoveringText(Collections.singletonList(tr(key)),mx,my);break;
        }
    }
}
