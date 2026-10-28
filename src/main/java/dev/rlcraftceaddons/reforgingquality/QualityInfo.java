package dev.rlcraftceaddons.reforgingquality;

import com.tmtravlr.qualitytools.ClientEventHandler;
import com.tmtravlr.qualitytools.QualityToolsHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/** Read-only snapshot of the live slot; the original mod formats all attribute operations. */
public final class QualityInfo {
    public final String name;
    public final List<String> lines;
    private QualityInfo(String name, List<String> lines) { this.name=name; this.lines=lines; }

    public static QualityInfo read(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        String name;
        if (stack.isEmpty()) {
            name = I18n.format("reforgingquality.empty");
        } else if (!QualityToolsHelper.hasQualityTag(stack)) {
            name = I18n.format("reforgingquality.unassigned");
        } else {
            NBTTagCompound tag = QualityToolsHelper.getQualityTag(stack);
            if (tag.isEmpty()) {
                name = I18n.format("reforgingquality.normal");
            } else {
                TextFormatting color = TextFormatting.getValueByName(tag.getString("Color"));
                String key = tag.getString("Name");
                name = (color == null ? "" : color.toString()) + (key.isEmpty()
                        ? I18n.format("reforgingquality.unknown") : I18n.format(key));
                ClientEventHandler.onItemTooltip(new ItemTooltipEvent(stack,
                        Minecraft.getMinecraft().player, lines, ITooltipFlag.TooltipFlags.NORMAL));
                lines.removeIf(String::isEmpty);
            }
        }
        if (lines.isEmpty()) lines.add(name);
        return new QualityInfo(name, lines);
    }
}
