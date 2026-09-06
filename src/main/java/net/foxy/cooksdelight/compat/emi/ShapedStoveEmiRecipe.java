package net.foxy.cooksdelight.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.recipe.EmiShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ShapedStoveEmiRecipe extends EmiShapedRecipe {
    public ShapedStoveEmiRecipe(ShapedRecipe recipe) {
        super(recipe);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CooksDelightEmiPlugin.STOVE_CATEGORY;
    }
}
