package dev.rlcraftceaddons.reforgingquality;

import com.tmtravlr.qualitytools.reforging.GuiReforgingStation;
import com.tmtravlr.qualitytools.reforging.TileEntityReforgingStation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class QualityPanel implements net.minecraft.client.resources.IResourceManagerReloadListener {
    private final SlotSnapshotCache<NameLayout> textCache=new SlotSnapshotCache<>();
    @Override public void onResourceManagerReload(net.minecraft.client.resources.IResourceManager manager) {textCache.clear();}
    @SubscribeEvent public void open(GuiOpenEvent event) {
        textCache.clear();
        if(event.getGui()!=null && event.getGui().getClass()==GuiReforgingStation.class) {
            GuiReforgingStation original=(GuiReforgingStation)event.getGui();
            if(original.inventorySlots.getSlot(0).inventory instanceof TileEntityReforgingStation) {
                event.setGui(new QualityStationScreen(Minecraft.getMinecraft().player.inventory,
                        (TileEntityReforgingStation)original.inventorySlots.getSlot(0).inventory,
                        original.inventorySlots));
            }
        }
    }
    private PanelLayout layout(GuiContainer gui) {
        return PanelLayout.create(gui.width, gui.height, gui.getGuiLeft(), gui.getGuiTop(),
                gui.getXSize(), gui.getYSize()).withFooter(((QualityStationScreen)gui).getFooterHeight());
    }
    private QualityInfo read(GuiContainer gui) {
        return QualityInfo.read(gui.inventorySlots.getSlot(0).getStack());
    }
    private NameLayout effectLayout(FontRenderer font, QualityInfo info, PanelLayout panel) {
        return NameLayout.fit(panel.width-10, panel.textHeight(), font.FONT_HEIGHT, width -> {
            List<String> wrapped=new ArrayList<>();
            for(String line:info.lines) wrapped.addAll(font.listFormattedStringToWidth(line,width));
            return wrapped;
        });
    }
    @SubscribeEvent public void foreground(GuiContainerEvent.DrawForeground event) {
        GuiContainer gui = event.getGuiContainer();
        if (!(gui instanceof QualityStationScreen)) return;
        PanelLayout p = layout(gui);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        Minecraft mc=Minecraft.getMinecraft();
        NameLayout name=textCache.get(gui.inventorySlots.getSlot(0).getStack(),
                Arrays.asList(mc.player,mc.world,mc.gameSettings.language,font,p.width,p.textHeight(),font.FONT_HEIGHT),
                ()->effectLayout(font,read(gui),p));
        GlStateManager.pushMatrix();
        GlStateManager.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        try {
            Gui.drawRect(p.x, p.y, p.x+p.width, p.y+p.height-p.footerHeight, 0xF0181620);
            Gui.drawRect(p.x, p.y, p.x+p.width, p.y+1, 0xFF9A7EBC);
            GlStateManager.pushMatrix();
            try {
                GlStateManager.translate(p.x+5, p.y+5, 0);
                GlStateManager.scale(name.scale, name.scale, 1F);
                for (int i=0; i<name.lines.size(); i++) {
                    font.drawString(name.lines.get(i), 0, i*name.lineHeight, 0xEEEEEE);
                }
            } finally { GlStateManager.popMatrix(); }
        } finally {
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.color(1,1,1,1);
            GlStateManager.popMatrix();
        }
    }
}
