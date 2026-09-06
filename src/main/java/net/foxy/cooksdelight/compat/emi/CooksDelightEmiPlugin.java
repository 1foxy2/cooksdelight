package net.foxy.cooksdelight.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.foxy.cooksdelight.CooksDelightMod;
import net.foxy.cooksdelight.base.CDRecipeTypes;
import net.foxy.cooksdelight.data.ShapedStoveRecipe;
import net.foxy.cooksdelight.data.ShapelessStoveRecipe;
import net.foxy.cooksdelight.data.StoveRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.Tags;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@EmiEntrypoint
public class CooksDelightEmiPlugin implements EmiPlugin {
    public static final EmiStack STOVE_WORKSTATION = EmiStack.of(ModBlocks.STOVE.get());
    public static final EmiRecipeCategory STOVE_CATEGORY
            = new EmiRecipeCategory(CooksDelightMod.id("stove"), STOVE_WORKSTATION);

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(STOVE_CATEGORY);
        registry.addWorkstation(STOVE_CATEGORY, STOVE_WORKSTATION);

        RecipeManager manager = registry.getRecipeManager();

        // Use vanilla's concept of your recipes and pass them to your EmiRecipe representation
        for (RecipeHolder<StoveRecipe> recipe : manager.getAllRecipesFor(CDRecipeTypes.STOVE.get())) {
            if (recipe.value() instanceof ShapedStoveRecipe shapedStoveRecipe) {
                registry.addRecipe(new ShapedStoveEmiRecipe(shapedStoveRecipe));
            } else if (recipe.value() instanceof ShapelessStoveRecipe shapelessStoveRecipe) {
                registry.addRecipe(new ShapelessStoveEmiRecipe(shapelessStoveRecipe));
            }
        }
    }
}
