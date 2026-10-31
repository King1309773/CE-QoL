package dev.rlcraftceaddons.reforgingquality;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public final class ReforgingQuality {
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            QualityPanel panel=new QualityPanel();
            MinecraftForge.EVENT_BUS.register(panel);
            net.minecraft.client.resources.IResourceManager resources=net.minecraft.client.Minecraft.getMinecraft().getResourceManager();
            if(resources instanceof net.minecraft.client.resources.IReloadableResourceManager)
                ((net.minecraft.client.resources.IReloadableResourceManager)resources).registerReloadListener(panel);
        }
    }
}
