package net.foxy.cooksdelight.data;

import net.minecraft.world.item.crafting.CraftingRecipe;

public interface StoveRecipe extends CraftingRecipe {
    float getExperience();

    int getCookingTime();
}
