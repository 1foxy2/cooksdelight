package net.foxy.cooksdelight.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.foxy.cooksdelight.base.CDRecipeSerializers;
import net.foxy.cooksdelight.base.CDRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class ShapelessStoveRecipe extends ShapelessRecipe implements StoveRecipe {
    protected final float experience;
    protected final int cookingTime;

    public ShapelessStoveRecipe(String group, CraftingBookCategory category, ItemStack result,
                                NonNullList<Ingredient> ingredients, float experience, int cookingTime) {
        super(group, category, result, ingredients);
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public float getExperience() {
        return experience;
    }

    @Override
    public int getCookingTime() {
        return cookingTime;
    }

    @Override
    public RecipeType<?> getType() {
        return CDRecipeTypes.STOVE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CDRecipeSerializers.STOVE_SHAPELESS.get();
    }


    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.getIngredients().size()) {
            return false;
        } else {
            return input.size() == 1 && this.getIngredients().size() == 1
                    ? this.getIngredients().getFirst().test(input.getItem(0))
                    : input.stackedContents().canCraft(this, null);
        }
    }

    public static class Serializer implements RecipeSerializer<ShapelessStoveRecipe> {
        private static final MapCodec<ShapelessStoveRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(null)),
                                Ingredient.CODEC_NONEMPTY
                                        .listOf()
                                        .fieldOf("ingredients")
                                        .flatXmap(
                                                ingredients -> {
                                                    Ingredient[] aingredient = ingredients.toArray(Ingredient[]::new); // Neo skip the empty check and immediately create the array.
                                                    if (aingredient.length == 0) {
                                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                                    } else {
                                                        return aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()
                                                                ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()))
                                                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                                    }
                                                },
                                                DataResult::success
                                        )
                                        .forGetter(ShapelessRecipe::getIngredients),
                                Codec.FLOAT.optionalFieldOf("experience", 2.0F).forGetter(StoveRecipe::getExperience),
                                Codec.INT.optionalFieldOf("cookingtime", 300).forGetter(StoveRecipe::getCookingTime)
                        )
                        .apply(instance, ShapelessStoveRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessStoveRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapelessStoveRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapelessStoveRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapelessStoveRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
            int i = buffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);
            nonnulllist.replaceAll(p_319735_ -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            float exp = buffer.readFloat();
            int cookingTime = buffer.readInt();
            return new ShapelessStoveRecipe(s, craftingbookcategory, itemstack, nonnulllist, exp, cookingTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapelessStoveRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(buffer.registryAccess()));
            buffer.writeFloat(recipe.getExperience());
            buffer.writeInt(recipe.getCookingTime());
        }
    }
}
