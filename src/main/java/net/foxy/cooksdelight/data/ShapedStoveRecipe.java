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
    public ShapedStoveRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    public ShapedStoveRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result) {
        super(group, category, pattern, result);
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
                p_340778_ -> p_340778_.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                                ShapedRecipePattern.MAP_CODEC.forGetter(p_311733_ -> p_311733_.pattern),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_311730_ -> p_311730_.getResultItem(null)),
                                Codec.BOOL.optionalFieldOf("show_notification", Boolean.TRUE).forGetter(ShapedRecipe::showNotification)
                        )
                        .apply(p_340778_, ShapedStoveRecipe::new)
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
            return new ShapedStoveRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(buffer.registryAccess()));
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
