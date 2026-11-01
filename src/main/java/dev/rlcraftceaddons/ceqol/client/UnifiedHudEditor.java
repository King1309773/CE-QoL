package dev.rlcraftceaddons.ceqol.client;

import com.northwestwind.forgeautofish.AutoFishSettings;
import dev.rlcraftceaddons.ceqol.CEQoL;
import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import dev.rlcraftceaddons.sdtemperaturehud.client.*;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.*;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.*;
import java.io.IOException;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Shared layout editor. Every preview uses drafts; only Save commits. */
public final class UnifiedHudEditor extends GuiScreen {
    private final GuiScreen parent;
    private final HudSettings base=HudSettings.current();
    private TemperatureHudRenderer renderer;
    private HudEditorSession session;
    private TemperatureSnapshot preview;
    private TemperatureHudRenderer.CompactPanel compact;
    private TemperatureHudRenderer.AnalysisPanel analysis;
    private HudSettings previewSettings;
    private HudLayoutState previewCompact,previewAnalysis;
    private int cw,ch,aw,ah,selected,dragX,dragY;
    private boolean dragging,closed,compactWindow,failed;
    private Object textContext;
    private String title,hint,example,emptyAnalysis;
    public UnifiedHudEditor(GuiScreen parent){this.parent=parent;}
    private String tr(String key){return I18n.format("ceqol.hud."+key);}
    @Override public void initGui() {
        if(closed)return;
        if(renderer==null)renderer=new TemperatureHudRenderer(mc);
        measure();
        if(session==null)session=new HudEditorSession(base,new FishingHudLayout(AutoFishSettings.hudX,AutoFishSettings.hudBottom),width,height,cw,ch,aw,ah);
        else session.temperature().endDrag();
        dragging=false;buttonList.clear();compactWindow=width<280 || height<180;
        int y=Math.max(0,height-24);
        if(!compactWindow) {
            addButton(new GuiButton(0,width/2-134,y,80,20,I18n.format("ceqol.config.save")));
            addButton(new GuiButton(2,width/2-50,y,100,20,tr("reset")));
        }
        addButton(new GuiButton(1,compactWindow?Math.max(0,(width-80)/2):width/2+54,y,80,20,I18n.format("gui.cancel")));
    }
    private void measure() {
        Object context=HudRenderContext.current(mc);
        if(textContext!=context) {
            textContext=context;title=tr("title");hint=tr("hint");example=tr("example");emptyAnalysis=tr("empty_analysis");
        }
        TemperatureSnapshot live=SDTemperatureHud.currentSnapshot();
        if(live==null) {
            if(preview==null || !preview.isAvailable())preview=TemperatureSnapshot.withAnalysis(11,new TemperatureBreakdown(11,1,-1,2,0,13));
        }else if(!live.isAvailable() || live.hasBreakdown())preview=live;
        else {
            int body=live.getBodyTemperature(),target=live.hasTargetTemperature()?live.getTargetTemperature():body;
            TemperatureSnapshot fallback=TemperatureSnapshot.withAnalysis(body,new TemperatureBreakdown(body,0,0,0,0,target));
            if(!fallback.equals(preview))preview=fallback;
        }
        compact=renderer.measureCompact(preview,base);analysis=renderer.measureAnalysis(preview,base);
        cw=compact.getWidth();ch=compact.getHeight();
        aw=analysis==null?Math.max(96,fontRenderer.getStringWidth(emptyAnalysis)+8):analysis.getWidth();
        ah=analysis==null?16:analysis.getHeight();
    }
    private HudPanel panel(){return selected==1?HudPanel.ANALYSIS:HudPanel.COMPACT;}
    private int panelWidth(){return selected==1?aw:cw;}
    private int panelHeight(){return selected==1?ah:ch;}
    private HudBounds boundsFor(int which) {
        if(which==2)return session.fishing().bounds(width,height,FishingStatusHud.measure("waiting_bite",width).width,fontRenderer.FONT_HEIGHT);
        return session.temperature().bounds(which==1?HudPanel.ANALYSIS:HudPanel.COMPACT,width,height,which==1?aw:cw,which==1?ah:ch);
    }
    @Override public void drawScreen(int mouseX,int mouseY,float ticks) {
        if(closed)return;
        // Menus have no freshly rendered world behind them; clear old panel positions there.
        drawRect(0,0,width,height,mc.world==null?0xFF20252B:0xA020252B);
        if(compactWindow) {
            drawCenteredString(fontRenderer,I18n.format("ceqol.config.window_small"),width/2,height/2,0xFFFFFF);
            super.drawScreen(mouseX,mouseY,ticks);return;
        }
        measure();
        HudLayoutState c=session.temperature().state(HudPanel.COMPACT),a=session.temperature().state(HudPanel.ANALYSIS);
        if(!c.equals(previewCompact) || !a.equals(previewAnalysis)) {
            previewCompact=c;previewAnalysis=a;previewSettings=base.withLayouts(c,a);
        }
        WarningPanelStyle style=WarningPanelStyle.forLevel(preview.isAvailable()?TemperatureWarning.classifyBodyTemperature(preview.getBodyTemperature()):TemperatureWarningLevel.NORMAL,
                System.currentTimeMillis(),base.isWarningFlashEnabled());
        ScaledResolution resolution=new ScaledResolution(mc);
        renderer.renderAnalysis(resolution,analysis,previewSettings,style);
        if(analysis==null) {
            HudBounds box=boundsFor(1);double scale=a.getScale();
            GlStateManager.pushMatrix();
            try {GlStateManager.translate(box.getX(),box.getY(),0);GlStateManager.scale(scale,scale,1);
                drawRect(0,0,aw,ah,0xCC20242A);fontRenderer.drawStringWithShadow(emptyAnalysis,4,4,0xFFE0E0E0);
            }finally{GlStateManager.color(1,1,1,1);GlStateManager.popMatrix();}
        }
        renderer.renderCompact(resolution,compact,previewSettings,style);
        FishingStatusHud.draw("waiting_bite",session.fishing(),width,height);
        if(dragging) {
            drawRect(width/2,34,width/2+1,height-32,0x6699DDE0);
            drawRect(4,height/2,width-4,height/2+1,0x6699DDE0);
        }
        for(int i=0;i<3;i++)outline(boundsFor(i),i==selected?0xFFFFFFFF:0x7799AABB);
        drawCenteredString(fontRenderer,title,width/2,7,0xFFFFFF);
        drawCenteredString(fontRenderer,fontRenderer.trimStringToWidth(hint,width-12),width/2,19,0xCCDDDD);
        drawCenteredString(fontRenderer,fontRenderer.trimStringToWidth(failed?I18n.format("ceqol.config.save_failed"):example,width-12),width/2,height-37,failed?0xFF7777:0x99BBBB);
        super.drawScreen(mouseX,mouseY,ticks);
    }
    private void outline(HudBounds b,int color) {
        int x=b.getX()-1,y=b.getY()-1,r=b.getX()+b.getWidth()+1,d=b.getY()+b.getHeight()+1;
        drawRect(x,y,r,y+1,color);drawRect(x,d-1,r,d,color);drawRect(x,y,x+1,d,color);drawRect(r-1,y,r,d,color);
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException {
        if(closed)return;
        for(GuiButton control:buttonList)if(control.mousePressed(mc,x,y)){super.mouseClicked(x,y,button);return;}
        if(!compactWindow && button==0) {
            for(int which:new int[]{2,0,1})if(boundsFor(which).contains(x,y)) {
                selected=which;HudBounds bounds=boundsFor(which);dragX=x-bounds.getX();dragY=y-bounds.getY();dragging=true;
                if(which!=2){session.temperature().select(panel());session.temperature().beginDrag(x,y,width,height,panelWidth(),panelHeight());}
                break;
            }
        }
        super.mouseClicked(x,y,button);
    }
    @Override protected void mouseClickMove(int x,int y,int button,long elapsed) {
        if(closed || !dragging || button!=0)return;
        HudBounds current=boundsFor(selected);int left=x-dragX,top=y-dragY;
        if(!isShiftKeyDown()){left=HudSnap.position(left,current.getWidth(),width);top=HudSnap.position(top,current.getHeight(),height);}
        if(selected==2)session.fishing(session.fishing().moveTo(left,top,width,height,FishingStatusHud.measure("waiting_bite",width).width,fontRenderer.FONT_HEIGHT));
        else session.temperature().dragTo(left+dragX,top+dragY,width,height,panelWidth(),panelHeight());
    }
    @Override protected void mouseReleased(int x,int y,int button) {dragging=false;if(session!=null)session.temperature().endDrag();super.mouseReleased(x,y,button);}
    @Override public void handleMouseInput()throws IOException {
        super.handleMouseInput();int wheel=Mouse.getEventDWheel();
        if(wheel!=0 && !closed && !compactWindow) {
            mouseHandled=true;
            if(selected!=2)session.temperature().wheel(wheel,width,height,panelWidth(),panelHeight());
        }
    }
    private void resetSelected() {
        dragging=false;
        if(selected==2)session.fishing(new FishingHudLayout(8,58));
        else session.reset(panel(),width,height,cw,ch,aw,ah);
    }
    @Override protected void keyTyped(char c,int key)throws IOException {
        if(closed)return;
        if(key==Keyboard.KEY_ESCAPE){discardForLifecycle();mc.displayGuiScreen(parent);return;}
        if(key==Keyboard.KEY_R && !compactWindow){resetSelected();return;}
        super.keyTyped(c,key);
    }
    @Override protected void actionPerformed(GuiButton button) {
        if(closed)return;
        if(button.id==2){resetSelected();return;}
        if(button.id==0) {
            try {session.save(CEQoL.presets(),width,height,cw,ch,aw,ah);}
            catch(IOException|RuntimeException failure){failed=true;SDTemperatureHud.LOGGER.error("HUD layout save failed; retaining editor draft",failure);return;}
        }
        discardForLifecycle();mc.displayGuiScreen(parent);
    }
    public void discardForLifecycle(){closed=true;dragging=false;if(session!=null)session.cancel();preview=null;compact=null;analysis=null;previewSettings=null;if(renderer!=null)renderer.invalidatePresentations();}
    @Override public void onGuiClosed(){discardForLifecycle();super.onGuiClosed();}
    @Override public boolean doesGuiPauseGame(){return mc!=null && mc.isSingleplayer();}
}
