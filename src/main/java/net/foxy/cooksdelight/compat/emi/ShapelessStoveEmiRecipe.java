package net.foxy.cooksdelight.compat.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiShapedRecipe;
import net.foxy.cooksdelight.data.ShapelessStoveRecipe;

public class ShapelessStoveEmiRecipe extends StoveEmiRecipe {

    public ShapelessStoveEmiRecipe(ShapelessStoveRecipe recipe) {
        super(recipe.getIngredients().stream().map(i -> EmiIngredient.of(i)).toList(),
                EmiStack.of(EmiPort.getOutput(recipe)), EmiPort.getId(recipe), true, recipe.getCookingTime(), recipe.getExperience());
        EmiShapedRecipe.setRemainders(input, recipe);
    }

    @Override
    public boolean canFit(int width, int height) {
        return input.size() <= width * height;
    }
}
