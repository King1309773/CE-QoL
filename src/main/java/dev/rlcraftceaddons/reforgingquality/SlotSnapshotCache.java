package dev.rlcraftceaddons.reforgingquality;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.item.ItemStack;

/** One slot, one context; copying the key detects in-place NBT mutation. */
public final class SlotSnapshotCache<T> {
    private ItemStack snapshot;
    private Object context;
    private T value;
    public T get(ItemStack stack,Object context,Supplier<T> source) {
        if(snapshot==null || !ItemStack.areItemStacksEqual(snapshot,stack) || !Objects.equals(this.context,context)) {
            T next=source.get();
            snapshot=stack.copy();this.context=context;value=next;
        }
        return value;
    }
    public void clear() {snapshot=null;context=null;value=null;}
}
