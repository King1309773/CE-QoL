package dev.rlcraftceaddons.visualcontrols;

import dev.rlcraftceaddons.visualcontrols.client.ControlsEntry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public final class VisualControls {
    public static final String MODID = "visualcontrols";
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) MinecraftForge.EVENT_BUS.register(new ControlsEntry());
    }
}
