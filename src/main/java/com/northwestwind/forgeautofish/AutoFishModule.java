package com.northwestwind.forgeautofish;

import com.northwestwind.forgeautofish.handler.AutoFishHandler;
import com.northwestwind.forgeautofish.keybind.KeyBinds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AutoFishModule {
    private static boolean initialized;

    private AutoFishModule() { }

    public static synchronized void initialize() {
        if (initialized) return;
        KeyBinds.register();
        MinecraftForge.EVENT_BUS.register(new AutoFishHandler());
        initialized = true;
    }
}
