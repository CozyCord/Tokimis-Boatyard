package net.cozystudios.tokimiboatyard.recipe;

import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.registry.ModRecipes;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
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

    private static @Nullable BoatEntity.Type woodForVanillaChestBoat(Item item) {
        if (item == Items.OAK_CHEST_BOAT)      return BoatEntity.Type.OAK;
        if (item == Items.SPRUCE_CHEST_BOAT)   return BoatEntity.Type.SPRUCE;
        if (item == Items.BIRCH_CHEST_BOAT)    return BoatEntity.Type.BIRCH;
        if (item == Items.JUNGLE_CHEST_BOAT)   return BoatEntity.Type.JUNGLE;
        if (item == Items.ACACIA_CHEST_BOAT)   return BoatEntity.Type.ACACIA;
        if (item == Items.DARK_OAK_CHEST_BOAT) return BoatEntity.Type.DARK_OAK;
        if (item == Items.MANGROVE_CHEST_BOAT) return BoatEntity.Type.MANGROVE;
        if (item == Items.CHERRY_CHEST_BOAT)   return BoatEntity.Type.CHERRY;
        if (item == Items.BAMBOO_CHEST_RAFT)   return BoatEntity.Type.BAMBOO;
        return null;
    }

    private static boolean isBoatInput(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof UpgradedBoatItem || woodForVanillaChestBoat(item) != null;
    }

    private static @Nullable BoatTier getCurrentTier(ItemStack stack) {
        return stack.getItem() instanceof UpgradedBoatItem ubi ? ubi.getTier() : null;
    }

    private static @Nullable BoatEntity.Type getWood(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof UpgradedBoatItem ubi) return ubi.getWoodType();
        return woodForVanillaChestBoat(item);
    }

    @Override
    public boolean matches(RecipeInputInventory inv, net.minecraft.world.World world) {
        ItemStack boatStack = null;
        Item ingotType = null;
        int boatCount = 0;
        int ingotCount = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (isBoatInput(stack)) {
                boatCount++;
                if (boatCount > 1) return false;
                boatStack = stack;
            } else if (tierForIngot(item) != null) {
                if (ingotType == null) ingotType = item;
                else if (ingotType != item) return false;
                ingotCount++;
            } else {
                return false;
            }
        }
        if (boatCount != 1 || ingotCount != 8) return false;

        BoatTier target = tierForIngot(ingotType);
        if (target == null) return false;
        BoatTier current = getCurrentTier(boatStack);
        int currentOrd = current == null ? -1 : current.ordinal();
        return target.ordinal() == currentOrd + 1;
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
        BoatEntity.Type wood = getWood(boatStack);
        int extras = UpgradedBoatItem.readExtraSeats(boatStack);

        ItemStack out = new ItemStack(ModItems.get(target, wood));
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
