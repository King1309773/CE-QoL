package net.minecraftforge.client.event.sound;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
public class PlaySoundEvent {
    private final ISound sound;
    private ISound result;
    public PlaySoundEvent(SoundManager manager,ISound sound){this.sound=sound;this.result=sound;}
    public ISound getSound(){return sound;}
    public String getName(){return sound.func_147650_b().func_110623_a();}
    public ISound getResultSound(){return result;}
    public void setResultSound(ISound sound){result=sound;}
}
