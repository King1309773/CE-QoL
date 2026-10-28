package dev.rlcraftceaddons.reforgingquality;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public final class ReforgingQuality {
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) MinecraftForge.EVENT_BUS.register(new QualityPanel());
    }
}
