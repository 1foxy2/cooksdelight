package net.foxy.cooksdelight.event;

import net.foxy.cooksdelight.base.CDEnums;
import net.foxy.cooksdelight.base.CDMenus;
import net.foxy.cooksdelight.base.CDRecipeTypes;
import net.foxy.cooksdelight.screen.StoveScreen;
import net.minecraft.client.RecipeBookCategories;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class CDClientEvents {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CDMenus.STOVE.get(), StoveScreen::new);
    }

    @SubscribeEvent
    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerAggregateCategory(CDEnums.STOVE_FOOD.getValue(), List.of(CDEnums.STOVE_FOOD.getValue()));
        event.registerBookCategories(CDEnums.STOVE.getValue(), List.of(CDEnums.STOVE_FOOD.getValue()));
        event.registerRecipeCategoryFinder(CDRecipeTypes.STOVE.get(), r -> CDEnums.STOVE_FOOD.getValue());
    }
}
