package dev.rlcraftceaddons.thirstpreview;

import java.io.File;
import java.io.IOException;
import dev.rlcraftceaddons.ceqol.config.ConfigFileStore;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ThirstPreviewConfig {
    private static final String TOOLTIP_COMMENT =
            "Show per-use hydration as waterdrop icons in item tooltips (inventory, containers and JEI).";
    private static final String HUD_COMMENT =
            "Preview held hydration on the native thirst HUD or supported GraveStone water bar.";
    private static final String SHIFT_COMMENT =
            "Show saturation and dehydration probability with Shift. Hydration remains icons only. False: always show details.";
    public static boolean showTooltip = true;
    public static boolean showHud = true;
    public static boolean showDetailsWithShift = true;
    private static File configFile;

    private ThirstPreviewConfig() { }

    public static void load(File file) {
        Configuration config = new Configuration(file);
        config.load();
        configFile = file;
        showTooltip = config.getBoolean("showTooltip", "client", true, TOOLTIP_COMMENT);
        showHud = config.getBoolean("showHud", "client", true, HUD_COMMENT);
        showDetailsWithShift = config.getBoolean("showDetailsWithShift", "client", true, SHIFT_COMMENT);
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        try { apply(showTooltip,showHud,showDetailsWithShift); }
        catch(IOException failure) {throw new IllegalStateException("Hydration settings were not saved",failure);}
    }

    public static void apply(boolean tooltip,boolean hud,boolean details)throws IOException {
        if(configFile!=null)ConfigFileStore.write(configFile,config->{
            setBoolean(config,"showTooltip",tooltip,TOOLTIP_COMMENT);
            setBoolean(config,"showHud",hud,HUD_COMMENT);
            setBoolean(config,"showDetailsWithShift",details,SHIFT_COMMENT);
        },config->config.getBoolean("showTooltip","client",true,TOOLTIP_COMMENT)==tooltip
                && config.getBoolean("showHud","client",true,HUD_COMMENT)==hud
                && config.getBoolean("showDetailsWithShift","client",true,SHIFT_COMMENT)==details);
        showTooltip=tooltip;showHud=hud;showDetailsWithShift=details;
    }

    private static void setBoolean(Configuration config, String key, boolean value, String comment) {
        Property property = config.get("client", key, true, comment + " [default: true]");
        // Forge marks a property changed even when set to its current value.
        if (property.getBoolean() != value) property.set(value);
    }
}
