package net.foxy.cooksdelight.screen;

import net.foxy.cooksdelight.CooksDelightMod;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StoveRecipeBookComponent extends RecipeBookComponent {
    private static final Component COOKABLE = Component.translatable("container.farmersdelight.recipe_book.cookable");
    private static final WidgetSprites STOVE_FILTER_BUTTON_SPRITES = new WidgetSprites(
            CooksDelightMod.id("recipe_book/filter_enabled"),
            CooksDelightMod.id("recipe_book/filter_disabled"),
            CooksDelightMod.id("recipe_book/filter_enabled_highlighted"),
            CooksDelightMod.id("recipe_book/filter_disabled_highlighted")
    );

    @Override
    protected Component getRecipeFilterName() {
        return COOKABLE;
    }

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(STOVE_FILTER_BUTTON_SPRITES);
    }
}
