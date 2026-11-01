package dev.rlcraftceaddons.ceqol.client;

import dev.rlcraftceaddons.ceqol.presets.PresetService;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.editor.*;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.*;
import dev.rlcraftceaddons.sdtemperaturehud.config.LegacyLayoutMigration;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.*;

/** Draft only: mutations do not reach config fields or disk until save succeeds. */
public final class HudEditorSession {
    private HudEditorModel temperature;
    private final HudLayoutState initialCompact,initialAnalysis;
    private final FishingHudLayout initialFishing;
    private FishingHudLayout fishing;
    private boolean closed;
    public HudEditorSession(HudSettings base,FishingHudLayout fishing,int w,int h,int cw,int ch,int aw,int ah) {
        HudLayoutState compact=base.getCompactLayout(),analysis=base.getAnalysisLayout();
        if(base.isLegacyLayoutPending()) {
            LegacyLayoutMigration.PanelLayouts legacy=LegacyLayoutMigration.forPanels(base.getXOffset(),base.getYOffset(),
                    base.getScale(),compact.getScale(),analysis.getScale(),w,h,cw,ch,aw,ah);
            compact=legacy.compact();analysis=legacy.analysis();
        }
        initialCompact=compact;initialAnalysis=analysis;initialFishing=fishing;this.fishing=fishing;
        HudLayoutDefaults defaults=HudLayoutDefaults.forScreen(w,h,cw,ch,aw,ah);
        temperature=new HudEditorModel(compact,analysis,defaults.compact(),defaults.analysis());
    }
    public HudEditorModel temperature(){return temperature;}
    public FishingHudLayout fishing(){return fishing;}
    public void fishing(FishingHudLayout layout){if(!closed)fishing=Objects.requireNonNull(layout);}
    public boolean isClosed(){return closed;}
    public void cancel(){closed=true;temperature.endDrag();}
    public void reset(HudPanel panel,int w,int h,int cw,int ch,int aw,int ah) {
        if(closed)return;
        HudLayoutDefaults defaults=HudLayoutDefaults.forScreen(w,h,cw,ch,aw,ah);
        temperature=new HudEditorModel(temperature.state(HudPanel.COMPACT),temperature.state(HudPanel.ANALYSIS),
                defaults.compact(),defaults.analysis());
        temperature.select(panel);temperature.resetSelected(w,h,cw,ch,aw,ah);
    }
    public void save(PresetService service,int w,int h,int cw,int ch,int aw,int ah)throws IOException {
        if(closed)return;
        Set<HudPanel> changed=EnumSet.noneOf(HudPanel.class);
        if(!initialCompact.equals(temperature.state(HudPanel.COMPACT)))changed.add(HudPanel.COMPACT);
        if(!initialAnalysis.equals(temperature.state(HudPanel.ANALYSIS)))changed.add(HudPanel.ANALYSIS);
        JsonObject doc=HudLayoutCommit.document(service,temperature.validatedStates(w,h,cw,ch,aw,ah),changed,
                initialFishing.equals(fishing)?null:fishing);
        if(doc!=null)service.apply(doc);
        cancel();
    }
}
