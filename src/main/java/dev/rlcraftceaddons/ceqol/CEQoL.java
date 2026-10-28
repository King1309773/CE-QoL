package dev.rlcraftceaddons.ceqol;

import com.northwestwind.forgeautofish.AutoFishModule;
import dev.rlcraftceaddons.reforgingquality.ReforgingQuality;
import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import dev.rlcraftceaddons.thirstpreview.ThirstPreview;
import dev.rlcraftceaddons.visualcontrols.VisualControls;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/** One Forge identity; the feature packages retain their existing resources and settings. */
@Mod(modid = CEQoL.MODID, name = "CE QoL", version = "1.1.0",
        acceptedMinecraftVersions = "[1.12.2]", clientSideOnly = true,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:simpledifficulty@[0.3.9,0.4);required-after:qualitytools@[1.0.7_for_1.12.2];after:appleskin;after:controlling",
        guiFactory = "dev.rlcraftceaddons.ceqol.client.CEQoLGuiFactory")
public final class CEQoL {
    public static final String MODID = "ceqol";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) new ThirstPreview().preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (!event.getSide().isClient()) return;
        new ReforgingQuality().init(event);
        new VisualControls().init(event);
        new SDTemperatureHud().initialize(event);
        AutoFishModule.initialize();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Keep thirst preview registration after other HUD owners' initialization.
        if (event.getSide().isClient()) new ThirstPreview().postInit(event);
    }
}
