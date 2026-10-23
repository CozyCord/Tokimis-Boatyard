package net.cozystudios.tokimiboatyard.recipe;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
//? if >=1.21.1 {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingInput;
*///? } else {
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
//? }
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public class TierUpgradeRecipe extends CustomRecipe {

    public TierUpgradeRecipe(
//? if >=1.21.1 {
        /*CraftingBookCategory category) {
        super(category);
*///? } else {
        ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
//? }
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

    private static @Nullable ResourceLocation getWoodId(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof UpgradedBoatItem ubi) return ubi.getWoodId();
        BoatWood wood = BoatWoodRegistry.getByBaseItem(item);
        return wood == null ? null : wood.id();
    }

    private static int inputSize(
//? if >=1.21.1 {
        /*CraftingInput input) {
        return input.size();
*///? } else {
        CraftingContainer input) {
        return input.getContainerSize();
//? }
    }

    @Override
    public boolean matches(
//? if >=1.21.1 {
        /*CraftingInput inv,
*///? } else {
        CraftingContainer inv,
//? }
        net.minecraft.world.level.Level world) {
        ItemStack boatStack = null;
        Item ingotType = null;
        int boatCount = 0;
        int ingotCount = 0;
        int rejectReason = 0;

        for (int i = 0; i < inputSize(inv); i++) {
            ItemStack stack = inv.getItem(i);
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
                TokimiBoatyard.LOGGER.debug("[TierUpgrade] reject: slot {} has non-boat non-ingot item {}", i, BuiltInRegistries.ITEM.getKey(item));
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
            BuiltInRegistries.ITEM.getKey(boatStack.getItem()), current, target, ok);
        return ok;
    }

    @Override
    public ItemStack assemble(
//? if >=1.21.1 {
        /*CraftingInput inv,
*///? } else {
        CraftingContainer inv,
//? }
//? if >=1.21.1 {
        /*HolderLookup.Provider registries) {
*///? } else {
        RegistryAccess registries) {
//? }
        ItemStack boatStack = null;
        Item ingotType = null;
        for (int i = 0; i < inputSize(inv); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (isBoatInput(stack)) boatStack = stack;
            else if (tierForIngot(stack.getItem()) != null) ingotType = stack.getItem();
        }
        if (boatStack == null || ingotType == null) return ItemStack.EMPTY;

        BoatTier target = tierForIngot(ingotType);
        ResourceLocation woodId = getWoodId(boatStack);
        if (target == null || woodId == null) return ItemStack.EMPTY;

        UpgradedBoatItem outItem = ModItems.get(target, woodId);
        if (outItem == null) return ItemStack.EMPTY;

        int extras = UpgradedBoatItem.readExtraSeats(boatStack);
        ItemStack out = new ItemStack(outItem);
        if (extras > 0) {
            UpgradedBoatItem.writeExtraSeats(out, extras);
        }
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 9;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TIER_UPGRADE;
    }
}
