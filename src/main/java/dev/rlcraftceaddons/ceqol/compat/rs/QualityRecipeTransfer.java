package dev.rlcraftceaddons.ceqol.compat.rs;

import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.grid.GridType;
import com.raoulvdberge.refinedstorage.api.network.grid.IGridNetworkAware;
import com.raoulvdberge.refinedstorage.api.util.Action;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.api.util.IStackList;
import com.raoulvdberge.refinedstorage.api.util.StackListEntry;
import com.raoulvdberge.refinedstorage.api.util.StackListResult;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public final class QualityRecipeTransfer {
    private static final String QUALITY = "Quality";
    private static final int EXACT = IComparer.COMPARE_NBT | IComparer.COMPARE_DAMAGE;

    private QualityRecipeTransfer() {
    }

    // Called only after RS has checked permissions and completed its original transfer.
    public static void fillMissing(IGridNetworkAware grid, EntityPlayer player, ItemStack[][] recipe) {
        if (grid.getGridType() != GridType.CRAFTING) return;

        InventoryCrafting matrix = grid.getCraftingMatrix();
        InventoryCrafting preview = null;
        INetwork network = grid.getNetwork();
        for (int slot = 0; slot < matrix.getSizeInventory(); slot++) {
            ItemStack[] samples = recipe[slot];
            if (!matrix.getStackInSlot(slot).isEmpty() || samples == null || samples.length == 0) {
                continue;
            }

            if (preview == null) preview = previewOf(matrix, recipe);
            ItemStack replacement = fromNetwork(network, player, preview, slot, samples);
            if (replacement.isEmpty()) {
                replacement = fromInventory(player, network.world(), preview, slot, samples);
            }
            if (!replacement.isEmpty()) {
                matrix.setInventorySlotContents(slot, replacement);
                preview.setInventorySlotContents(slot, replacement.copy());
            }
        }
    }

    private static InventoryCrafting previewOf(InventoryCrafting matrix, ItemStack[][] recipe) {
        InventoryCrafting preview = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer player) {
                return false;
            }
        }, matrix.getWidth(), matrix.getHeight());
        for (int slot = 0; slot < matrix.getSizeInventory(); slot++) {
            ItemStack stack = matrix.getStackInSlot(slot);
            ItemStack[] samples = recipe[slot];
            if (stack.isEmpty() && samples != null && samples.length != 0) {
                stack = samples[0];
            }
            preview.setInventorySlotContents(slot, stack.copy());
        }
        return preview;
    }

    private static ItemStack fromNetwork(INetwork network, EntityPlayer player, InventoryCrafting preview,
                                         int slot, ItemStack[] samples) {
        IStackList<ItemStack> storage = network.getItemStorageCache().getList();
        if (!containsIngredientItem(storage, samples)) return ItemStack.EMPTY;
        World world = network.world();
        for (StackListEntry<ItemStack> entry : storage.getStacks()) {
            ItemStack candidate = entry.getStack();
            if (entry.getCount() <= 0 || !matches(preview, slot, samples, candidate, world)) {
                continue;
            }

            StackListResult<ItemStack> extracted = network.extractItem(candidate, 1L, EXACT, Action.PERFORM);
            if (extracted != null) {
                ItemStack stack = extracted.getFixedStack();
                network.getItemStorageTracker().changed(player, stack.copy());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean containsIngredientItem(IStackList<ItemStack> storage, ItemStack[] samples) {
        // RS can check its item index without matching damage or NBT. This only rejects
        // a guaranteed miss; the normal candidate and recipe checks still run on hits.
        for (ItemStack sample : samples) {
            if (!sample.isEmpty() && storage.getEntry(sample, 0) != null) return true;
        }
        return false;
    }

    private static ItemStack fromInventory(EntityPlayer player, World world, InventoryCrafting preview,
                                           int slot, ItemStack[] samples) {
        for (int index = 0; index < player.inventory.getSizeInventory(); index++) {
            ItemStack candidate = player.inventory.getStackInSlot(index);
            if (matches(preview, slot, samples, candidate, world)) {
                return player.inventory.decrStackSize(index, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean matches(InventoryCrafting preview, int slot, ItemStack[] samples,
                                   ItemStack candidate, World world) {
        NBTTagCompound tag = candidate.getTagCompound();
        if (candidate.isEmpty() || tag == null || !tag.hasKey(QUALITY, 10)) return false;

        ItemStack normalized = null;
        for (ItemStack sample : samples) {
            if (sample.isEmpty() || sample.getItem() != candidate.getItem()) continue;

            ItemStack comparisonSample = sample;
            NBTTagCompound sampleTag = sample.getTagCompound();
            if (sampleTag != null && sampleTag.hasKey(QUALITY)) {
                if (!sampleTag.hasKey(QUALITY, 10)
                        || !sampleTag.getCompoundTag(QUALITY).isEmpty()) {
                    continue;
                }
                comparisonSample = withoutQuality(sample);
            }
            if (normalized == null) normalized = withoutQuality(candidate);

            int flags = IComparer.COMPARE_NBT;
            if (!sample.getItem().isDamageable()) flags |= IComparer.COMPARE_DAMAGE;
            if (!API.instance().getComparer().isEqual(comparisonSample, normalized, flags)) continue;

            // Fill unavailable slots in the preview with JEI samples. Verify the real
            // candidate selects the same recipe before taking anything from storage.
            ItemStack previous = preview.getStackInSlot(slot);
            try {
                preview.setInventorySlotContents(slot, ItemHandlerHelper.copyStackWithSize(sample, 1));
                IRecipe expected = CraftingManager.findMatchingRecipe(preview, world);
                if (expected == null) continue;
                preview.setInventorySlotContents(slot, ItemHandlerHelper.copyStackWithSize(candidate, 1));
                if (CraftingManager.findMatchingRecipe(preview, world) == expected) return true;
            } finally {
                preview.setInventorySlotContents(slot, previous);
            }
        }
        return false;
    }

    private static ItemStack withoutQuality(ItemStack stack) {
        ItemStack copy = stack.copy();
        NBTTagCompound tag = copy.getTagCompound();
        tag.removeTag(QUALITY);
        if (tag.isEmpty()) copy.setTagCompound(null);
        return copy;
    }
}
