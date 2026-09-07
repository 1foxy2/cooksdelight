package net.foxy.cooksdelight.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.foxy.cooksdelight.data.StoveRecipe;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {
    @Definition(id = "recipe", local = @Local(type = Recipe.class))
    @Definition(id = "CraftingRecipe", type = CraftingRecipe.class)
    @Expression("recipe instanceof CraftingRecipe")
    @WrapOperation(
            method = "getCategory",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private static boolean excludeStoveRecipe(Object object, Operation<Boolean> original) {
        return original.call(object) && !(object instanceof StoveRecipe);
    }
}
