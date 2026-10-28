package dev.rlcraftceaddons.thirstpreview;

import dev.rlcraftceaddons.thirstpreview.client.ClientEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public final class ThirstPreview {
    public static final String MODID = "thirstpreview";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) {
            ThirstPreviewConfig.load(new java.io.File(event.getModConfigurationDirectory(), "thirstpreview.cfg"));
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (event.getSide().isClient()) {
            // Register after every mod's init, so our LOWEST Post(ALL) follows the bar owner.
            MinecraftForge.EVENT_BUS.register(new ClientEvents());
        }
    }
}
