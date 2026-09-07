package net.foxy.cooksdelight.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.foxy.cooksdelight.CooksDelightMod;
import net.foxy.cooksdelight.data.ShapedStoveRecipe;
import net.foxy.cooksdelight.data.StoveRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.utility.ClientRenderUtils;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;


public class StoveRecipeCategory implements IRecipeCategory<RecipeHolder<StoveRecipe>> {
    private final ICraftingGridHelper craftingGridHelper;
    protected final IDrawable heatIndicator;
    protected final IDrawable timeIcon;
    protected final IDrawable expIcon;
    protected final IDrawableAnimated arrow;
    private final IDrawable background;
    private final Component title;
    private final IDrawable icon;

    public StoveRecipeCategory(IGuiHelper guiHelper) {
        this.craftingGridHelper = guiHelper.createCraftingGridHelper();
        ResourceLocation interfaceImage = CooksDelightMod.id("textures/gui/container/stove.png");
        this.background = guiHelper.createDrawable(CooksDelightMod.id("textures/gui/jei/stove.png"), 0, 0, 122, 56);
        this.heatIndicator = guiHelper.createDrawable(interfaceImage, 176, 0, 17, 15);
        this.timeIcon = guiHelper.createDrawable(interfaceImage, 176, 32, 8, 11);
        this.expIcon = guiHelper.createDrawable(interfaceImage, 176, 43, 9, 9);
        this.arrow = guiHelper.drawableBuilder(interfaceImage, 176, 15, 24, 17).buildAnimated(300, IDrawableAnimated.StartDirection.LEFT, false);
        this.title = Component.translatable("emi.category.cooksdelight.stove");
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                ModBlocks.STOVE.get().asItem().getDefaultInstance()
        );
    }

    @Override
    public RecipeType<RecipeHolder<StoveRecipe>> getRecipeType() {
        return CooksDelightJeiPlugin.STOVE.get();
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 122;
    }

    @Override
    public int getHeight() {
        return 56;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<StoveRecipe> recipeHolder, IFocusGroup focuses) {
        StoveRecipe recipe = recipeHolder.value();
        ItemStack resultItem = RecipeUtil.getResultItem(recipe);
        int width = 3;
        int height = 3;
        IRecipeSlotBuilder outputSlot = builder.addOutputSlot(98, 19);
        outputSlot.addIngredient(VanillaTypes.ITEM_STACK, resultItem);
        craftingGridHelper.createAndSetIngredients(builder, recipe.getIngredients(), width, height);
    }

    public void draw(RecipeHolder<StoveRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 0);
        this.arrow.draw(guiGraphics, 60, 18);
        this.heatIndicator.draw(guiGraphics, 97, 42);
        this.timeIcon.draw(guiGraphics, 64, 11);
        if (recipeHolder.value().getExperience() > 0.0F) {
            this.expIcon.draw(guiGraphics, 63, 30);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<StoveRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (ClientRenderUtils.isCursorInsideBounds(61, 11, 22, 28, mouseX, mouseY)) {
            int cookTime = recipe.value().getCookingTime();
            if (cookTime > 0) {
                int cookTimeSeconds = cookTime / 20;
                tooltip.add(Component.translatable("gui.jei.category.smelting.time.seconds", new Object[]{cookTimeSeconds}));
            }

            float experience = recipe.value().getExperience();
            if (experience > 0.0F) {
                tooltip.add(Component.translatable("gui.jei.category.smelting.experience", new Object[]{experience}));
            }
        }

    }
}
