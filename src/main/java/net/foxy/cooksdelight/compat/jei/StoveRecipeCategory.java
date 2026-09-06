package net.foxy.cooksdelight.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.foxy.cooksdelight.data.ShapedStoveRecipe;
import net.foxy.cooksdelight.data.StoveRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.List;


public class StoveRecipeCategory extends AbstractRecipeCategory<RecipeHolder<StoveRecipe>> {
    private final IGuiHelper guiHelper;
    private final ICraftingGridHelper craftingGridHelper;

    public StoveRecipeCategory(IGuiHelper guiHelper) {
        super(
                CooksDelightJeiPlugin.STOVE.get(),
                Component.translatable("emi.category.cooksdelight.stove"),
                guiHelper.createDrawableIngredient(
                        VanillaTypes.ITEM_STACK,
                        ModBlocks.STOVE.get().asItem().getDefaultInstance()
                ),
                116,
                54
        );
        this.guiHelper = guiHelper;
        this.craftingGridHelper = guiHelper.createCraftingGridHelper();
    }

    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<StoveRecipe> recipeHolder, IFocusGroup focuses) {
        StoveRecipe recipe = recipeHolder.value();
        ItemStack resultItem = RecipeUtil.getResultItem(recipe);
        int width = 3;
        int height = 3;
        craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
        craftingGridHelper.createAndSetIngredients(builder, recipe.getIngredients(), width, height);
    }

    public void draw(RecipeHolder<StoveRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IDrawableStatic recipeArrow = this.guiHelper.getRecipeArrow();
        recipeArrow.draw(guiGraphics, 61, (54 - recipeArrow.getHeight()) / 2);
    }
}
