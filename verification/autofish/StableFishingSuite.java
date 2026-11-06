package autofish.tests;

import com.northwestwind.forgeautofish.handler.AutoFishHandler;
import com.northwestwind.forgeautofish.keybind.KeyBinds;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class StableFishingSuite {
    private static final UUID ID=UUID.fromString("28fd2a08-85ee-409b-a6f8-cd1830a07d9a");
    private static final String BITE="minecraft:entity.bobber.splash";
    private static int passed,failed;
    private interface Check {void run()throws Exception;}

    public static void main(String[] args)throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Expected target JAR");
        File expected=new File(args[0]).getCanonicalFile();
        File actual=new File(AutoFishHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
        require(expected.equals(actual),"handler did not load from requested JAR");
        System.out.println("REAL_HANDLER_SOURCE "+actual);

        test("sinking_new_hook_without_bite_sound_never_reels",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.tick(120);
            require(f.count()==0,"new sinking hook emitted "+f.count()+" use packets without any bite sound");
        }});
        test("generic_water_splash_ignored",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound("minecraft:entity.generic.splash"); f.tick(30); f.expect(0);
        }});
        test("bobber_throw_and_retrieve_sounds_ignored",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound("minecraft:entity.bobber.throw"); f.sound("minecraft:entity.bobber.retrieve"); f.tick(30); f.expect(0);
        }});
        test("non_minecraft_namespace_ignored",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound("othermod:entity.bobber.splash"); f.tick(30); f.expect(0);
        }});
        test("far_away_bite_sound_ignored",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.soundAt(BITE,9.01f,64,8); f.tick(30); f.expect(0);
        }});
        test("one_block_sound_radius_is_inclusive",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.soundAt(BITE,9,64,8); f.tick(); f.expect(1);
        }});
        test("sound_only_queues_and_END_tick_reels",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE); f.expect(0); f.tick(); f.expect(1); f.checkPackets();
        }});
        test("one_reel_per_hook_despite_repeated_sounds_and_ticks",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE); f.tick(); f.expect(1);
            for(int i=0;i<100;i++){f.sound(BITE);f.tick();}
            f.expect(1);
        }});
        test("recast_waits_at_least_ten_END_ticks_and_sends_once",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.player.field_71104_cf=null;
            f.tick(9); f.expect(1); f.tick(); f.expect(2); f.checkPackets();
        }});
        test("old_hook_still_present_blocks_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.tick(20); f.expect(1);
        }});
        test("old_hook_timeout_cancels_without_retry",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.tick(65); f.player.field_71104_cf=null; f.tick(30); f.expect(1);
        }});
        test("missing_new_spawn_after_recast_never_retries",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.player.field_71104_cf=null; f.tick(10); f.expect(2); f.tick(200); f.expect(2);
        }});
        test("manual_new_hook_cancels_previous_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.newHook(); f.tick(100); f.expect(1);
        }});
        test("new_hook_requires_its_own_bite_signal",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.player.field_71104_cf=null; f.tick(10); f.expect(2);
            f.newHook(); f.tick(100); f.expect(2); f.sound(BITE); f.tick(); f.expect(3);
        }});
        test("disable_cancels_pending_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.enabled(false); f.tick(); f.player.field_71104_cf=null;
            f.enabled(true); f.tick(30); f.expect(1);
        }});
        test("disable_cancels_queued_bite",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE); f.enabled(false); f.tick(); f.enabled(true); f.tick(); f.expect(0);
        }});
        test("switching_to_non_rod_cancels_pending_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.player.held=new ItemStack(new Item()); f.tick();
            f.player.held=new ItemStack(new ItemFishingRod()); f.player.field_71104_cf=null; f.tick(30); f.expect(1);
        }});
        test("empty_main_hand_cancels_queued_bite",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE); f.player.held=ItemStack.EMPTY; f.tick(); f.expect(0);
        }});
        test("connection_change_cancels_pending_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); NetHandlerPlayClient next=new NetHandlerPlayClient();
            f.mc.connection=next; f.player.field_71104_cf=null; f.tick(30);
            f.expect(1); require(next.packets.size()==0,"old session sent on new connection");
        }});
        test("world_change_cancels_pending_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.mc.field_71441_e=new WorldClient();
            f.player.field_70170_p=f.mc.field_71441_e; f.player.field_71104_cf=null; f.tick(30); f.expect(1);
        }});
        test("player_change_cancels_pending_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); EntityPlayerSP next=new EntityPlayerSP(ID);
            next.field_70170_p=f.mc.field_71441_e;next.held=new ItemStack(new ItemFishingRod());
            f.mc.field_71439_g=next; f.tickFor(next,30); f.expect(1);
        }});
        test("null_client_player_cancels_pending_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.reel(); f.mc.field_71439_g=null; f.tick();
            f.mc.field_71439_g=f.player;f.player.field_71104_cf=null;f.tick(30);f.expect(1);
        }});
        test("null_world_cancels_queued_bite",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE);WorldClient world=f.mc.field_71441_e;
            f.mc.field_71441_e=null;f.tick();f.mc.field_71441_e=world;f.tick();f.expect(0);
        }});
        test("null_connection_cancels_queued_bite",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE);f.mc.connection=null;f.tick();
            f.mc.connection=f.connection;f.tick();f.expect(0);
        }});
        test("dead_player_cancels_queued_bite",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE);f.player.field_70128_L=true;f.tick();
            f.player.field_70128_L=false;f.tick();f.expect(0);
        }});
        test("START_and_server_ticks_do_not_consume_bite",new Check(){public void run()throws Exception{
            Fixture f=new Fixture(); f.sound(BITE);
            f.handler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START,f.player));f.expect(0);
            f.handler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,new EntityPlayerMP(ID)));f.expect(0);
            f.tick();f.expect(1);
        }});
        test("foreign_player_with_same_uuid_ignored",new Check(){public void run()throws Exception{
            Fixture f=new Fixture();f.sound(BITE);
            f.handler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,new EntityPlayerSP(ID)));
            f.expect(0);f.tick();f.expect(1);
        }});
        test("foreign_owner_hook_ignored",new Check(){public void run()throws Exception{
            Fixture f=new Fixture();f.player.field_71104_cf.owner=new EntityPlayerSP(ID);
            f.sound(BITE);f.tick(20);f.expect(0);
        }});
        test("dead_hook_ignored",new Check(){public void run()throws Exception{
            Fixture f=new Fixture();f.player.field_71104_cf.field_70128_L=true;
            f.sound(BITE);f.tick(20);f.expect(0);
        }});
        test("sound_with_no_hook_does_not_arm_a_future_hook",new Check(){public void run()throws Exception{
            Fixture f=new Fixture();f.player.field_71104_cf=null;f.sound(BITE);f.newHook();f.tick();f.expect(0);
        }});
        test("key_toggle_off_cancels_pending_recast",new Check(){public void run()throws Exception{
            Fixture f=new Fixture();f.reel();KeyBinds.autofish=new KeyBinding();KeyBinds.autofish.pressed=true;
            f.handler.onKeyInput(new InputEvent.KeyInputEvent());f.tick();
            require(!f.isEnabled(),"key did not toggle automation off");
            f.player.field_71104_cf=null;f.enabled(true);f.tick(30);f.expect(1);
        }});
        test("three_complete_fishing_cycles_remain_available",new Check(){public void run()throws Exception{
            Fixture f=new Fixture();
            for(int cycle=0;cycle<3;cycle++){
                if(cycle>0)f.newHook();
                f.sound(BITE);f.tick();f.expect(2*cycle+1);
                f.player.field_71104_cf=null;f.tick(10);f.expect(2*cycle+2);
            }
            f.checkPackets();
        }});
        test("reel_and_recast_create_no_worker_threads",new Check(){public void run()throws Exception{
            Fixture f=new Fixture();Set<Thread> before=Thread.getAllStackTraces().keySet();
            f.reel();f.player.field_71104_cf=null;f.tick(10);f.expect(2);
            for(Thread t:Thread.getAllStackTraces().keySet())
                require(before.contains(t),"automation spawned worker thread "+t.getName());
        }});
        test("low_durability_stops_before_reel",()->{
            Fixture f=new Fixture();f.player.held.maxDamage=100;f.player.held.damage=90;
            f.sound(BITE);f.tick();f.expect(0);require(!f.isEnabled(),"low durability did not disable fishing");
            require("low_durability".equals(status(f.handler)),"missing low durability status");
            f.tick(100);require(f.player.notices==1,"low durability notice repeated");
        });
        test("damage_during_recast_delay_cancels_recast",()->{
            Fixture f=new Fixture();f.reel();f.player.field_71104_cf=null;
            f.player.held.maxDamage=100;f.player.held.damage=95;f.tick(30);f.expect(1);
            require(!f.isEnabled(),"pending recast survived low durability");
        });
        test("reserve_boundary_allows_eleven_remaining",()->{
            Fixture f=new Fixture();f.player.held.maxDamage=100;f.player.held.damage=89;f.reel();
        });
        test("unbreakable_rod_is_not_stopped",()->{
            Fixture f=new Fixture();f.player.held.maxDamage=100;f.player.held.damage=99;f.player.held.unbreakable=true;f.reel();
        });
        test("status_tracks_bite_recast_and_manual_first_cast",()->{
            Fixture f=new Fixture();f.tick();require("waiting_bite".equals(status(f.handler)),"bite status");
            f.reel();require("recasting".equals(status(f.handler)),"recast status");
            f.player.field_71104_cf=null;f.tick(10);require("waiting_hook".equals(status(f.handler)),"new hook status");
        });
        test("disabled_guard_allows_low_durability",()->{
            Fixture f=new Fixture();option("protectRod",false);
            f.player.held.maxDamage=100;f.player.held.damage=99;f.reel();
        });
        test("custom_reserve_is_inclusive",()->{
            Fixture f=new Fixture();option("durabilityReserve",20);
            f.player.held.maxDamage=100;f.player.held.damage=80;f.sound(BITE);f.tick();f.expect(0);
            require(!f.isEnabled(),"custom reserve ignored");
        });
        test("undamageable_rod_bypasses_guard",()->{
            Fixture f=new Fixture();f.player.held.maxDamage=0;f.reel();
        });
        test("no_bobber_status_requires_manual_first_cast",()->{
            Fixture f=new Fixture();f.player.field_71104_cf=null;f.tick(30);f.expect(0);
            require("manual_cast".equals(status(f.handler)),"manual first cast status missing");
        });
        test("old_hook_timeout_status_requests_manual_handling",()->{
            Fixture f=new Fixture();f.reel();f.tick(65);f.expect(1);
            require("manual_reset".equals(status(f.handler)),"timeout must not advertise waiting for a new bite");
        });
        test("menu_tick_releases_session_without_a_player_tick",()->{
            Fixture f=new Fixture();f.reel();
            f.mc.field_71439_g=null;f.mc.field_71441_e=null;f.mc.connection=null;
            AutoFishHandler.class.getMethod("onClientTick",TickEvent.ClientTickEvent.class)
                    .invoke(f.handler,new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            for(String name:new String[]{"sessionPlayer","sessionWorld","sessionConnection","pendingBite","processedHook"}) {
                Field field=AutoFishHandler.class.getDeclaredField(name);
                field.setAccessible(true);
                require(field.get(f.handler)==null,"disconnected session retained "+name);
            }
            require(f.isEnabled(),"disconnect changed user's toggle");
            f.expect(1);
        });
        System.out.println("RESULT passed="+passed+" failed="+failed+" total="+(passed+failed));
        if(failed!=0)System.exit(1);
    }

    private static String status(AutoFishHandler handler)throws Exception {
        try{return (String)AutoFishHandler.class.getMethod("statusKey").invoke(handler);}
        catch(NoSuchMethodException e){throw new AssertionError("Fishing status is missing",e);}
    }

    private static final class Fixture {
        final Minecraft mc=Minecraft.func_71410_x();
        final EntityPlayerSP player=new EntityPlayerSP(ID);
        final NetHandlerPlayClient connection=new NetHandlerPlayClient();
        final AutoFishHandler handler=new AutoFishHandler();
        Fixture()throws Exception{
            try {option("protectRod",true);option("durabilityReserve",10);}catch(ClassNotFoundException oldBaseline){}
            mc.field_71439_g=player;mc.field_71441_e=new WorldClient();mc.connection=connection;
            player.field_70170_p=mc.field_71441_e;player.held=new ItemStack(new ItemFishingRod());
            newHook();enabled(true);
        }
        EntityFishHook newHook(){
            EntityFishHook hook=new EntityFishHook();
            hook.field_70165_t=8;hook.field_70163_u=64;hook.field_70161_v=8;
            hook.field_70181_x=-0.10;hook.inWater=true;hook.owner=player;
            player.field_71104_cf=hook;return hook;
        }
        void enabled(boolean value)throws Exception{
            Field f=AutoFishHandler.class.getDeclaredField("autofishenabled");
            f.setAccessible(true);f.setBoolean(handler,value);
        }
        boolean isEnabled()throws Exception{
            Field f=AutoFishHandler.class.getDeclaredField("autofishenabled");f.setAccessible(true);return f.getBoolean(handler);
        }
        int count(){return connection.packets.size();}
        void expect(int expected){require(count()==expected,"expected "+expected+" use packets, observed "+count());}
        void tick()throws Exception{handler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,player));}
        void tick(int n)throws Exception{for(int i=0;i<n;i++)tick();}
        void tickFor(EntityPlayer subject,int n)throws Exception{
            for(int i=0;i<n;i++)handler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,subject));
        }
        void sound(String name)throws Exception{soundAt(name,8,64,8);}
        void soundAt(String name,float x,float y,float z)throws Exception{
            Method method;
            try{method=AutoFishHandler.class.getMethod("onSound",PlaySoundEvent.class);}
            catch(NoSuchMethodException error){throw new AssertionError("Missing onSound(PlaySoundEvent); baseline uses motion-only detection");}
            try{method.invoke(handler,new PlaySoundEvent(null,new Cue(name,x,y,z)));}
            catch(InvocationTargetException error){
                Throwable cause=error.getCause();
                if(cause instanceof Exception)throw (Exception)cause;
                if(cause instanceof Error)throw (Error)cause;
                throw new AssertionError(cause);
            }
        }
        void reel()throws Exception{sound(BITE);tick();expect(1);}
        void checkPackets(){
            for(Packet<?> p:connection.packets){
                require(p instanceof CPacketPlayerTryUseItem,"unexpected packet");
                require(((CPacketPlayerTryUseItem)p).hand==EnumHand.MAIN_HAND,"wrong hand");
            }
        }
    }
    private static void option(String name,Object value)throws Exception {
        Class.forName("com.northwestwind.forgeautofish.AutoFishSettings").getField(name).set(null,value);
    }
    private static final class Cue implements ISound {
        private final ResourceLocation id;private final float x,y,z;
        Cue(String name,float x,float y,float z){id=new ResourceLocation(name);this.x=x;this.y=y;this.z=z;}
        public ResourceLocation func_147650_b(){return id;}
        public float func_147649_g(){return x;}
        public float func_147654_h(){return y;}
        public float func_147651_i(){return z;}
    }
    private static void test(String name,Check check){
        try{check.run();passed++;System.out.println("PASS "+name);}
        catch(Throwable error){failed++;System.out.println("FAIL "+name+" -> "+error.getClass().getName()+": "+error.getMessage());}
    }
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
