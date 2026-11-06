package dev.rlcraftceaddons.ceqol.compat.drops;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.entity.IEntityDefinition;
import crafttweaker.api.entity.IEntityDrop;
import crafttweaker.api.minecraft.CraftTweakerMC;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import jeresources.api.conditionals.Conditional;
import jeresources.api.conditionals.LightLevel;
import jeresources.api.drop.LootDrop;
import jeresources.compatibility.JERAPI;
import jeresources.entry.MobEntry;
import jeresources.jei.JEIConfig;
import jeresources.jei.mob.MobWrapper;
import mezz.jei.api.IRecipeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Reads applied additive CraftTweaker rules. Does not run scripts or change drops. */
final class CraftTweakerDrops {
    private static final Logger LOG = LogManager.getLogger("CE QoL Scripted Drops");
    private static final String TEXT = "ceqol.scripted_drops.";

    static void addTo(IRecipeRegistry registry) {
        if (CraftTweakerAPI.game == null || registry.getRecipeCategory(JEIConfig.MOB) == null) return;
        World previewWorld = JERAPI.getInstance().getWorld();
        int count = 0;
        for (IEntityDefinition definition : CraftTweakerAPI.game.getEntities()) {
            if (definition.getDrops().isEmpty()) continue;
            String id = definition.getId();
            try {
                ResourceLocation key = new ResourceLocation(id);
                Entity preview = EntityList.createEntityByIDFromName(key, previewWorld);
                if (!(preview instanceof EntityLivingBase)) {
                    LOG.debug("No living preview entity for {}", id);
                    continue;
                }
                for (IEntityDrop rule : definition.getDrops()) {
                    LootDrop drop = convert(rule);
                    if (drop == null) continue;
                    registry.addRecipe(new ScriptedMobRecipe((EntityLivingBase) preview, key, drop), JEIConfig.MOB);
                    count++;
                }
            } catch (RuntimeException | LinkageError failure) {
                LOG.warn("Could not index scripted drops for {}", id, failure);
            }
        }
        LOG.info("Indexed {} CraftTweaker mob drop rules for HEI/JEI", count);
    }

    private static LootDrop convert(IEntityDrop rule) {
        if (rule.getItemStack() == null) return null;
        ItemStack stack = CraftTweakerMC.getItemStack(rule.getItemStack()).copy();
        int min = rule.getMin();
        int max = rule.getMax();
        if (min == 0 && max == 0) min = max = rule.getItemStack().getAmount();
        float chance = rule.getChance();
        if (stack.isEmpty() || min < 0 || max < min || max == 0
                || !Float.isFinite(chance) || chance < 0 || chance > 1) return null;
        // CraftTweaker uses zero as an unconditional roll, just like one.
        if (chance == 0) chance = 1;
        stack.setCount(1);
        LootDrop drop = new LootDrop(stack, min, max, chance, 0);
        if (rule.isPlayerOnly()) drop.addConditional(Conditional.playerKill);
        return drop;
    }

    private static final class ScriptedMobRecipe extends MobWrapper {
        private static final MathContext CHANCE_PRECISION = new MathContext(6);
        private final LootDrop drop;
        private final ItemStack egg;
        private final String percent;

        ScriptedMobRecipe(EntityLivingBase mob, ResourceLocation key, LootDrop drop) {
            super(new MobEntry(mob, LightLevel.any, 0, drop));
            this.drop = drop;
            // CraftTweaker parses percentages as floats, so 5% can arrive as 0.049999997.
            percent = new BigDecimal(Float.toString(drop.chance)).movePointRight(2)
                    .round(CHANCE_PRECISION).stripTrailingZeros().toPlainString();
            if (EntityList.ENTITY_EGGS.containsKey(key)) {
                egg = new ItemStack(Items.SPAWN_EGG);
                ItemMonsterPlacer.applyEntityIdToItemStack(egg, key);
            } else {
                egg = ItemStack.EMPTY;
            }
        }

        @Override
        public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            mc.fontRenderer.drawSplitString(getMob().getDisplayName().getUnformattedText(),
                    7, 3, recipeWidth - 14, 0x404040);
            mc.fontRenderer.drawString(I18n.format(TEXT + "source"), 7, 25, 0x404040);
            if (!egg.isEmpty()) mc.getRenderItem().renderItemIntoGUI(egg, 29, 70);
        }

        @Override
        public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
            String quantity = drop.minDrop == drop.maxDrop ? Integer.toString(drop.minDrop)
                    : drop.minDrop + "–" + drop.maxDrop;
            tooltip.add(I18n.format(TEXT + "count", quantity));
            tooltip.add(I18n.format(TEXT + (drop.minDrop == 0 ? "roll_chance" : "chance"), percent));
            tooltip.addAll(drop.getTooltipText());
            tooltip.add(I18n.format(TEXT + "source"));
        }
    }
}
