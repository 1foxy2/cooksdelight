package net.foxy.cooksdelight.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.foxy.cooksdelight.base.CDRecipeSerializers;
import net.foxy.cooksdelight.base.CDRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class ShapedStoveRecipe extends ShapedRecipe implements StoveRecipe {
    protected final float experience;
    protected final int cookingTime;

    public ShapedStoveRecipe(
            String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result,
            boolean showNotification, float experience, int cookingTime) {
        super(group, category, pattern, result, showNotification);
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
    public @NotNull RecipeType<?> getType() {
        return CDRecipeTypes.STOVE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CDRecipeSerializers.STOVE_SHAPED.get();
    }

    public static class Serializer implements RecipeSerializer<ShapedStoveRecipe> {
        public static final MapCodec<ShapedStoveRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                                ShapedRecipePattern.MAP_CODEC.forGetter(p_311733_ -> p_311733_.pattern),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_311730_ -> p_311730_.getResultItem(null)),
                                Codec.BOOL.optionalFieldOf("show_notification", Boolean.TRUE).forGetter(ShapedRecipe::showNotification),
                                Codec.FLOAT.optionalFieldOf("experience", 2.0F).forGetter(StoveRecipe::getExperience),
                                Codec.INT.optionalFieldOf("cookingtime", 300).forGetter(StoveRecipe::getCookingTime)
                        )
                        .apply(instance, ShapedStoveRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedStoveRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapedStoveRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedStoveRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapedStoveRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            boolean flag = buffer.readBoolean();
            float exp = buffer.readFloat();
            int cookingTime = buffer.readInt();
            return new ShapedStoveRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag, exp, cookingTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedStoveRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(buffer.registryAccess()));
            buffer.writeBoolean(recipe.showNotification());
            buffer.writeFloat(recipe.getExperience());
            buffer.writeInt(recipe.getCookingTime());
        }
    }
}
