package dev.rlcraftceaddons.ceqol.presets;

import java.lang.reflect.Method;
import net.minecraftforge.common.config.*;

/** Refresh the pinned Forge cache so later normal settings saves preserve the preset. */
public final class TemperaturePresetBridge implements Runnable {
    @Override public void run() {
        reloadOnly();ConfigManager.sync("ceqol",Config.Type.INSTANCE);
    }
    public void reloadOnly() {
        try {
            Method getter=ConfigManager.class.getDeclaredMethod("getConfiguration",String.class,String.class);getter.setAccessible(true);
            Configuration cached=(Configuration)getter.invoke(null,"ceqol","sdtemperaturehud");
            if(cached==null)throw new IllegalStateException("Temperature configuration is not registered");
            cached.load();
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Temperature configuration cache could not be refreshed",e);}
    }
}
