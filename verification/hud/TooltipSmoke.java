package smoke;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import dev.rlcraftceaddons.ceqol.CEQoL;
import dev.rlcraftceaddons.ceqol.client.*;
import dev.rlcraftceaddons.ceqol.presets.*;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.*;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.*;
import com.northwestwind.forgeautofish.AutoFishSettings;
@Mod(modid="ceqolsmoke", name="Isolated CE QoL Integration Smoke", version="test", clientSideOnly=true, dependencies="required-after:simpledifficulty;required-after:refinedstorage;after:ceqol")
public final class TooltipSmoke {
  private boolean opened;
  @Mod.EventHandler public void init(FMLInitializationEvent event) { MinecraftForge.EVENT_BUS.register(this); }
  @SubscribeEvent public void open(GuiOpenEvent event) { if (!opened && event.getGui() instanceof GuiMainMenu) { opened=true; event.setGui(Boolean.getBoolean("smoke.restartCheck")?new HudRestartScreen():new SmokeScreen()); } }
  @SubscribeEvent(priority=EventPriority.LOWEST) public void post(RenderTooltipEvent.PostText e) { System.out.println("SMOKE_POST case="+SmokeScreen.index+" stackEmpty="+e.getStack().isEmpty()+" rows="+e.getLines().size()+" height="+e.getHeight()+" xy="+e.getX()+","+e.getY()); }
  @SubscribeEvent public void renderTick(net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent event) { if(event.phase==net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) {GuiDrive.tick();HudChecks.tick();} }
  public static final class SmokeScreen extends GuiScreen {
    static int index; static final List<String> configComments=new ArrayList<String>();
    int frames; boolean assertionsDone;
    String[] names={"rs-icons-only-apple-footer", "rs-icons-only-melon-footer", "rs-icons-only-apple-no-footer", "rs-icons-only-melon-no-footer", "forge-list-only-icons-only-apple", "forge-list-only-icons-only-melon", "rs-icons-only-narrow-wrapped-apple", "rs-icons-only-narrow-wrapped-melon", "forge-icons-only-zero", "forge-icons-only-negative3", "forge-icons-only-large40"};
    public void drawScreen(int mx,int my,float partial) {
      try {
        if (!assertionsDone) { verifySingleMod(); verifyAutoFishBinding(); verifyLegacyConfigs(); verifyQualityConstructor(); verifyGeneratedVariants(); assertionsDone=true; }
        drawRect(0,0,width,height,0xff18232d);
        drawString(fontRenderer,"CE QoL: actual merged client integration smoke",16,16,0xffffff);
        drawString(fontRenderer,names[index],16,35,0x70d3ff);
        drawString(fontRenderer,"No world loaded | fake offline identity | vanilla food",16,height-22,0x9aabbc);
        ItemStack stack=new ItemStack(index>=8 ? new net.minecraft.item.Item[]{Items.BREAD,Items.CARROT,Items.POTATO}[index-8] : (index % 2)==0 ? Items.APPLE : Items.MELON);
        List<String> lines=new ArrayList<String>(stack.getTooltip(null,ITooltipFlag.TooltipFlags.NORMAL));
        int amount=index>=8 ? new int[]{0,-3,40}[index-8] : (index%2)==0 ? 1 : 3;
        Integer decoded=snapshot(lines);
        int pointsRows=pointsRows(lines,amount);
        if(decoded==null || decoded.intValue()!=amount || pointsRows!=0) throw new AssertionError("SMOKE_ICON_ASSERT case="+index+" expected="+amount+" decoded="+decoded+" pointsRows="+pointsRows);
        if(frames==0) System.out.println("SMOKE_ICON_PASS case="+index+" amount="+amount+" pointsRows="+pointsRows);
        if (index<4 || (index>=6 && index<8)) {
          java.lang.reflect.Method m=Class.forName("com.raoulvdberge.refinedstorage.util.RenderUtils").getMethod("drawTooltipWithSmallText",List.class,List.class,boolean.class,ItemStack.class,int.class,int.class,int.class,int.class,FontRenderer.class);
          m.invoke(null,lines,Arrays.asList("RS small footer one","RS small footer two"),index<2 || index>=6,stack,index>=6 ? 20 : 65,110,index>=6 ? (mc.gameSettings.language.equals("zh_cn")?128:44) : width,height,fontRenderer);
        } else { GuiUtils.drawHoveringText(ItemStack.EMPTY,new ArrayList<String>(lines),65,110,width,height,-1,fontRenderer); }
        if (++frames>=12) {
          org.lwjgl.opengl.GL11.glFinish();
          File out=new File(System.getProperty("smoke.captures"),names[index]+".png");
          ImageIO.write(ScreenShotHelper.createScreenshot(mc.displayWidth,mc.displayHeight,mc.getFramebuffer()),"png",out);
          System.out.println("SMOKE_CAPTURE "+names[index]+" "+out.getAbsolutePath()+" rows="+lines.size()+" worldNull="+(mc.world==null));
          frames=0; if (++index>=names.length) { System.out.println("SMOKE_TOOLTIPS_COMPLETE eleven cases"); GuiDrive.start(); }
        }
      } catch (Throwable error) { error.printStackTrace(); System.out.println("SMOKE_FAILED"); mc.shutdown(); }
    }
    private static void verifySingleMod() {
      java.util.Map<String,net.minecraftforge.fml.common.ModContainer> mods=net.minecraftforge.fml.common.Loader.instance().getIndexedModList();
      java.util.List<String> old=new java.util.ArrayList<String>();
      for(String id:new String[]{"reforgingquality","visualcontrols","sdtemperaturehud","thirstpreview","forgeautofish"}) if(mods.containsKey(id)) old.add(id);
      long count=net.minecraftforge.fml.common.Loader.instance().getModList().stream().filter(m -> "ceqol".equals(m.getModId())).count();
      if(count!=1 || !old.isEmpty()) throw new AssertionError("SMOKE_SINGLE_MOD_ASSERT ceqolCount="+count+" oldIds="+old);
      System.out.println("SMOKE_SINGLE_MOD_PASS ceqolCount="+count+" oldIds="+old);
      String gui=mods.get("ceqol").getGuiClassName();
      if(!"dev.rlcraftceaddons.ceqol.client.CEQoLGuiFactory".equals(gui))throw new AssertionError("Registered ceqol GUI factory mismatch: "+gui);
      System.out.println("SMOKE_REGISTERED_GUI_FACTORY_PASS "+gui);
    }
    private static void verifyAutoFishBinding() throws Exception {
      List<net.minecraft.client.settings.KeyBinding> found=new ArrayList<net.minecraft.client.settings.KeyBinding>();
      for(net.minecraft.client.settings.KeyBinding binding:Minecraft.getMinecraft().gameSettings.keyBindings) if("key.categories.forgeautofish".equals(binding.getKeyCategory())) found.add(binding);
      if(found.size()!=1) throw new AssertionError("SMOKE_AUTOFISH_BINDING_ASSERT count="+found.size()+" expected=1 standaloneMod="+net.minecraftforge.fml.common.Loader.isModLoaded("forgeautofish"));
      net.minecraft.client.settings.KeyBinding binding=found.get(0);
      String localized=I18n.format("key.forgeautofish.autofish");
      String legacy=new net.minecraft.util.text.TextComponentTranslation("key.forgeautofish.autofish").getFormattedText();
      if(binding.getKeyCodeDefault()!=43 || binding.getKeyCode()!=43 || !binding.getKeyDescription().equals(legacy) || !localized.equals(TextFormatting.getTextWithoutFormattingCodes(binding.getKeyDescription())) || localized.equals("key.forgeautofish.autofish")) throw new AssertionError("SMOKE_AUTOFISH_BINDING_ASSERT code="+binding.getKeyCode()+" default="+binding.getKeyCodeDefault()+" description="+binding.getKeyDescription()+" localized="+localized);
      System.out.println("SMOKE_AUTOFISH_BINDING_PASS count=1 default=43 current="+binding.getKeyCode()+" category="+binding.getKeyCategory()+" description="+localized);
      Object registered=Class.forName("com.northwestwind.forgeautofish.keybind.KeyBinds").getField("autofish").get(null);
      if(registered!=binding)throw new AssertionError("AutoFish static key does not match registered binding");
      Class<?> handlerClass=Class.forName("com.northwestwind.forgeautofish.handler.AutoFishHandler");
      java.lang.reflect.Field listenersField=net.minecraftforge.fml.common.eventhandler.EventBus.class.getDeclaredField("listeners");listenersField.setAccessible(true);
      Map<?,?> listeners=(Map<?,?>)listenersField.get(MinecraftForge.EVENT_BUS);int handlers=0;
      for(Object owner:listeners.keySet())if(owner!=null && owner.getClass()==handlerClass)handlers++;
      if(handlers!=1)throw new AssertionError("SMOKE_AUTOFISH_HANDLER_ASSERT registeredHandlers="+handlers);
      Object handler=handlerClass.getConstructor().newInstance();java.lang.reflect.Field enabled=handlerClass.getDeclaredField("autofishenabled");enabled.setAccessible(true);
      Minecraft mc=Minecraft.getMinecraft();if(mc.player!=null || mc.world!=null)throw new AssertionError("AutoFish probe must remain at menu");
      enabled.setBoolean(handler,true);handlerClass.getMethod("onKeyInput",net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent.class).invoke(handler,new net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent());
      net.minecraft.client.audio.ISound sound=net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(net.minecraft.init.SoundEvents.ENTITY_BOBBER_SPLASH,1.0F);
      net.minecraftforge.client.event.sound.PlaySoundEvent soundEvent=new net.minecraftforge.client.event.sound.PlaySoundEvent(null,sound);
      enabled.setBoolean(handler,true);handlerClass.getMethod("onSound",net.minecraftforge.client.event.sound.PlaySoundEvent.class).invoke(handler,soundEvent);
      if(soundEvent.getResultSound()!=sound)throw new AssertionError("AutoFish replaced the observed splash sound");
      for(net.minecraftforge.fml.common.gameevent.TickEvent.Phase phase:net.minecraftforge.fml.common.gameevent.TickEvent.Phase.values()) {
        enabled.setBoolean(handler,true);handlerClass.getMethod("onPlayerTick",net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent.class).invoke(handler,new net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent(phase,null));
      }
      System.out.println("SMOKE_AUTOFISH_HANDLER_PASS registeredHandlers=1 nullWorldKeySoundTickSafe=true soundUnchanged=true");
    }
    private static void verifyLegacyConfigs() throws Exception {
      Class<?> temp=Class.forName("dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig");
      double scale=temp.getField("scale").getDouble(null);
      int alpha=temp.getField("backgroundAlpha").getInt(null);
      Class<?> thirst=Class.forName("dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig");
      boolean tooltip=thirst.getField("showTooltip").getBoolean(null);
      boolean hud=thirst.getField("showHud").getBoolean(null);
      boolean details=thirst.getField("showDetailsWithShift").getBoolean(null);
      if(Math.abs(scale-1.25)>0.00001 || alpha!=173 || !tooltip || hud || details) throw new AssertionError("SMOKE_CONFIG_ASSERT scale="+scale+" alpha="+alpha+" tooltip="+tooltip+" hud="+hud+" shiftGate="+details);
      System.out.println("SMOKE_CONFIG_PASS scale="+scale+" alpha="+alpha+" tooltip="+tooltip+" hud="+hud+" shiftGate="+details);
      for(String line:java.nio.file.Files.readAllLines(new File(Minecraft.getMinecraft().gameDir,"config/thirstpreview.cfg").toPath(),java.nio.charset.StandardCharsets.UTF_8)) if(line.trim().startsWith("#") && !line.trim().equals("# Configuration file")) configComments.add(line.trim());
      if(configComments.stream().noneMatch(line -> line.contains("Preview held"))) throw new AssertionError("No owned comment available after load to test save");
      System.out.println("SMOKE_CONFIG_COMMENTS_CAPTURE afterLoadCount="+configComments.size()+" comments="+configComments);
      thirst.getField("showDetailsWithShift").setBoolean(null,true);
    }
    private static void verifyQualityConstructor() throws Exception {
      Class<?> tileClass=Class.forName("com.tmtravlr.qualitytools.reforging.TileEntityReforgingStation");
      Object tile=tileClass.getConstructor().newInstance();
      net.minecraft.entity.player.InventoryPlayer inventory=new net.minecraft.entity.player.InventoryPlayer(null);
      net.minecraft.client.gui.inventory.GuiContainer original=(net.minecraft.client.gui.inventory.GuiContainer)Class.forName("com.tmtravlr.qualitytools.reforging.GuiReforgingStation").getConstructor(net.minecraft.entity.player.InventoryPlayer.class,tileClass).newInstance(inventory,tile);
      java.lang.reflect.Constructor<?> constructor=Class.forName("dev.rlcraftceaddons.reforgingquality.QualityStationScreen").getDeclaredConstructor(net.minecraft.entity.player.InventoryPlayer.class,tileClass,net.minecraft.inventory.Container.class);
      constructor.setAccessible(true);
      net.minecraft.client.gui.inventory.GuiContainer patched=(net.minecraft.client.gui.inventory.GuiContainer)constructor.newInstance(inventory,tile,original.inventorySlots);
      if(patched.inventorySlots!=original.inventorySlots || patched.inventorySlots.inventorySlots.size()!=38 || patched.inventorySlots.getSlot(0).inventory!=tile) throw new AssertionError("SMOKE_QUALITY_ASSERT");
      System.out.println("SMOKE_QUALITY_PASS actualQualityTools="+net.minecraftforge.fml.common.Loader.isModLoaded("qualitytools")+" retainedContainer=true slots="+patched.inventorySlots.inventorySlots.size()+" worldNull="+(Minecraft.getMinecraft().world==null)+" initNotCalled=true");
    }
    private static Integer snapshot(List<String> lines) throws Exception {
      return (Integer)Class.forName("dev.rlcraftceaddons.thirstpreview.client.TooltipHydrationSnapshot").getMethod("decode",List.class).invoke(null,lines);
    }
    private static int pointsRows(List<String> lines,int amount) {
      int points=0;
      String expected=I18n.format("thirstpreview.tooltip.points",Integer.toString(amount));
      for(String raw:lines) if(expected.equals(TextFormatting.getTextWithoutFormattingCodes(raw))) points++;
      return points;
    }
    private void verifyGeneratedVariants() throws Exception {
      java.lang.reflect.Field gate=Class.forName("dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig").getField("showDetailsWithShift");
      boolean original=gate.getBoolean(null);
      net.minecraft.item.Item[] foods={Items.APPLE,Items.MELON,Items.BREAD,Items.CARROT,Items.POTATO};
      int[] amounts={1,3,0,-3,40};
      String[] sats={"0.5","1.5","0.5","0.5","0.5"};
      String[] risks={"0","0","25","25","25"};
      try {
        if(GuiScreen.isShiftKeyDown()) throw new AssertionError("SMOKE physical Shift is down");
        for(int i=0;i<foods.length;i++) for(boolean details:new boolean[]{false,true}) {
          gate.setBoolean(null,!details);
          List<String> lines=new ArrayList<String>(new ItemStack(foods[i]).getTooltip(null,ITooltipFlag.TooltipFlags.NORMAL));
          Integer decoded=snapshot(lines);
          int markerRows=0,sat=0,risk=0,points=pointsRows(lines,amounts[i]);
          for(String raw:lines) {
            String line=TextFormatting.getTextWithoutFormattingCodes(raw);
            if(raw.contains("\u00a70\u00a71\u00a72\u00a73\u00a7b")) markerRows++;
            if(line.equals(I18n.format("thirstpreview.tooltip.saturation",sats[i]))) sat++;
            if(line.equals(I18n.format("thirstpreview.tooltip.dirty_chance",risks[i]))) risk++;
            if(line.contains("drops") || line.contains("补水：")) throw new AssertionError("SMOKE_VARIANT legacy drops/prefix row: "+line);
          }
          if(decoded==null || decoded.intValue()!=amounts[i] || markerRows!=1 || points!=0 || sat!=(details?1:0) || risk!=(details?1:0) || lines.size()!=(details?4:2)) throw new AssertionError("SMOKE_ICON_ASSERT amount="+amounts[i]+" details="+details+" decoded="+decoded+" markerRows="+markerRows+" pointsRows="+points+" saturationRows="+sat+" riskRows="+risk+" rows="+lines.size());
          System.out.println("SMOKE_VARIANT_PASS amount="+amounts[i]+" details="+details+" markerRows="+markerRows+" pointsRows="+points+" saturationRows="+sat+" riskRows="+risk+" rows="+lines.size());
        }
      } finally {gate.setBoolean(null,original);}
    }
    public boolean doesGuiPauseGame(){return false;}
  }
  public static final class GuiDrive {
    private static byte[] presetOptions;
    private static boolean active;
    private static int stage,frames;
    private static final int TEMP_BUTTON=1;
    static void start() { try { Class.forName("dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig").getField("showDetailsWithShift").setBoolean(null,false); } catch(Exception error){throw new RuntimeException(error);} active=true; stage=1;frames=0; Minecraft mc=Minecraft.getMinecraft(); mc.displayGuiScreen(new GuiControls(new GuiMainMenu(),mc.gameSettings)); }
    @SuppressWarnings("unchecked") private static List<GuiButton> buttons(GuiScreen screen) throws Exception {
      java.lang.reflect.Field field=net.minecraftforge.fml.relauncher.ReflectionHelper.findField(GuiScreen.class,"buttonList","field_146292_n");
      field.setAccessible(true); return (List<GuiButton>)field.get(screen);
    }
    private static void capture(String name) throws Exception {
      Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen!=null)mc.currentScreen.drawScreen(0,0,0);org.lwjgl.opengl.GL11.glFinish();
      File out=new File(System.getProperty("smoke.captures"),name+".png");
      ImageIO.write(ScreenShotHelper.createScreenshot(mc.displayWidth,mc.displayHeight,mc.getFramebuffer()),"png",out);
      System.out.println("SMOKE_GUI_CAPTURE "+name+" class="+mc.currentScreen.getClass().getName()+" worldNull="+(mc.world==null));
    }
    private static void action(GuiScreen screen,GuiButton button,List<GuiButton> buttons) throws Exception {
      net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event=new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(screen,button,buttons);
      boolean canceled=MinecraftForge.EVENT_BUS.post(event);
      if(!canceled) {
        java.lang.reflect.Method method=null;
        for(Class<?> type=screen.getClass();type!=null && method==null;type=type.getSuperclass()) try{method=type.getDeclaredMethod("actionPerformed",GuiButton.class);}catch(NoSuchMethodException ignored){try{method=type.getDeclaredMethod("func_146284_a",GuiButton.class);}catch(NoSuchMethodException alsoIgnored){}}
        if(method==null) throw new AssertionError("No GUI action method"); method.setAccessible(true);method.invoke(screen,button);
      }
    }
    static void tick() {
      if(!active || ++frames<12) return;
      try {
        Minecraft mc=Minecraft.getMinecraft();GuiScreen screen=mc.currentScreen;
        if(stage==1) {
          if(!(screen instanceof GuiControls)) throw new AssertionError("Controls not shown");
          List<GuiButton> list=buttons(screen);GuiButton entry=null;
          for(GuiButton button:list) if(button.getClass().getName().equals("dev.rlcraftceaddons.visualcontrols.client.ControlsEntry$EntryButton")) { if(entry!=null)throw new AssertionError("Duplicate visual entry");entry=button; }
          if(entry==null)throw new AssertionError("Missing visual controls entry");
          capture("controls-entry");action(screen,entry,list);
          if(!mc.currentScreen.getClass().getName().equals("dev.rlcraftceaddons.visualcontrols.client.VisualControlsScreen"))throw new AssertionError("Entry did not open VisualControlsScreen");
          System.out.println("SMOKE_CONTROLS_ENTRY_PASS id="+entry.id);stage=2;
        } else if(stage==2) {
          java.lang.reflect.Field indexField=screen.getClass().getDeclaredField("index");indexField.setAccessible(true);Object oldIndex=indexField.get(screen);
          try {
            Class<?> indexClass=oldIndex.getClass();
            net.minecraft.client.settings.KeyBinding[] sample={new CountingBinding("smoke.one",19),new CountingBinding("smoke.two",20)};
            Object sampleIndex=indexClass.getConstructor(net.minecraft.client.settings.KeyBinding[].class,java.util.function.Function.class)
              .newInstance(sample,(java.util.function.Function<String,String>)(value->value));
            int count=CountingBinding.calls;if(count==0)throw new AssertionError("Conflict probe did not execute");
            indexField.set(screen,sampleIndex);java.lang.reflect.Method refresh=screen.getClass().getDeclaredMethod("refresh");refresh.setAccessible(true);
            for(int i=0;i<10;i++)refresh.invoke(screen);
            if(CountingBinding.calls!=count)throw new AssertionError("Filtering rebuilt conflict graph");
            System.out.println("SMOKE_SEARCH_PERFORMANCE_PASS repeatedFilters=10 conflictCalls=0");
          }finally{indexField.set(screen,oldIndex);java.lang.reflect.Method refresh=screen.getClass().getDeclaredMethod("refresh");refresh.setAccessible(true);refresh.invoke(screen);}
          for(String name:new String[]{"index","editor"}) {java.lang.reflect.Field field=screen.getClass().getDeclaredField(name);field.setAccessible(true);if(field.get(screen)==null)throw new AssertionError("Visual editor not initialized: "+name);}
          capture("visual-controls-editor");
          net.minecraftforge.fml.client.IModGuiFactory factory=(net.minecraftforge.fml.client.IModGuiFactory)Class.forName("dev.rlcraftceaddons.ceqol.client.CEQoLGuiFactory").getConstructor().newInstance();
          factory.initialize(mc);if(!factory.hasConfigGui())throw new AssertionError("No ceqol config GUI");
          GuiScreen center=factory.createConfigGui(new GuiMainMenu());mc.displayGuiScreen(center);
          if(!center.getClass().getName().equals("dev.rlcraftceaddons.ceqol.client.CEQoLConfigScreen"))throw new AssertionError("Wrong ceqol config GUI");
          System.out.println("SMOKE_VISUAL_EDITOR_PASS initialized=true");stage=3;
        } else if(stage==3) {
          capture("ceqol-config-center");List<GuiButton> list=buttons(screen);GuiButton temp=null;
          for(GuiButton button:list)if(button.id==TEMP_BUTTON)temp=button;
          if(temp==null)throw new AssertionError("Temperature entry button missing");action(screen,temp,list);
          if(!mc.currentScreen.getClass().getName().equals("dev.rlcraftceaddons.sdtemperaturehud.client.config.GuiHudConfig"))throw new AssertionError("Temperature entry did not open GuiHudConfig: "+mc.currentScreen.getClass().getName());
          System.out.println("SMOKE_CONFIG_CENTER_PASS temperatureButton="+temp.id);stage=4;
        } else if(stage==4) {
          capture("temperature-config");System.out.println("SMOKE_TEMPERATURE_CONFIG_PASS worldNull="+(mc.world==null));
          mc.displayGuiScreen((GuiScreen)Class.forName("dev.rlcraftceaddons.ceqol.client.CEQoLConfigScreen").getConstructor(GuiScreen.class).newInstance(new GuiMainMenu()));stage=5;
        } else if(stage==5) {
          List<GuiButton> list=buttons(screen);GuiButton thirst=null;
          for(GuiButton button:list)if(button.id==3)thirst=button;
          if(thirst==null)throw new AssertionError("Thirst entry button missing");action(screen,thirst,list);
          if(!mc.currentScreen.getClass().getName().equals("dev.rlcraftceaddons.ceqol.client.ThirstPreviewConfigScreen"))throw new AssertionError("Thirst panel not shown");
          System.out.println("SMOKE_THIRST_ENTRY_PASS id=3");stage=6;
        } else if(stage==6) {
          capture("thirst-toggle-panel");
          Class<?> config=Class.forName("dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig");
          File file=new File(mc.gameDir,"config/thirstpreview.cfg");
          byte[] original=java.nio.file.Files.readAllBytes(file.toPath());
          long originalTime=file.lastModified();
          boolean originalHud=config.getField("showHud").getBoolean(null);
          GuiScreen parent=(GuiScreen)screen.getClass().getDeclaredConstructor(GuiScreen.class).newInstance(new GuiMainMenu());
          click(screen,2);
          if(config.getField("showHud").getBoolean(null)!=originalHud || !Arrays.equals(original,java.nio.file.Files.readAllBytes(file.toPath())) || originalTime!=file.lastModified())throw new AssertionError("Draft toggle modified live settings or disk");
          screen.setWorldAndResolution(mc,screen.width,screen.height);
          GuiButton hudButton=null;for(GuiButton b:buttons(screen))if(b.id==2)hudButton=b;
          if(hudButton==null || !hudButton.displayString.contains(I18n.format(!originalHud?"options.on":"options.off")))throw new AssertionError("Resize lost working copy");
          click(screen,4);
          if(config.getField("showHud").getBoolean(null)!=originalHud || !Arrays.equals(original,java.nio.file.Files.readAllBytes(file.toPath())))throw new AssertionError("Cancel changed settings");
          mc.displayGuiScreen(parent);screen=mc.currentScreen;click(screen,2);
          java.lang.reflect.Method escape=null;
          for(String name:new String[]{"keyTyped","func_73869_a"})try{escape=screen.getClass().getDeclaredMethod(name,char.class,int.class);break;}catch(NoSuchMethodException ignored){}
          if(escape==null)throw new AssertionError("Escape handler missing");escape.setAccessible(true);escape.invoke(screen,'\0',org.lwjgl.input.Keyboard.KEY_ESCAPE);
          if(config.getField("showHud").getBoolean(null)!=originalHud || !Arrays.equals(original,java.nio.file.Files.readAllBytes(file.toPath())))throw new AssertionError("Escape changed settings");
          for(boolean expected:new boolean[]{true,false}) {
            screen=(GuiScreen)Class.forName("dev.rlcraftceaddons.ceqol.client.ThirstPreviewConfigScreen").getConstructor(GuiScreen.class).newInstance(new GuiMainMenu());
            mc.displayGuiScreen(screen);click(screen,2);click(screen,0);
            if(config.getField("showHud").getBoolean(null)!=expected)throw new AssertionError("HUD toggle did not update in-memory setting");
            String saved=new String(java.nio.file.Files.readAllBytes(new File(mc.gameDir,"config/thirstpreview.cfg").toPath()),java.nio.charset.StandardCharsets.UTF_8);
            if(!saved.contains("B:showHud="+expected))throw new AssertionError("HUD toggle did not save legacy thirstpreview.cfg");
            if(!saved.contains("S:FixtureRetainedValue=preserve-this-fixture"))throw new AssertionError("Legacy config setting was lost");
            for(String prefix:new String[]{"# Show per-use hydration","# Preview held","# Show saturation"}) {
              if(SmokeScreen.configComments.stream().noneMatch(comment -> comment.startsWith(prefix)))throw new AssertionError("Missing owned comment after load: "+prefix);
              if(!saved.contains(prefix))throw new AssertionError("Owned config comment lost after save: "+prefix);
            }
          }
          long savedTime=file.lastModified();byte[] savedBytes=java.nio.file.Files.readAllBytes(file.toPath());
          screen=(GuiScreen)Class.forName("dev.rlcraftceaddons.ceqol.client.ThirstPreviewConfigScreen").getConstructor(GuiScreen.class).newInstance(new GuiMainMenu());
          mc.displayGuiScreen(screen);click(screen,0);
          if(savedTime!=file.lastModified() || !Arrays.equals(savedBytes,java.nio.file.Files.readAllBytes(file.toPath())))throw new AssertionError("Unchanged Save rewrote config");
          screen=(GuiScreen)Class.forName("dev.rlcraftceaddons.ceqol.client.ThirstPreviewConfigScreen").getConstructor(GuiScreen.class).newInstance(new GuiMainMenu());
          mc.displayGuiScreen(screen);click(screen,2);
          java.lang.reflect.Field remembered=config.getDeclaredField("configFile");remembered.setAccessible(true);Object prior=remembered.get(null);
          try {
            remembered.set(null,new File(mc.gameDir,"config"));click(screen,0);
            if(mc.currentScreen!=screen || config.getField("showHud").getBoolean(null)!=originalHud || !Arrays.equals(savedBytes,java.nio.file.Files.readAllBytes(file.toPath())))throw new AssertionError("Failed Save changed live settings, file or screen");
            screen.drawScreen(0,0,0);capture("hydration-save-failure");
          } finally {remembered.set(null,prior);}
          click(screen,0);
          if(config.getField("showHud").getBoolean(null)==originalHud || mc.currentScreen==screen)throw new AssertionError("Retry did not commit retained draft");
          byte[] backup=java.nio.file.Files.readAllBytes(new File(file+".bak").toPath());
          if(!Arrays.equals(backup,savedBytes))throw new AssertionError("Previous bytes were not backed up");
          System.out.println("SMOKE_SAVE_FAILURE_PASS retainedDraft=true runtimeUnchanged=true diskUnchanged=true retry=true backup=true");
          System.out.println("SMOKE_THIRST_DRAFT_PASS cancel=true escape=true resize=true save=true unchangedNoWrite=true");
          mc.displayGuiScreen((GuiScreen)Class.forName("dev.rlcraftceaddons.ceqol.client.CEQoLConfigScreen").getConstructor(GuiScreen.class).newInstance(new GuiMainMenu()));stage=7;
        } else if(stage==7) {
          List<GuiButton> list=buttons(screen);GuiButton autofish=null;for(GuiButton button:list)if(button.id==5)autofish=button;
          if(autofish==null)throw new AssertionError("AutoFish entry ID5 missing");action(screen,autofish,list);
          if(!mc.currentScreen.getClass().getName().equals("dev.rlcraftceaddons.visualcontrols.client.VisualControlsScreen"))throw new AssertionError("AutoFish entry did not open original VisualControlsScreen");
          System.out.println("SMOKE_AUTOFISH_ENTRY_PASS id=5");stage=8;
        } else if(stage==8) {
          java.lang.reflect.Field visibleField=screen.getClass().getDeclaredField("visible");visibleField.setAccessible(true);
          List<?> visible=(List<?>)visibleField.get(screen);
          Object binding=Class.forName("com.northwestwind.forgeautofish.keybind.KeyBinds").getField("autofish").get(null);
          int index=visible.indexOf(binding);java.lang.reflect.Field scrollField=screen.getClass().getDeclaredField("scroll");scrollField.setAccessible(true);int scroll=scrollField.getInt(screen);
          java.lang.reflect.Method page=screen.getClass().getDeclaredMethod("pageSize");page.setAccessible(true);int pageSize=((Integer)page.invoke(screen)).intValue();
          if(index<scroll || index>=scroll+pageSize)throw new AssertionError("AutoFish binding not visible in viewport: index="+index+" scroll="+scroll+" pageSize="+pageSize);
          java.lang.reflect.Field searchField=screen.getClass().getDeclaredField("search");searchField.setAccessible(true);GuiTextField search=(GuiTextField)searchField.get(screen);
          if(search==null || !I18n.format("key.categories.forgeautofish").equals(search.getText()) || visible.size()!=1 || visible.get(0)!=binding)throw new AssertionError("AutoFish category filter mismatch");
          capture("autofish-key-binding");System.out.println("SMOKE_AUTOFISH_VISIBLE_PASS index="+index+" scroll="+scroll+" totalFiltered="+visible.size()+" search="+(search==null?"<compact>":search.getText()));
          mc.displayGuiScreen((GuiScreen)Class.forName("dev.rlcraftceaddons.ceqol.client.AutoFishConfigScreen").getConstructor(GuiScreen.class).newInstance(new GuiMainMenu()));stage=9;
        } else if(stage==9) {
          capture("fishing-settings");
          Class<?> options=Class.forName("com.northwestwind.forgeautofish.AutoFishSettings");
          boolean initial=options.getField("showStatus").getBoolean(null);
          click(screen,1);click(screen,4);click(screen,0);
          if(options.getField("showStatus").getBoolean(null)==initial || options.getField("durabilityReserve").getInt(null)!=11)throw new AssertionError("Fishing save failed");
          File cfg=new File(mc.gameDir,"config/ceqol.cfg");String content=new String(java.nio.file.Files.readAllBytes(cfg.toPath()),java.nio.charset.StandardCharsets.UTF_8);
          if(!content.contains("I:durabilityReserve=11"))throw new AssertionError("Fishing config not persisted");
          System.out.println("SMOKE_FISHING_CONFIG_PASS reserve=11 stagedSave=true");
          mc.displayGuiScreen(new FishingPresentation());stage=10;
        } else if(stage==10) {
          capture("fishing-status-presentation");
          mc.displayGuiScreen((GuiScreen)Class.forName("dev.rlcraftceaddons.ceqol.client.CEQoLConfigScreen").getConstructor(GuiScreen.class).newInstance(new GuiMainMenu()));stage=11;
        } else if(stage==11) {
          click(screen,7);if(!mc.currentScreen.getClass().getName().endsWith("PresetConfigScreen"))throw new AssertionError("Preset entry did not open");stage=12;
        } else if(stage==12) {
          capture("presets-empty");presetOptions=java.nio.file.Files.readAllBytes(new File(mc.gameDir,"options.txt").toPath());
          java.lang.reflect.Field field=screen.getClass().getDeclaredField("name");field.setAccessible(true);((GuiTextField)field.get(screen)).setText("测试布局");click(screen,4);
          java.nio.file.Path file=new File(mc.gameDir,"config/ceqol/presets/测试布局.json").toPath();
          if(!java.nio.file.Files.exists(file) || mc.currentScreen!=screen)throw new AssertionError("Preset save failed");
          Class<?> temp=Class.forName("dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig");temp.getField("scale").setDouble(null,1.6);temp.getField("backgroundAlpha").setInt(null,99);
          Object layout=temp.getField("layout").get(null);layout.getClass().getField("version").setInt(layout,1);layout.getClass().getField("compactX").setDouble(layout,0.15);
          net.minecraftforge.common.config.ConfigManager.sync("ceqol",net.minecraftforge.common.config.Config.Type.INSTANCE);
          Class<?> thirst=Class.forName("dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig");thirst.getField("showTooltip").setBoolean(null,false);thirst.getMethod("save").invoke(null);
          Class.forName("dev.rlcraftceaddons.ceqol.config.AutoFishConfig").getMethod("apply",boolean.class,boolean.class,int.class,int.class,int.class).invoke(null,true,true,37,64,84);
          click(screen,5);if(!(mc.currentScreen instanceof GuiYesNo))throw new AssertionError("Apply was not confirmed");stage=13;
        } else if(stage==13) {
          capture("presets-apply-confirmation");click(screen,0);screen=mc.currentScreen;
          if(Math.abs(tempScale()-1.25)>0.0001 || thirstTooltip()!=true || fishingReserve()!=11)throw new AssertionError("Preset values not restored");
          net.minecraftforge.common.config.ConfigManager.sync("ceqol",net.minecraftforge.common.config.Config.Type.INSTANCE);
          if(!readCfg("sdtemperaturehud.cfg").contains("D:Scale=1.25"))throw new AssertionError("Forge cache overwrote applied preset");
          capture("presets-selected");click(screen,9);if(!(mc.currentScreen instanceof GuiYesNo))throw new AssertionError("Undo not confirmed");stage=14;
        } else if(stage==14) {
          click(screen,0);screen=mc.currentScreen;
          if(Math.abs(tempScale()-1.6)>0.0001 || thirstTooltip() || fishingReserve()!=37)throw new AssertionError("Undo did not restore prior values");
          net.minecraftforge.common.config.ConfigManager.sync("ceqol",net.minecraftforge.common.config.Config.Type.INSTANCE);
          if(!readCfg("sdtemperaturehud.cfg").contains("D:Scale=1.6"))throw new AssertionError("Forge cache overwrote undo");
          java.lang.reflect.Field scope=screen.getClass().getDeclaredField("scope");scope.setAccessible(true);scope.setInt(screen,2);screen.setWorldAndResolution(mc,screen.width,screen.height);
          click(screen,8);click(mc.currentScreen,1);screen=mc.currentScreen;
          if(thirstTooltip())throw new AssertionError("Cancelled reset changed live settings");
          click(screen,8);click(mc.currentScreen,0);screen=mc.currentScreen;
          if(!thirstTooltip() || fishingReserve()!=37 || Math.abs(tempScale()-1.6)>0.0001)throw new AssertionError("Scoped defaults affected another module");
          click(screen,6);
          java.nio.file.Path exported=new File(mc.gameDir,"config/ceqol/exports/测试布局.json").toPath();
          if(!java.nio.file.Files.exists(exported))throw new AssertionError("Export missing");
          java.nio.file.Path imports=new File(mc.gameDir,"config/ceqol/imports").toPath();java.nio.file.Files.createDirectories(imports);
          java.nio.file.Files.copy(exported,imports.resolve("朋友分享.json"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
          click(screen,7);click(screen,5);if(!(mc.currentScreen instanceof GuiYesNo))throw new AssertionError("Import overwrite not confirmed");stage=15;
        } else if(stage==15) {
          capture("presets-import-overwrite");click(screen,0);screen=mc.currentScreen;
          if(!thirstTooltip() || fishingReserve()!=37 || Math.abs(tempScale()-1.6)>0.0001)throw new AssertionError("Import applied values without approval");
          if(!Arrays.equals(presetOptions,java.nio.file.Files.readAllBytes(new File(mc.gameDir,"options.txt").toPath())))throw new AssertionError("Presets changed key options");
          capture("presets-imported");
          System.out.println("SMOKE_PRESETS_PASS save=true apply=true cacheSurvivesSync=true undo=true scopedDefaults=true cancelledReset=true export=true importDoesNotApply=true keyOptionsUnchanged=true");
          active=false;HudChecks.start();
        }
        frames=0;
      }catch(Throwable error){error.printStackTrace();System.out.println("SMOKE_FAILED gui stage="+stage);active=false;Minecraft.getMinecraft().shutdown();}
    }
    private static void click(GuiScreen screen,int id) throws Exception {
      List<GuiButton> list=buttons(screen);for(GuiButton button:list)if(button.id==id){action(screen,button,list);return;}
      throw new AssertionError("Missing button "+id);
    }
    private static double tempScale()throws Exception {return Class.forName("dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig").getField("scale").getDouble(null);}
    private static boolean thirstTooltip()throws Exception {return Class.forName("dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig").getField("showTooltip").getBoolean(null);}
    private static int fishingReserve()throws Exception {return Class.forName("com.northwestwind.forgeautofish.AutoFishSettings").getField("durabilityReserve").getInt(null);}
    private static String readCfg(String name)throws Exception {return new String(java.nio.file.Files.readAllBytes(new File(Minecraft.getMinecraft().gameDir,"config/"+name).toPath()),java.nio.charset.StandardCharsets.UTF_8);}
  }
  public static final class CountingBinding extends net.minecraft.client.settings.KeyBinding {
    static int calls;
    CountingBinding(String name,int code){super(name,code,"smoke.category");}
    @Override public boolean conflicts(net.minecraft.client.settings.KeyBinding other){calls++;return super.conflicts(other);}
  }
  public static final class HudChecks {
    private static boolean active;private static int frames,stage;private static GuiScreen parent;
    private static Map<String,byte[]> beforeFiles;private static com.google.gson.JsonObject beforeLive;
    static void start(){active=true;stage=0;frames=0;parent=new CEQoLConfigScreen(new GuiMainMenu());Minecraft.getMinecraft().displayGuiScreen(parent);}
    static Object field(Object target,String name)throws Exception {java.lang.reflect.Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
    static HudEditorSession session(GuiScreen screen)throws Exception{return (HudEditorSession)field(screen,"session");}
    static void input(GuiScreen screen,String name,String srg,Class<?>[] types,Object...args)throws Exception {
      java.lang.reflect.Method m;try{m=screen.getClass().getDeclaredMethod(name,types);}catch(NoSuchMethodException e){m=screen.getClass().getDeclaredMethod(srg,types);}m.setAccessible(true);m.invoke(screen,args);
    }
    static void drag(GuiScreen screen,int panel,int dx,int dy)throws Exception {
      java.lang.reflect.Method m=screen.getClass().getDeclaredMethod("boundsFor",int.class);m.setAccessible(true);HudBounds b=(HudBounds)m.invoke(screen,panel);
      int x=b.getX()+5,y=b.getY()+Math.min(5,b.getHeight()/2);
      input(screen,"mouseClicked","func_73864_a",new Class[]{int.class,int.class,int.class},x,y,0);
      input(screen,"mouseClickMove","func_146273_a",new Class[]{int.class,int.class,int.class,long.class},x+dx,y+dy,0,100L);
      input(screen,"mouseReleased","func_146286_b",new Class[]{int.class,int.class,int.class},x+dx,y+dy,0);
    }
    static Map<String,byte[]> files()throws Exception {
      Map<String,byte[]> result=new HashMap<>();for(String name:new String[]{"config/sdtemperaturehud.cfg","config/ceqol.cfg","config/thirstpreview.cfg","options.txt"}) {
        java.nio.file.Path p=new File(Minecraft.getMinecraft().gameDir,name).toPath();result.put(name,java.nio.file.Files.exists(p)?java.nio.file.Files.readAllBytes(p):null);
      }return result;
    }
    static void unchanged()throws Exception {Map<String,byte[]> now=files();for(String key:beforeFiles.keySet())if(!Arrays.equals(beforeFiles.get(key),now.get(key)))throw new AssertionError("Draft wrote "+key);if(!beforeLive.equals(new PresetSchema().capture("all",false)))throw new AssertionError("Draft changed live settings");}
    static void captureBaseline()throws Exception{beforeFiles=files();beforeLive=new PresetSchema().capture("all",false);}
    static void tick(){if(!active || ++frames<12)return;frames=0;Minecraft mc=Minecraft.getMinecraft();GuiScreen screen=mc.currentScreen;
      try {
        if(stage==0){GuiDrive.capture("hud-main-entry");GuiDrive.click(screen,8);if(!(mc.currentScreen instanceof UnifiedHudEditor))throw new AssertionError("Unified editor not opened");stage=1;}
        else if(stage==1){
          GuiDrive.capture("hud-unified-initial");captureBaseline();HudEditorSession draft=session(screen);HudLayoutState initial=draft.temperature().state(HudPanel.COMPACT);
          drag(screen,0,45,-25);if(initial.equals(draft.temperature().state(HudPanel.COMPACT)))throw new AssertionError("Compact mouse drag did not move");
          drag(screen,1,-45,-20);drag(screen,2,70,-35);if(draft.fishing().x()==AutoFishSettings.hudX)throw new AssertionError("Fishing drag did not move");
          unchanged();GuiDrive.capture("hud-unified-draft");int oldW=screen.width,oldH=screen.height;
          HudLayoutState moved=draft.temperature().state(HudPanel.COMPACT);FishingHudLayout fish=draft.fishing();screen.setWorldAndResolution(mc,oldW+40,oldH+20);
          if(session(screen)!=draft || !moved.equals(draft.temperature().state(HudPanel.COMPACT)) || !fish.equals(draft.fishing()))throw new AssertionError("Resize discarded draft");
          screen.setWorldAndResolution(mc,oldW,oldH);input(screen,"keyTyped","func_73869_a",new Class[]{char.class,int.class},'\0',org.lwjgl.input.Keyboard.KEY_ESCAPE);
          unchanged();if(!draft.isClosed() || mc.currentScreen!=parent)throw new AssertionError("Esc did not discard to parent");
          System.out.println("SMOKE_HUD_CANCEL_RESIZE_PASS mouseDrag=true configUnchanged=true resizeDraftRetained=true escDiscards=true");
          HudConfig.layout.version=0;net.minecraftforge.common.config.ConfigManager.sync("ceqol",net.minecraftforge.common.config.Config.Type.INSTANCE);HudSettings.refreshFromConfig();
          GuiDrive.click(parent,8);stage=2;
        }else if(stage==2){
          captureBaseline();HudEditorSession draft=session(screen);drag(screen,0,55,-25);drag(screen,1,-55,-35);drag(screen,2,85,-45);
          Map<HudPanel,HudLayoutState> expected=draft.temperature().validatedStates(screen.width,screen.height,(Integer)field(screen,"cw"),(Integer)field(screen,"ch"),(Integer)field(screen,"aw"),(Integer)field(screen,"ah"));FishingHudLayout fish=draft.fishing();
          java.lang.reflect.Field serviceField=CEQoL.class.getDeclaredField("presetService");serviceField.setAccessible(true);Object actualService=serviceField.get(null);
          java.nio.file.Path blocked=new File(mc.gameDir,"hud-save-blocked").toPath();java.nio.file.Files.createDirectories(blocked.resolve("ceqol.cfg"));
          try{serviceField.set(null,new PresetService(blocked,()->{}));GuiDrive.click(screen,0);}finally{serviceField.set(null,actualService);}
          if(mc.currentScreen!=screen || draft.isClosed() || !fish.equals(draft.fishing()) || !(Boolean)field(screen,"failed"))throw new AssertionError("Failed save lost editor/draft");unchanged();GuiDrive.capture("hud-save-failure-draft");
          GuiDrive.click(screen,0);if(mc.currentScreen!=parent || !draft.isClosed())throw new AssertionError("Retry did not save/close");
          if(HudConfig.layout.version!=1 || Math.abs(HudConfig.layout.compactX-expected.get(HudPanel.COMPACT).getNormalizedX())>.00001 || Math.abs(HudConfig.layout.analysisX-expected.get(HudPanel.ANALYSIS).getNormalizedX())>.00001 || AutoFishSettings.hudX!=fish.x() || AutoFishSettings.hudBottom!=fish.bottom())throw new AssertionError("Saved layout mismatch");
          com.google.gson.JsonObject saved=new PresetSchema().capture("all",false);net.minecraftforge.common.config.ConfigManager.sync("ceqol",net.minecraftforge.common.config.Config.Type.INSTANCE);if(!saved.equals(new PresetSchema().capture("all",false)))throw new AssertionError("Normal sync lost layout");
          if(!Arrays.equals(beforeFiles.get("options.txt"),files().get("options.txt")) || !Arrays.equals(beforeFiles.get("config/thirstpreview.cfg"),files().get("config/thirstpreview.cfg")))throw new AssertionError("HUD save changed unrelated files");
          java.nio.file.Files.write(new File(mc.gameDir,"hud-expected.json").toPath(),saved.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
          System.out.println("SMOKE_HUD_SAVE_PASS legacyBothMigrated=true saveRetry=true cacheSurvivesSync=true unrelatedSettingsPreserved=true");
          GuiDrive.click(parent,8);stage=3;
        }else if(stage==3){
          GuiDrive.capture("hud-unified-saved");captureBaseline();HudEditorSession draft=session(screen);drag(screen,2,30,-10);
          MinecraftForge.EVENT_BUS.post(new GuiOpenEvent(new GuiMainMenu()));if(!draft.isClosed())throw new AssertionError("Replacement did not discard");unchanged();mc.displayGuiScreen(parent);
          checkCaches(mc);System.out.println("SMOKE_HUD_LIFECYCLE_PASS replacementDiscards=true");
          System.out.println("SMOKE_COMPLETE ceqol 1.4.0 HUD cache and existing client integration");active=false;mc.shutdown();
        }
      }catch(Throwable failure){failure.printStackTrace();System.out.println("SMOKE_FAILED HUD stage="+stage);active=false;mc.shutdown();}
    }
    static void checkCaches(Minecraft mc)throws Exception {
      TemperatureHudRenderer renderer=new TemperatureHudRenderer(mc);HudSettings settings=HudSettings.current();TemperatureBreakdown b=new TemperatureBreakdown(11,1,-1,2,0,13);
      Object compact=renderer.measureCompact(TemperatureSnapshot.withTarget(11,13),settings),analysis=renderer.measureAnalysis(b,settings);
      for(int i=0;i<120;i++){if(compact!=renderer.measureCompact(TemperatureSnapshot.withTarget(11,13),settings) || analysis!=renderer.measureAnalysis(new TemperatureBreakdown(11,1,-1,2,0,13),settings))throw new AssertionError("Repeated measurements not cached");}
      if(compact==renderer.measureCompact(TemperatureSnapshot.withTarget(12,13),settings))throw new AssertionError("Changed temperature stale");
      Object fish=FishingStatusHud.measure("waiting_bite",512);for(int i=0;i<120;i++)if(fish!=FishingStatusHud.measure("waiting_bite",512))throw new AssertionError("Fishing text not cached");
      if(fish==FishingStatusHud.measure("recasting",512) || fish==FishingStatusHud.measure("waiting_bite",400))throw new AssertionError("Fishing state/width stale");
      Object context=HudRenderContext.current(mc);boolean unicode=mc.fontRenderer.getUnicodeFlag(),bidi=mc.fontRenderer.getBidiFlag();mc.fontRenderer.setUnicodeFlag(!unicode);if(context==HudRenderContext.current(mc))throw new AssertionError("Unicode stale");mc.fontRenderer.setUnicodeFlag(unicode);
      context=HudRenderContext.current(mc);mc.fontRenderer.setBidiFlag(!bidi);if(context==HudRenderContext.current(mc))throw new AssertionError("Bidi stale");mc.fontRenderer.setBidiFlag(bidi);
      net.minecraft.client.resources.Language language=mc.getLanguageManager().getCurrentLanguage();context=HudRenderContext.current(mc);for(net.minecraft.client.resources.Language candidate:mc.getLanguageManager().getLanguages())if(candidate.getLanguageCode().equals(language.getLanguageCode().equals("en_us")?"zh_cn":"en_us")){mc.getLanguageManager().setCurrentLanguage(candidate);break;}if(context==HudRenderContext.current(mc))throw new AssertionError("Language stale");mc.getLanguageManager().setCurrentLanguage(language);
      context=HudRenderContext.current(mc);Object previous=renderer.measureCompact(TemperatureSnapshot.withTarget(11,13),settings);mc.refreshResources();if(context==HudRenderContext.current(mc) || previous==renderer.measureCompact(TemperatureSnapshot.withTarget(11,13),settings))throw new AssertionError("Resource reload stale");
      System.out.println("SMOKE_HUD_CACHE_PASS equalSamples120=true fishing120=true stateWidth=true unicode=true bidi=true language=true realReload=true");
    }
  }
  public static final class HudRestartScreen extends GuiScreen {
    private int frames;public void drawScreen(int x,int y,float partial){try{
      com.google.gson.JsonObject expected=new com.google.gson.JsonParser().parse(new String(java.nio.file.Files.readAllBytes(new File(mc.gameDir,"hud-expected.json").toPath()),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
      if(!expected.equals(new PresetSchema().capture("all",false)))throw new AssertionError("HUD layout did not survive restart");
      drawDefaultBackground();drawCenteredString(fontRenderer,"HUD layouts persisted after restart: PASS",width/2,height/2,0x77FF99);
      if(++frames==12){GuiDrive.capture("hud-restart-persistence");System.out.println("SMOKE_HUD_RESTART_PASS exactAllSettings=true");System.out.println("SMOKE_COMPLETE ceqol 1.4.0 restart persistence");mc.shutdown();}
    }catch(Throwable failure){failure.printStackTrace();System.out.println("SMOKE_FAILED HUD restart");mc.shutdown();}}
  }
  public static final class RestartScreen extends GuiScreen {
    int frames;
    public void drawScreen(int x,int y,float ticks) {
      try {
        if(Math.abs(GuiDrive.tempScale()-1.6)>0.0001 || !GuiDrive.thirstTooltip() || GuiDrive.fishingReserve()!=37)throw new AssertionError("Restart lost applied or undone values");
        Class<?> temp=Class.forName("dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig");Object layout=temp.getField("layout").get(null);
        if(layout.getClass().getField("version").getInt(layout)!=1 || Math.abs(layout.getClass().getField("compactX").getDouble(layout)-0.15)>0.0001)throw new AssertionError("Restart lost layout position");
        drawDefaultBackground();drawCenteredString(fontRenderer,"Preset persistence after restart: PASS",width/2,height/2,0x77ff99);
        if(++frames==12){GuiDrive.capture("presets-restart-state");System.out.println("SMOKE_PRESET_RESTART_PASS scale=1.6 compactX=0.15 layoutVersion=1 hydration=true reserve=37");System.out.println("SMOKE_COMPLETE ceqol 1.3.0 restart persistence");mc.shutdown();}
      }catch(Throwable e){e.printStackTrace();System.out.println("SMOKE_FAILED restart");mc.shutdown();}
    }
  }
  public static final class FishingPresentation extends GuiScreen {
    public void drawScreen(int mx,int my,float ticks) {
      drawDefaultBackground();drawCenteredString(fontRenderer,"Fishing status presentation (no world)",width/2,15,0xffffff);
      try {
        Class<?> options=Class.forName("com.northwestwind.forgeautofish.AutoFishSettings");
        java.lang.reflect.Field x=options.getField("hudX"),bottom=options.getField("hudBottom");int oldX=x.getInt(null),oldBottom=bottom.getInt(null);
        try {int y=45;for(String state:new String[]{"manual_cast","waiting_bite","recasting","waiting_hook","no_rod","manual_reset","low_durability"}) {
          x.setInt(null,20);bottom.setInt(null,height-y);
          Class.forName("dev.rlcraftceaddons.ceqol.client.FishingStatusHud").getMethod("draw",String.class,int.class,int.class).invoke(null,state,width,height);y+=30;
        }}finally{x.setInt(null,oldX);bottom.setInt(null,oldBottom);}
      }catch(Exception failure){throw new RuntimeException(failure);}
    }
  }
}
