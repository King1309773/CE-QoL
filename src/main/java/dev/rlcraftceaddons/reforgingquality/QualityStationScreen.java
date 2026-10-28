package dev.rlcraftceaddons.reforgingquality;

import com.tmtravlr.qualitytools.reforging.GuiButtonReforgingStation;
import com.tmtravlr.qualitytools.reforging.GuiReforgingStation;
import com.tmtravlr.qualitytools.reforging.TileEntityReforgingStation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

/** Rearranges only the client view, retaining the original container and reforge packet handler. */
public final class QualityStationScreen extends GuiReforgingStation {
    private static final ResourceLocation TEXTURE=new ResourceLocation("qualitytools","textures/gui/container/reforging_station.png");
    private final TileEntityReforgingStation station;
    private final InventoryPlayer playerInventory;
    private GuiButtonReforgingStation button;
    private int footerHeight;
    public int getFooterHeight() { return footerHeight; }

    QualityStationScreen(InventoryPlayer player, TileEntityReforgingStation station, Container original) {
        super(player,station);
        this.station=station;
        this.playerInventory=player;
        this.inventorySlots=original;
    }
    @Override public void initGui() {
        ySize=PanelLayout.stationHeight(height);
        // The pinned Quality Tools binary exposes its override under the runtime SRG name.
        super.func_73866_w_();
        // Keep buttons injected into the parent's initGui (including DebrisClient's real auto button).
        buttonList.removeIf(candidate -> candidate instanceof GuiButtonReforgingStation);
        button=new GuiButtonReforgingStation(0,14,40);
        buttonList.add(0,button);
        button.updatePos(guiLeft,guiTop);
        button.enabled=station.func_174887_a_(0)>0;
        for(int i=0;i<inventorySlots.inventorySlots.size();i++) {
            Slot slot=inventorySlots.getSlot(i);
            if(i<2) { slot.xPos=14;slot.yPos=i==0?20:60; }
            else {
                int index=i-2;
                slot.xPos=8+index%9*18;
                slot.yPos=index<27?ySize-82+index/9*18:ySize-24;
            }
        }
        positionExtraButtons();
    }
    private void positionExtraButtons() {
        footerHeight=0;
        int leftY=84;
        int right=guiLeft+xSize-12, cursor=right;
        int bottom=guiTop+ySize-104, rowHeight=0;
        for(GuiButton extra:buttonList) {
            if(extra==button)continue;
            if(extra.width<=30 && leftY+extra.height<=ySize-98) {
                extra.x=guiLeft+8;
                extra.y=guiTop+leftY;
                leftY+=extra.height+4;
            } else {
                extra.width=Math.min(extra.width,118);
                if(cursor-extra.width<guiLeft+46 && rowHeight>0) {
                    bottom-=rowHeight+4;cursor=right;rowHeight=0;
                }
                extra.x=cursor-extra.width;
                extra.y=bottom-extra.height;
                cursor=extra.x-4;
                rowHeight=Math.max(rowHeight,extra.height);
                footerHeight=Math.max(footerHeight,guiTop+ySize-100-extra.y+4);
            }
        }
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX,int mouseY) {
        String title=station.getDisplayName().getUnformattedText();
        fontRenderer.drawString(title,(xSize-fontRenderer.getStringWidth(title))/2,6,0x404040);
        fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(),
                ySize<190?42:8,ySize-94,0x404040);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks,int mouseX,int mouseY) {
        int x=guiLeft,y=guiTop;
        Gui.drawRect(x,y,x+xSize,y+ySize,0xFF161616);
        Gui.drawRect(x+1,y+1,x+xSize-1,y+ySize-1,0xFF555555);
        Gui.drawRect(x+2,y+2,x+xSize-2,y+ySize-2,0xFFC6C6C6);
        Gui.drawRect(x+2,y+2,x+xSize-2,y+3,0xFFFFFFFF);
        Gui.drawRect(x+2,y+2,x+3,y+ySize-2,0xFFFFFFFF);
        GlStateManager.color(1,1,1,1);
        mc.getTextureManager().bindTexture(TEXTURE);
        for(Slot slot:inventorySlots.inventorySlots) {
            drawTexturedModalRect(x+slot.xPos-1,y+slot.yPos-1,7,83,18,18);
        }
        button.enabled=station.func_174887_a_(0)>0;
        button.updatePos(x,y);
    }
}
