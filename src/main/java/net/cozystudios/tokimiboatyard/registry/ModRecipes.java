package net.cozystudios.tokimiboatyard.registry;

import net.cozystudios.tokimiboatyard.TokimiBoatyard;
import net.cozystudios.tokimiboatyard.recipe.SeatDowngradeRecipe;
import net.cozystudios.tokimiboatyard.recipe.SeatExpansionRecipe;
import net.cozystudios.tokimiboatyard.recipe.TierUpgradeRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModRecipes {
    public static final RecipeSerializer<TierUpgradeRecipe> TIER_UPGRADE =
        Registry.register(Registries.RECIPE_SERIALIZER,
            new Identifier(TokimiBoatyard.MOD_ID, "tier_upgrade"),
            new SpecialRecipeSerializer<>(TierUpgradeRecipe::new));

    public static final RecipeSerializer<SeatExpansionRecipe> SEAT_EXPANSION =
        Registry.register(Registries.RECIPE_SERIALIZER,
            new Identifier(TokimiBoatyard.MOD_ID, "seat_expansion"),
            new SpecialRecipeSerializer<>(SeatExpansionRecipe::new));

    public static final RecipeSerializer<SeatDowngradeRecipe> SEAT_DOWNGRADE =
        Registry.register(Registries.RECIPE_SERIALIZER,
            new Identifier(TokimiBoatyard.MOD_ID, "seat_downgrade"),
            new SpecialRecipeSerializer<>(SeatDowngradeRecipe::new));

    private ModRecipes() {}
    public static void register() {}
}
