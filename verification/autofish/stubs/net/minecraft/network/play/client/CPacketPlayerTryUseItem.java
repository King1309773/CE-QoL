package net.minecraft.network.play.client;
import net.minecraft.network.Packet;
import net.minecraft.util.EnumHand;
public class CPacketPlayerTryUseItem implements Packet<Object> {
    public final EnumHand hand;
    public CPacketPlayerTryUseItem(EnumHand hand){this.hand=hand;}
}
