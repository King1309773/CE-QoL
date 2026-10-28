package net.minecraft.item;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
public class ItemStack {
    public static final ItemStack EMPTY=new ItemStack(null);
    public final Item item;
    public final AtomicInteger rightClicks=new AtomicInteger();
    public ItemStack(Item item){this.item=item;}
    public Item func_77973_b(){return item;}
    public boolean func_190926_b(){return item==null;}
    public ActionResult<ItemStack> func_77957_a(World world,EntityPlayer player,EnumHand hand){
        rightClicks.incrementAndGet();return new ActionResult<ItemStack>(this);
    }
}
