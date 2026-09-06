package net.foxy.cooksdelight.base;

import net.foxy.cooksdelight.CooksDelightMod;
import net.foxy.cooksdelight.data.ShapedStoveRecipe;
import net.foxy.cooksdelight.data.StoveRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CDRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, CooksDelightMod.MOD_ID);

    public static final Supplier<RecipeType<StoveRecipe>> STOVE =
            RECIPE_TYPES.register("stove", () -> RecipeType.simple(CooksDelightMod.id("stove")));
}
