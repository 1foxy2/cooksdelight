package net.foxy.cooksdelight.menu;

import net.foxy.cooksdelight.base.CDMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class StoveMenu extends RecipeBookMenu<SingleRecipeInput, AbstractCookingRecipe> {

    /*public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int DATA_COUNT = 4;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;*/
    private final Container container;
    private final ContainerData data;
    protected final Level level;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;
    private final RecipeBookType recipeBookType;

    public StoveMenu(
            int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data
    ) {
        this(containerId, playerInventory, new SimpleContainer(10), new SimpleContainerData(4));
    }

    public StoveMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data
    ) {
        super(CDMenus.STOVE.get(), containerId);
        this.recipeType = RecipeType.SMELTING;
        this.recipeBookType = RecipeBookType.FURNACE;
        checkContainerSize(container, 10);
        checkContainerDataCount(data, 4);
        this.container = container;
        this.data = data;
        this.level = playerInventory.player.level();
        this.addSlot(new FurnaceResultSlot(playerInventory.player, container, 9, 127, 35));

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(container, i, 30 + (i % 3) * 18, 17 + (i / 3) * 18));
        }
        //this.addSlot(new FurnaceFuelSlot(this, container, 1, 56, 53));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public void fillCraftSlotsStackedContents(@NotNull StackedContents itemHelper) {
        if (this.container instanceof StackedContentsCompatible) {
            ((StackedContentsCompatible)this.container).fillStackedContents(itemHelper);
        }
    }

    @Override
    public void clearCraftingContent() {
        for (int i = 0; i < 10; i++) {
            this.getSlot(i).set(ItemStack.EMPTY);
        }
    }

    @Override
    public boolean recipeMatches(RecipeHolder<AbstractCookingRecipe> recipe) {
        return recipe.value().matches(new SingleRecipeInput(this.container.getItem(0)), this.level);
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    public int getGridWidth() {
        return 3;
    }

    @Override
    public int getGridHeight() {
        return 3;
    }

    @Override
    public int getSize() {
        return 10;
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index >= 10 && index < 46) {
                if (!this.moveItemStackTo(itemstack1, 1, 10, false)) {
                    if (index < 37) {
                        if (!this.moveItemStackTo(itemstack1, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
            if (index == 0) {
                player.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    protected boolean canSmelt(ItemStack stack) {
        return this.level.getRecipeManager().getRecipeFor(this.recipeType, new SingleRecipeInput(stack), this.level).isPresent();
    }

    protected boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(this.recipeType) > 0;
    }

    public float getBurnProgress() {
        int i = this.data.get(2);
        int j = this.data.get(3);
        return j != 0 && i != 0 ? Mth.clamp((float)i / (float)j, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) {
            i = 200;
        }

        return Mth.clamp((float)this.data.get(0) / (float)i, 0.0F, 1.0F);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public @NotNull RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    @Override
    public boolean shouldMoveToInventory(int slotIndex) {
        return slotIndex != 1;
    }
}
