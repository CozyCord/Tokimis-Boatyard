package net.cozystudios.tokimiboatyard.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class TokimiJeiPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return new Identifier(TokimiBoatyard.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        TokimiBoatyard.LOGGER.info("[JEI] TokimiJeiPlugin.registerRecipes called; wood registry size={}",
            BoatWoodRegistry.all().size());

        List<CraftingRecipe> crafting = new ArrayList<>();
        int upgradeCount = 0, expansionCount = 0, downgradeCount = 0;

        for (BoatTier tier : BoatTier.values()) {
            Item ingot = ingotFor(tier);
            if (ingot == null) continue;
            for (BoatWood wood : BoatWoodRegistry.all()) {
                UpgradedBoatItem output = ModItems.get(tier, wood.id());
                if (output == null) continue;
                Item input;
                if (tier.previous() == null) {
                    input = wood.baseChestBoatItem();
                } else {
                    input = ModItems.get(tier.previous(), wood.id());
                }
                if (input == null) continue;
                crafting.add(buildUpgradeRecipe(tier, wood, input, ingot, output));
                upgradeCount++;
            }
        }

        for (BoatTier tier : BoatTier.values()) {
            for (BoatWood wood : BoatWoodRegistry.all()) {
                UpgradedBoatItem item = ModItems.get(tier, wood.id());
                if (item == null) continue;
                crafting.add(buildExpansionRecipe(tier, wood, item));
                expansionCount++;
                crafting.add(buildDowngradeRecipe(tier, wood, item));
                downgradeCount++;
            }
        }

        TokimiBoatyard.LOGGER.info("[JEI] Submitting to RecipeTypes.CRAFTING: upgrades={} expansions={} downgrades={} total={}",
            upgradeCount, expansionCount, downgradeCount, crafting.size());
        registration.addRecipes(RecipeTypes.CRAFTING, crafting);
    }

    private CraftingRecipe buildUpgradeRecipe(BoatTier tier, BoatWood wood, Item input, Item ingot, UpgradedBoatItem output) {
        Identifier id = new Identifier(TokimiBoatyard.MOD_ID,
            "jei_up_" + tier.id() + "_" + wood.tokimiPath());
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(9, Ingredient.EMPTY);
        Ingredient ingotIng = Ingredient.ofItems(ingot);
        Ingredient inputIng = Ingredient.ofItems(input);
        for (int i = 0; i < 9; i++) {
            ingredients.set(i, i == 4 ? inputIng : ingotIng);
        }
        return new ShapedRecipe(id, "", CraftingRecipeCategory.MISC, 3, 3, ingredients, new ItemStack(output));
    }

    private CraftingRecipe buildExpansionRecipe(BoatTier tier, BoatWood wood, UpgradedBoatItem item) {
        Identifier id = new Identifier(TokimiBoatyard.MOD_ID,
            "jei_seat_" + tier.id() + "_" + wood.tokimiPath());
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(2, Ingredient.EMPTY);
        ingredients.set(0, Ingredient.ofItems(item));
        ingredients.set(1, Ingredient.ofItems(Items.SADDLE));
        ItemStack result = new ItemStack(item);
        result.getOrCreateNbt().putInt("ExtraSeats", 1);
        return new ShapelessRecipe(id, "", CraftingRecipeCategory.MISC, result, ingredients);
    }

    private CraftingRecipe buildDowngradeRecipe(BoatTier tier, BoatWood wood, UpgradedBoatItem item) {
        Identifier id = new Identifier(TokimiBoatyard.MOD_ID,
            "jei_down_" + tier.id() + "_" + wood.tokimiPath());
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(1, Ingredient.EMPTY);
        ItemStack boatWithSeat = new ItemStack(item);
        boatWithSeat.getOrCreateNbt().putInt("ExtraSeats", 1);
        ingredients.set(0, Ingredient.ofStacks(boatWithSeat));
        return new ShapelessRecipe(id, "", CraftingRecipeCategory.MISC, new ItemStack(Items.SADDLE), ingredients);
    }

    private Item ingotFor(BoatTier tier) {
        return switch (tier) {
            case COPPER -> Items.COPPER_INGOT;
            case IRON -> Items.IRON_INGOT;
            case GOLD -> Items.GOLD_INGOT;
            case EMERALD -> Items.EMERALD;
            case DIAMOND -> Items.DIAMOND;
            case NETHERITE -> Items.NETHERITE_INGOT;
        };
    }
}
