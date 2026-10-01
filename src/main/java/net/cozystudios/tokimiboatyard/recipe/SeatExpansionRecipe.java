package net.cozystudios.tokimiboatyard.recipe;

import net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public class SeatExpansionRecipe extends SpecialCraftingRecipe {

    public SeatExpansionRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inv, net.minecraft.world.World world) {
        ItemStack boatStack = null;
        int boatCount = 0;
        int saddleCount = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof UpgradedBoatItem) {
                boatCount++;
                if (boatCount > 1) return false;
                boatStack = stack;
            } else if (stack.getItem() == Items.SADDLE) {
                saddleCount++;
            } else {
                return false;
            }
        }
        if (boatCount != 1) return false;
        if (saddleCount < 1 || saddleCount > UpgradedBoatEntity.MAX_EXTRA_SEATS) return false;

        int currentExtras = UpgradedBoatItem.readExtraSeats(boatStack);
        return currentExtras + saddleCount <= UpgradedBoatEntity.MAX_EXTRA_SEATS;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager registries) {
        ItemStack boatStack = null;
        int saddleCount = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof UpgradedBoatItem) boatStack = stack;
            else if (stack.getItem() == Items.SADDLE) saddleCount++;
        }
        if (boatStack == null) return ItemStack.EMPTY;

        ItemStack output = boatStack.copy();
        output.setCount(1);
        int currentExtras = UpgradedBoatItem.readExtraSeats(boatStack);
        int newExtras = Math.min(UpgradedBoatEntity.MAX_EXTRA_SEATS, currentExtras + saddleCount);
        output.getOrCreateNbt().putInt("ExtraSeats", newExtras);
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SEAT_EXPANSION;
    }
}
