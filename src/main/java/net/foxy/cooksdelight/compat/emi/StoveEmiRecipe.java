package net.foxy.cooksdelight.compat.emi;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.foxy.cooksdelight.CooksDelightMod;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.utility.ClientRenderUtils;

import java.util.ArrayList;
import java.util.List;

public class StoveEmiRecipe extends EmiCraftingRecipe {
    private static final ResourceLocation BACKGROUND = CooksDelightMod.id("textures/gui/jei/stove.png");
    private static final ResourceLocation WIDGETS = CooksDelightMod.id("textures/gui/container/stove.png");
    private final int cookTime;
    private final float experience;
    private final List<ClientTooltipComponent> tooltipComponents;

    public StoveEmiRecipe(List<EmiIngredient> input, EmiStack output, ResourceLocation id, boolean shapeless,
                          int cookTime, float experience) {
        super(input, output, id, shapeless);
        this.cookTime = cookTime;
        this.experience = experience;
        this.tooltipComponents = this.createTooltipComponents();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CooksDelightEmiPlugin.STOVE_CATEGORY;
    }

    @Override
    public int getDisplayWidth() {
        return 122;
    }

    @Override
    public int getDisplayHeight() {
        return 56;
    }

    private List<ClientTooltipComponent> createTooltipComponents() {
        List<ClientTooltipComponent> tooltipStrings = new ArrayList<>();
        if (this.cookTime > 0) {
            int cookTimeSeconds = this.cookTime / 20;
            tooltipStrings.add(ClientTooltipComponent.create(Component.translatable("emi.cooking.time", cookTimeSeconds).getVisualOrderText()));
        }

        if (this.experience > 0.0F) {
            tooltipStrings.add(ClientTooltipComponent.create(Component.translatable("emi.cooking.experience", this.experience).getVisualOrderText()));
        }

        return tooltipStrings;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 122, 56, 0, 0);
        if (shapeless) {
            widgets.addTexture(EmiTexture.SHAPELESS, 60, 40);
        }
        int sOff = 0;
        if (!shapeless) {
            if (canFit(1, 3)) {
                sOff -= 1;
            }
            if (canFit(3, 1)) {
                sOff -= 3;
            }
        }
        for (int i = 0; i < 9; i++) {
            int s = i + sOff;
            if (s >= 0 && s < input.size()) {
                widgets.addSlot(input.get(s), i % 3 * 18, i / 3 * 18);
            } else {
                widgets.addSlot(EmiStack.of(ItemStack.EMPTY), i % 3 * 18, i / 3 * 18);
            }
        }
        widgets.addSlot(output, 97, 18).recipeContext(this).drawBack(false);

        widgets.addAnimatedTexture(WIDGETS, 60, 18, 24, 17, 176, 15, 10000, true, false, false);
        widgets.addTexture(WIDGETS, 97, 42, 17, 15, 176, 0);
        widgets.addTexture(WIDGETS, 64, 11, 8, 11, 176, 32);
        if (this.experience > 0.0F) {
            widgets.addTexture(WIDGETS, 63, 30, 9, 9, 176, 43);
        }

        widgets.addTooltip((mouseX, mouseY) ->
                        ClientRenderUtils.isCursorInsideBounds(60, 11, 22, 28, (double)mouseX, (double)mouseY) ?
                                this.tooltipComponents : List.of(), 0, 0, widgets.getWidth(), widgets.getHeight());
    }
}
