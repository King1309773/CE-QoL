package dev.rlcraftceaddons.ceqol.client;

import dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig;

/** One screen's working copy. Discarding it never changes live settings or disk. */
public final class ThirstSettingsDraft {
    private boolean tooltip = ThirstPreviewConfig.showTooltip;
    private boolean hud = ThirstPreviewConfig.showHud;
    private boolean details = ThirstPreviewConfig.showDetailsWithShift;

    public boolean showTooltip() { return tooltip; }
    public boolean showHud() { return hud; }
    public boolean showDetailsWithShift() { return details; }
    public void toggleTooltip() { tooltip = !tooltip; }
    public void toggleHud() { hud = !hud; }
    public void toggleDetails() { details = !details; }

    public void apply() throws java.io.IOException {
        if (tooltip == ThirstPreviewConfig.showTooltip && hud == ThirstPreviewConfig.showHud
                && details == ThirstPreviewConfig.showDetailsWithShift) return;
        ThirstPreviewConfig.apply(tooltip,hud,details);
    }
}
