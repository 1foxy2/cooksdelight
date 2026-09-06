package net.foxy.cooksdelight.base;

import net.foxy.cooksdelight.CooksDelightMod;
import net.foxy.cooksdelight.data.ShapedStoveRecipe;
import net.foxy.cooksdelight.data.ShapelessStoveRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CDRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS  =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CooksDelightMod.MOD_ID);

    public static final Supplier<RecipeSerializer<ShapedStoveRecipe>> STOVE_SHAPED =
            RECIPE_SERIALIZERS .register("stove_shaped", ShapedStoveRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<ShapelessStoveRecipe>> STOVE_SHAPELESS =
            RECIPE_SERIALIZERS .register("stove_shapeless", ShapelessStoveRecipe.Serializer::new);
}
