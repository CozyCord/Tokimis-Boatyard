package net.cozystudios.tokimiboatyard.recipe;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TierUpgradeRecipe extends SpecialCraftingRecipe {

    public TierUpgradeRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    private static @Nullable BoatTier tierForIngot(Item ingot) {
        if (ingot == Items.COPPER_INGOT)    return BoatTier.COPPER;
        if (ingot == Items.IRON_INGOT)      return BoatTier.IRON;
        if (ingot == Items.GOLD_INGOT)      return BoatTier.GOLD;
        if (ingot == Items.EMERALD)         return BoatTier.EMERALD;
        if (ingot == Items.DIAMOND)         return BoatTier.DIAMOND;
        if (ingot == Items.NETHERITE_INGOT) return BoatTier.NETHERITE;
        return null;
    }

    private static boolean isBoatInput(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof UpgradedBoatItem || BoatWoodRegistry.getByBaseItem(item) != null;
    }

    private static @Nullable BoatTier getCurrentTier(ItemStack stack) {
        return stack.getItem() instanceof UpgradedBoatItem ubi ? ubi.getTier() : null;
    }

    private static @Nullable Identifier getWoodId(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof UpgradedBoatItem ubi) return ubi.getWoodId();
        BoatWood wood = BoatWoodRegistry.getByBaseItem(item);
        return wood == null ? null : wood.id();
    }

    @Override
    public boolean matches(RecipeInputInventory inv, net.minecraft.world.World world) {
        ItemStack boatStack = null;
        Item ingotType = null;
        int boatCount = 0;
        int ingotCount = 0;
        int rejectReason = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (isBoatInput(stack)) {
                boatCount++;
                if (boatCount > 1) { rejectReason = 1; break; }
                boatStack = stack;
            } else if (tierForIngot(item) != null) {
                if (ingotType == null) ingotType = item;
                else if (ingotType != item) { rejectReason = 2; break; }
                ingotCount++;
            } else {
                TokimiBoatyard.LOGGER.debug("[TierUpgrade] reject: slot {} has non-boat non-ingot item {}", i, Registries.ITEM.getId(item));
                return false;
            }
        }
        if (rejectReason != 0) return false;
        if (boatCount != 1 || ingotCount != 8) {
            if (boatCount == 1 || ingotCount > 0) {
                TokimiBoatyard.LOGGER.debug("[TierUpgrade] shape miss: boats={} ingots={}", boatCount, ingotCount);
            }
            return false;
        }

        BoatTier target = tierForIngot(ingotType);
        if (target == null) return false;
        BoatTier current = getCurrentTier(boatStack);
        int currentOrd = current == null ? -1 : current.ordinal();
        boolean ok = target.ordinal() == currentOrd + 1;
        TokimiBoatyard.LOGGER.debug("[TierUpgrade] boat={} currentTier={} target={} ok={}",
            Registries.ITEM.getId(boatStack.getItem()), current, target, ok);
        return ok;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager registries) {
        ItemStack boatStack = null;
        Item ingotType = null;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (isBoatInput(stack)) boatStack = stack;
            else if (tierForIngot(stack.getItem()) != null) ingotType = stack.getItem();
        }
        if (boatStack == null || ingotType == null) return ItemStack.EMPTY;

        BoatTier target = tierForIngot(ingotType);
        Identifier woodId = getWoodId(boatStack);
        if (target == null || woodId == null) return ItemStack.EMPTY;

        UpgradedBoatItem outItem = ModItems.get(target, woodId);
        if (outItem == null) return ItemStack.EMPTY;

        int extras = UpgradedBoatItem.readExtraSeats(boatStack);
        ItemStack out = new ItemStack(outItem);
        if (extras > 0) {
            out.getOrCreateNbt().putInt("ExtraSeats", extras);
        }
        return out;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 9;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TIER_UPGRADE;
    }
}
