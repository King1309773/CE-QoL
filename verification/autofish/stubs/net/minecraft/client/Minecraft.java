package net.minecraft.client;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
public class Minecraft {
    private static final Minecraft INSTANCE=new Minecraft();
    public static int instanceReads;
    public EntityPlayerSP field_71439_g;
    public WorldClient field_71441_e;
    public NetHandlerPlayClient connection;
    public static Minecraft func_71410_x(){instanceReads++;return INSTANCE;}
    public NetHandlerPlayClient func_147114_u(){return connection;}
}
