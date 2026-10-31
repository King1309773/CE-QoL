package net.minecraft.client.entity;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
public class EntityPlayerSP extends EntityPlayer {
    public EntityPlayerSP(UUID id){super(id);}
    public int notices;
    public void func_146105_b(ITextComponent text,boolean actionBar){notices++;}
}
