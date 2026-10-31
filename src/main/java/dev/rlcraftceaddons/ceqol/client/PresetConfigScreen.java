package dev.rlcraftceaddons.ceqol.client;

import com.google.gson.JsonObject;
import dev.rlcraftceaddons.ceqol.presets.*;
import dev.rlcraftceaddons.ceqol.CEQoL;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

/** All operations are explicit clicks, never render/tick driven. Import does not apply. */
public final class PresetConfigScreen extends GuiScreen {
    private final GuiScreen parent;
    private final PresetService service;
    private GuiTextField name;
    private String nameText="",message="";
    private String preferredSelection;
    private boolean importMode,compact,repeatCaptured,previousRepeat;
    private int selected,scope;
    private List<String> entries=Collections.emptyList();
    private Action pending;
    private interface Action {void run()throws IOException;}
    public PresetConfigScreen(GuiScreen parent) {this.parent=parent;this.service=CEQoL.presets();}
    private String tr(String key,Object...args){return I18n.format("ceqol.presets."+key,args);}
    private String scopeKey(){return scope==0?"all":PresetSchema.GROUPS.get(scope-1);}
    private String selectedName(){return entries.isEmpty()?null:entries.get(selected);}
    @Override public void initGui() {
        if(name!=null)nameText=name.getText();
        if(!repeatCaptured){previousRepeat=Keyboard.areRepeatEventsEnabled();repeatCaptured=true;}Keyboard.enableRepeatEvents(true);
        buttonList.clear();compact=width<280 || height<228;
        int w=Math.max(1,Math.min(300,width-24)),left=(width-w)/2;
        if(compact){name=null;addButton(new GuiButton(0,left,Math.max(0,height-24),w,20,I18n.format("gui.done")));return;}
        String previous=preferredSelection==null?selectedName():preferredSelection;preferredSelection=null;
        try{entries=service.list(importMode?"imports":"presets");}catch(IOException e){entries=Collections.emptyList();message=tr("failed");}
        if(previous!=null && entries.contains(previous))selected=entries.indexOf(previous);
        selected=entries.isEmpty()?0:Math.max(0,Math.min(selected,entries.size()-1));
        int top=Math.max(34,(height-188)/2),half=(w-6)/2;
        name=new GuiTextField(0,fontRenderer,left,top,w,18);name.setMaxStringLength(32);name.setText(nameText);name.setFocused(false);
        addButton(new GuiButton(1,left,top+24,24,20,"<"));
        GuiButton label=addButton(new GuiButton(30,left+28,top+24,w-56,20,fontRenderer.trimStringToWidth(
                selectedName()==null?tr("empty"):selectedName(),w-64)));label.enabled=false;
        addButton(new GuiButton(2,left+w-24,top+24,24,20,">"));
        addButton(new GuiButton(3,left,top+48,w,20,tr("scope",tr("scope."+scopeKey()))));
        addButton(new GuiButton(4,left,top+72,w,20,tr("save_current")));
        addButton(new GuiButton(5,left,top+96,half,20,tr(importMode?"import_selected":"apply")));
        addButton(new GuiButton(6,left+half+6,top+96,half,20,tr("export")));
        addButton(new GuiButton(7,left,top+120,half,20,tr(importMode?"back_to_presets":"imports")));
        addButton(new GuiButton(10,left+half+6,top+120,half,20,tr("folder")));
        addButton(new GuiButton(8,left,top+144,half,20,tr("defaults")));
        addButton(new GuiButton(9,left+half+6,top+144,half,20,tr("undo")));
        addButton(new GuiButton(0,left,top+168,w,20,I18n.format("gui.done")));
        for(GuiButton b:buttonList) {
            if(b.id==1||b.id==2||b.id==5||b.id==6)b.enabled=selectedName()!=null;
            if(b.id==6 && importMode)b.enabled=false;
            if(b.id==9)try{b.enabled=service.hasUndo();}catch(IOException e){b.enabled=false;}
        }
    }
    private void confirm(String title,String detail,Action action) {
        nameText=name==null?nameText:name.getText();pending=action;
        mc.displayGuiScreen(new GuiYesNo(this,title,detail,1));
    }
    @Override public void confirmClicked(boolean yes,int id) {
        Action action=pending;pending=null;
        if(yes&&action!=null)run(action);
        mc.displayGuiScreen(this);
    }
    private void run(Action action) {
        try{message=tr("success");action.run();}catch(IOException|RuntimeException e){message=tr("failed");}
    }
    private void writeOrConfirm(String target,Action first,Action replacement)throws IOException {
        try{message=tr("success");first.run();}
        catch(FileAlreadyExistsException exists){confirm(tr("overwrite"),tr("overwrite_detail",target),replacement);}
    }
    @Override protected void actionPerformed(GuiButton button) {
        if(!button.enabled)return;
        try {
            String chosen=selectedName();
            switch(button.id) {
                case 0:mc.displayGuiScreen(parent);return;
                case 1:selected=(selected+entries.size()-1)%entries.size();break;
                case 2:selected=(selected+1)%entries.size();break;
                case 3:scope=(scope+1)%4;break;
                case 4:
                    JsonObject snapshot=service.capture(name.getText(),scopeKey());
                    writeOrConfirm(snapshot.get("name").getAsString(),()->save(snapshot,false),()->save(snapshot,true));break;
                case 5:
                    if(importMode){
                        JsonObject imported=service.inspectImport(chosen);
                        writeOrConfirm(imported.get("name").getAsString(),()->importFile(imported,false),()->importFile(imported,true));break;
                    }
                    JsonObject doc=service.read(chosen);confirm(tr("confirm_apply"),tr("apply_detail",groups(doc)),()->service.apply(doc));return;
                case 6:writeOrConfirm(chosen,()->export(chosen,false),()->export(chosen,true));break;
                case 7:importMode=!importMode;selected=0;entries=Collections.emptyList();break;
                case 8:
                    JsonObject defaults=service.defaults(scopeKey());confirm(tr("confirm_defaults"),tr("apply_detail",groups(defaults)),()->service.apply(defaults));return;
                case 9:confirm(tr("undo"),tr("undo_detail"),service::undo);return;
                case 10:
                    java.awt.Desktop.getDesktop().open((importMode?service.directory("imports"):service.directory("presets").getParent()).toFile());break;
                default:return;
            }
            if(mc.currentScreen==this)initGui();
        }catch(IOException|RuntimeException e){message=tr("failed");}
    }
    private void save(JsonObject doc,boolean replace)throws IOException {
        service.save(doc,replace);importMode=false;preferredSelection=doc.get("name").getAsString();
    }
    private void importFile(JsonObject doc,boolean replace)throws IOException {
        service.save(doc,replace);importMode=false;preferredSelection=doc.get("name").getAsString();message=tr("imported");
    }
    private void export(String filename,boolean replace)throws IOException {service.export(filename,replace);message=tr("exported");}
    private String groups(JsonObject doc) {
        List<String> names=new ArrayList<>();for(Map.Entry<String,com.google.gson.JsonElement> group:doc.getAsJsonObject("settings").entrySet())names.add(tr("scope."+group.getKey()));
        return String.join(", ",names);
    }
    @Override public void updateScreen(){if(name!=null)name.updateCursorCounter();}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException {if(name!=null)name.mouseClicked(x,y,button);super.mouseClicked(x,y,button);}
    @Override protected void keyTyped(char c,int key)throws IOException {
        if(key==Keyboard.KEY_ESCAPE){mc.displayGuiScreen(parent);return;}
        if(name!=null && name.textboxKeyTyped(c,key))nameText=name.getText();else super.keyTyped(c,key);
    }
    @Override public void onGuiClosed(){if(name!=null)nameText=name.getText();if(repeatCaptured)Keyboard.enableRepeatEvents(previousRepeat);repeatCaptured=false;}
    @Override public void drawScreen(int x,int y,float ticks) {
        drawDefaultBackground();
        if(width>=80&&height>=30)drawCenteredString(fontRenderer,fontRenderer.trimStringToWidth(tr(importMode?"import_title":"title"),Math.max(1,width-16)),width/2,10,0xFFFFFF);
        if(compact){if(height>=70)drawCenteredString(fontRenderer,I18n.format("ceqol.config.window_small"),width/2,height/2-10,0xCCCCCC);}
        else {
            drawCenteredString(fontRenderer,fontRenderer.trimStringToWidth(tr(importMode?"import_hint":"name_hint"),width-16),width/2,24,0xBBBBBB);
            name.drawTextBox();
            drawCenteredString(fontRenderer,fontRenderer.trimStringToWidth(message,width-16),width/2,height-14,0xDDDD99);
        }
        super.drawScreen(x,y,ticks);
    }
}
