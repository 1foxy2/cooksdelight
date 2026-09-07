package net.foxy.cooksdelight.base;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.List;
import java.util.function.Supplier;

public class CDEnums {
    public static final EnumProxy<RecipeBookType> STOVE = new EnumProxy<>(RecipeBookType.class);
    public static final EnumProxy<RecipeBookCategories> STOVE_FOOD =
            new EnumProxy<>(RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(ModItems.WHEAT_DOUGH.get().getDefaultInstance()));
}
