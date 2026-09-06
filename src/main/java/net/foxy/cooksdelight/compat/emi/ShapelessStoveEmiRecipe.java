package net.foxy.cooksdelight.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.recipe.EmiShapedRecipe;
import dev.emi.emi.recipe.EmiShapelessRecipe;
import net.foxy.cooksdelight.data.ShapelessStoveRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ShapelessStoveEmiRecipe extends EmiShapelessRecipe {
    public ShapelessStoveEmiRecipe(ShapelessStoveRecipe recipe) {
        super(recipe);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CooksDelightEmiPlugin.STOVE_CATEGORY;
    }
}
