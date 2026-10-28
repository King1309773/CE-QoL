package net.minecraftforge.fml.common.gameevent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
public class TickEvent {
    public enum Type { WORLD, PLAYER, CLIENT, SERVER, RENDER }
    public enum Phase { START, END }
    public final Type type;
    public final Side side;
    public final Phase phase;
    public TickEvent(Type type, Side side, Phase phase){this.type=type;this.side=side;this.phase=phase;}
    public static class PlayerTickEvent extends TickEvent {
        public final EntityPlayer player;
        public PlayerTickEvent(Phase phase, EntityPlayer player){
            super(Type.PLAYER,player instanceof EntityPlayerMP?Side.SERVER:Side.CLIENT,phase);
            this.player=player;
        }
    }
}
