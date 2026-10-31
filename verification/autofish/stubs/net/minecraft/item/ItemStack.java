package net.minecraft.item;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
public class ItemStack {
    public static final ItemStack EMPTY=new ItemStack(null);
    public final Item item;
    public int maxDamage=1000, damage=0;
    public boolean unbreakable;
    public boolean func_77984_f(){return item!=null && maxDamage>0 && !unbreakable;}
    public int func_77958_k(){return maxDamage;}
    public int func_77952_i(){return damage;}
    public final AtomicInteger rightClicks=new AtomicInteger();
    public ItemStack(Item item){this.item=item;}
    public Item func_77973_b(){return item;}
    public boolean func_190926_b(){return item==null;}
    public ActionResult<ItemStack> func_77957_a(World world,EntityPlayer player,EnumHand hand){
        rightClicks.incrementAndGet();return new ActionResult<ItemStack>(this);
    }
}
