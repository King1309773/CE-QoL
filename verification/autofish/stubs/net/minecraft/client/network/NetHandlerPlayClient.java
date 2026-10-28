package net.minecraft.client.network;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.network.Packet;
public class NetHandlerPlayClient {
    public final List<Packet<?>> packets=Collections.synchronizedList(new ArrayList<Packet<?>>());
    public final List<Long> sentAt=Collections.synchronizedList(new ArrayList<Long>());
    public final CountDownLatch firstTwo=new CountDownLatch(2);
    public void func_147297_a(Packet<?> packet){
        packets.add(packet);sentAt.add(System.nanoTime());firstTwo.countDown();
    }
}
