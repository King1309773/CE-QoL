package dev.rlcraftceaddons.reforgingquality;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import static org.junit.Assert.*;

public class SlotSnapshotCacheTest {
    @Test public void unchangedSlotsReuseTextButNbtAndContextChangesInvalidate() throws Exception {
        Bootstrap.register();
        Object cache;
        try { cache=Class.forName("dev.rlcraftceaddons.reforgingquality.SlotSnapshotCache").newInstance(); }
        catch(ClassNotFoundException e){throw new AssertionError("Slot snapshot cache missing",e);}
        java.lang.reflect.Method get=cache.getClass().getMethod("get",ItemStack.class,Object.class,Supplier.class);
        AtomicInteger reads=new AtomicInteger();Supplier<Integer> source=reads::incrementAndGet;
        ItemStack stack=new ItemStack(Items.IRON_SWORD);stack.setTagCompound(new NBTTagCompound());
        assertEquals(1,get.invoke(cache,stack,"context",source));
        assertEquals(1,get.invoke(cache,stack.copy(),"context",source));
        stack.getTagCompound().setString("quality","changed");
        assertEquals(2,get.invoke(cache,stack,"context",source));
        assertEquals(3,get.invoke(cache,stack,"language-or-layout",source));
        cache.getClass().getMethod("clear").invoke(cache);
        assertEquals(4,get.invoke(cache,stack,"language-or-layout",source));
    }
}
