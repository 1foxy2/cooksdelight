package net.foxy.cooksdelight.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.foxy.cooksdelight.CooksDelightMod;
import net.foxy.cooksdelight.base.CDRecipeTypes;
import net.foxy.cooksdelight.data.ShapedStoveRecipe;
import net.foxy.cooksdelight.data.StoveRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.function.Supplier;

@JeiPlugin
public class CooksDelightJeiPlugin implements IModPlugin {
    public static final Supplier<RecipeType<RecipeHolder<StoveRecipe>>> STOVE =
        RecipeType.createFromDeferredVanilla(CDRecipeTypes.STOVE);
    @Override
    public ResourceLocation getPluginUid() {
        return CooksDelightMod.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new StoveRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(STOVE.get(), Minecraft.getInstance().level
                .getRecipeManager().getAllRecipesFor(CDRecipeTypes.STOVE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(STOVE.get(), ModBlocks.STOVE.get());
    }
}
