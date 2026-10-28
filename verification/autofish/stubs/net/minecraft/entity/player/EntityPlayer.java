package net.minecraft.entity.player;
import java.util.UUID;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
public class EntityPlayer {
    public World field_70170_p;
    public EntityFishHook field_71104_cf;
    public final UUID id;
    public ItemStack held=ItemStack.EMPTY;
    public int heldReads;
    public boolean field_70128_L;
    public boolean func_70089_S(){return !field_70128_L;}
    public EntityPlayer(UUID id){this.id=id;}
    public UUID func_110124_au(){return id;}
    public ItemStack func_184614_ca(){heldReads++;return held;}
}
