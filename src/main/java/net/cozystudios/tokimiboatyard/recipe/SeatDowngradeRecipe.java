package net.cozystudios.tokimiboatyard.recipe;

import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.minecraft.core.NonNullList;
//? if >=1.21.1 {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingInput;
*///? } else {
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
//? }
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SeatDowngradeRecipe extends CustomRecipe {

    public SeatDowngradeRecipe(
//? if >=1.21.1 {
        /*CraftingBookCategory category) {
        super(category);
*///? } else {
        ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
//? }
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
        int boatCount = 0;
        for (int i = 0; i < inputSize(inv); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof UpgradedBoatItem) {
                boatCount++;
                if (boatCount > 1) return false;
                boatStack = stack;
            } else {
                return false;
            }
        }
        if (boatCount != 1) return false;
        return UpgradedBoatItem.readExtraSeats(boatStack) > 0;
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
        return new ItemStack(Items.SADDLE);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(
//? if >=1.21.1 {
        /*CraftingInput inv) {
*///? } else {
        CraftingContainer inv) {
//? }
        NonNullList<ItemStack> remainders = NonNullList.withSize(inputSize(inv), ItemStack.EMPTY);
        for (int i = 0; i < inputSize(inv); i++) {
            ItemStack stack = inv.getItem(i);
            if (!(stack.getItem() instanceof UpgradedBoatItem)) continue;
            ItemStack modified = stack.copy();
            modified.setCount(1);
            int extras = UpgradedBoatItem.readExtraSeats(stack);
            int newExtras = Math.max(0, extras - 1);
            if (newExtras > 0) {
                UpgradedBoatItem.writeExtraSeats(modified, newExtras);
            } else {
                UpgradedBoatItem.removeExtraSeats(modified);
            }
            remainders.set(i, modified);
        }
        return remainders;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SEAT_DOWNGRADE;
    }
}
