package net.cozystudios.tokimiboatyard.recipe;

import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class SeatDowngradeRecipe extends SpecialCraftingRecipe {

    public SeatDowngradeRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inv, net.minecraft.world.World world) {
        ItemStack boatStack = null;
        int boatCount = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
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
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager registries) {
        return new ItemStack(Items.SADDLE);
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inv) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!(stack.getItem() instanceof UpgradedBoatItem)) continue;
            ItemStack modified = stack.copy();
            modified.setCount(1);
            int extras = UpgradedBoatItem.readExtraSeats(stack);
            int newExtras = Math.max(0, extras - 1);
            if (newExtras > 0) {
                modified.getOrCreateNbt().putInt("ExtraSeats", newExtras);
            } else if (modified.hasNbt()) {
                NbtCompound nbt = modified.getNbt();
                nbt.remove("ExtraSeats");
                if (nbt.isEmpty()) modified.setNbt(null);
            }
            remainders.set(i, modified);
        }
        return remainders;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SEAT_DOWNGRADE;
    }
}
