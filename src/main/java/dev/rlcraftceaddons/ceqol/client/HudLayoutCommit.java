package dev.rlcraftceaddons.ceqol.client;

import com.google.gson.JsonObject;
import dev.rlcraftceaddons.ceqol.presets.PresetService;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.*;
import java.io.IOException;
import java.util.*;

/** Merge only edited layouts into values captured at Save, preserving unrelated current preferences. */
public final class HudLayoutCommit {
    private HudLayoutCommit(){}
    public static JsonObject document(PresetService service,Map<HudPanel,HudLayoutState> layouts,
            Set<HudPanel> changed,FishingHudLayout fishing)throws IOException {
        if(changed.isEmpty() && fishing==null)return null;
        JsonObject doc=service.capture("HUD layout",changed.isEmpty()?"fishing":"temperature");
        JsonObject settings=doc.getAsJsonObject("settings");
        if(!changed.isEmpty()) {
            JsonObject temperature=settings.getAsJsonObject("temperature");
            boolean migrate=temperature.get("layout.version").getAsInt()<1;
            for(HudPanel panel:HudPanel.values())if(migrate || changed.contains(panel)) {
                HudLayoutState state=layouts.get(panel);
                if(state==null)throw new IOException("Both temperature layouts are required for legacy migration");
                String prefix=panel==HudPanel.COMPACT?"layout.compact":"layout.analysis";
                temperature.addProperty(prefix+"X",state.getNormalizedX());
                temperature.addProperty(prefix+"Y",state.getNormalizedY());
                temperature.addProperty(prefix+"Scale",state.getScale());
            }
            temperature.addProperty("layout.version",1);
        }
        if(fishing!=null) {
            JsonObject values=service.capture("HUD layout","fishing").getAsJsonObject("settings").getAsJsonObject("fishing");
            values.addProperty("hudX",fishing.x());values.addProperty("hudBottom",fishing.bottom());
            settings.add("fishing",values);
        }
        return doc;
    }
}
