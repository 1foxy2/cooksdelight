package net.foxy.cooksdelight;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.foxy.cooksdelight.base.CDBSP;
import net.foxy.cooksdelight.base.CDRecipeTypes;
import net.foxy.cooksdelight.data.ShapedStoveRecipe;
import net.foxy.cooksdelight.data.StoveRecipe;
import net.foxy.cooksdelight.menu.StoveMenu;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class StoveContainer implements WorldlyContainer, MenuProvider, RecipeCraftingHolder, StackedContentsCompatible {
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    private final RecipeType<StoveRecipe> recipeType;
    protected NonNullList<ItemStack> items = NonNullList.withSize(10, ItemStack.EMPTY);
    int litTime = Short.MIN_VALUE;
    int litDuration;
    int cookingProgress;
    int cookingTotalTime;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int p_58431_) {
            switch (p_58431_) {
                case 0:
                    if (litDuration > Short.MAX_VALUE) {
                        // Neo: preserve litTime / litDuration ratio on the client as data slots are synced as shorts.
                        return net.minecraft.util.Mth.floor(((double) litTime / litDuration) * Short.MAX_VALUE);
                    }

                    return StoveContainer.this.litTime;
                case 1:
                    return Math.min(StoveContainer.this.litDuration, Short.MAX_VALUE);
                case 2:
                    return StoveContainer.this.cookingProgress;
                case 3:
                    return StoveContainer.this.cookingTotalTime;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int p_58433_, int p_58434_) {
            switch (p_58433_) {
                case 0:
                    StoveContainer.this.litTime = p_58434_;
                    break;
                case 1:
                    StoveContainer.this.litDuration = p_58434_;
                    break;
                case 2:
                    StoveContainer.this.cookingProgress = p_58434_;
                    break;
                case 3:
                    StoveContainer.this.cookingTotalTime = p_58434_;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private final RecipeManager.CachedCheck<CraftingInput, StoveRecipe> quickCheck;
    public final AbstractStoveBlockEntity blockEntity;

    public StoveContainer(AbstractStoveBlockEntity blockEntity) {
        recipeType = CDRecipeTypes.STOVE.get();
        this.quickCheck = RecipeManager.createCheck(recipeType);
        this.blockEntity = blockEntity;
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.litTime = tag.getInt("BurnTime");
        this.cookingProgress = tag.getInt("CookTime");
        this.cookingTotalTime = tag.getInt("CookTimeTotal");
        this.litDuration = this.getBurnDuration(this.items.get(1));
    }

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("BurnTime", this.litTime);
        tag.putInt("CookTime", this.cookingProgress);
        tag.putInt("CookTimeTotal", this.cookingTotalTime);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }


    protected int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            return fuel.getBurnTime(this.recipeType);
        }
    }
    private static boolean isNeverAFurnaceFuel(Item item) {
        return item.builtInRegistryHolder().is(ItemTags.NON_FLAMMABLE_WOOD);
    }

    private static void add(Map<Item, Integer> map, TagKey<Item> itemTag, int burnTime) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(itemTag)) {
            if (!isNeverAFurnaceFuel(holder.value())) {
                map.put(holder.value(), burnTime);
            }
        }
    }

    private static void add(Map<Item, Integer> map, ItemLike p_item, int burnTime) {
        Item item = p_item.asItem();
        if (isNeverAFurnaceFuel(item)) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw (IllegalStateException) Util.pauseInIde(
                        new IllegalStateException(
                                "A developer tried to explicitly make fire resistant item " + item.getName(null).getString() + " a furnace fuel. That will not work!"
                        )
                );
            }
        } else {
            map.put(item, burnTime);
        }
    }

    private boolean isLit() {
        return blockEntity.getBlockState().getValue(BlockStateProperties.LIT);
    }


    public static void serverTick(Level level, BlockPos pos, BlockState state, StoveContainer blockEntity) {
        boolean flag = blockEntity.isLit();
        boolean flag1 = false;
        if (blockEntity.isLit()) {
            if (blockEntity.litTime == Short.MIN_VALUE) {
                blockEntity.litTime = 3600 + level.random.nextInt(3600);
            }
            blockEntity.litTime--;
            if (blockEntity.litTime <= 0) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
                blockEntity.litTime = Short.MIN_VALUE;
            }
        }

        boolean isEmpty = blockEntity.isEmpty();
        if (isEmpty == blockEntity.blockEntity.getBlockState().getValue(CDBSP.CLOSED)) {
            level.setBlock(pos, state.setValue(CDBSP.CLOSED, !isEmpty), 3);
        }

        if (blockEntity.isLit()) {
            RecipeHolder<StoveRecipe> recipeholder;
            if (!blockEntity.items.isEmpty()) {
                recipeholder = blockEntity.quickCheck.getRecipeFor(CraftingInput.of(3, 3, blockEntity.items), level).orElse(null);
            } else {
                recipeholder = null;
            }

            int i = blockEntity.getMaxStackSize();
            /*if (!blockEntity.isLit() && canBurn(level.registryAccess(), recipeholder, blockEntity.items, i, blockEntity)) {
                blockEntity.litTime = blockEntity.getBurnDuration(itemstack);
                blockEntity.litDuration = blockEntity.litTime;
                if (blockEntity.isLit()) {
                    flag1 = true;
                    if (itemstack.hasCraftingRemainingItem())
                        blockEntity.items.set(1, itemstack.getCraftingRemainingItem());
                    else
                    if (flag3) {
                        Item item = itemstack.getItem();
                        itemstack.shrink(1);
                        if (itemstack.isEmpty()) {
                            blockEntity.items.set(1, itemstack.getCraftingRemainingItem());
                        }
                    }
                }
            }*/

            if (blockEntity.isLit() && canBurn(level.registryAccess(), recipeholder, blockEntity.items, i, blockEntity)) {
                blockEntity.cookingProgress++;
                if (blockEntity.cookingProgress == blockEntity.cookingTotalTime) {
                    blockEntity.cookingProgress = 0;
                    blockEntity.cookingTotalTime = getTotalCookTime(level, blockEntity);
                    if (burn(level.registryAccess(), recipeholder, blockEntity.items, i, blockEntity)) {
                        blockEntity.setRecipeUsed(recipeholder);
                    }

                    flag1 = true;
                }
            } else {
                blockEntity.cookingProgress = 0;
            }
        } else if (blockEntity.cookingProgress > 0) {
            blockEntity.cookingProgress = Mth.clamp(blockEntity.cookingProgress - 2, 0, blockEntity.cookingTotalTime);
        }

        if (flag != blockEntity.isLit()) {
            flag1 = true;
            state = state.setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(blockEntity.isLit()));
            level.setBlock(pos, state, 3);
        }

        if (flag1) {
            setChanged(level, pos, state);
        }
    }

    protected static void setChanged(Level level, BlockPos pos, BlockState state) {
        level.blockEntityChanged(pos);
        if (!state.isAir()) {
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<StoveRecipe> recipe, NonNullList<ItemStack> inventory, int maxStackSize, StoveContainer stove) {
        CraftingInput input = CraftingInput.of(3, 3, stove.items);
        if (!input.items().isEmpty() && recipe != null) {
            ItemStack itemstack = recipe.value().assemble(input, registryAccess);
            if (itemstack.isEmpty()) {
                return false;
            } else {
                ItemStack itemstack1 = inventory.get(9);
                if (itemstack1.isEmpty()) {
                    return true;
                } else if (!ItemStack.isSameItemSameComponents(itemstack1, itemstack)) {
                    return false;
                } else {
                    return itemstack1.getCount() + itemstack.getCount() <= maxStackSize && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize() // Neo fix: make furnace respect stack sizes in furnace recipes
                            ? true
                            : itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxStackSize(); // Neo fix: make furnace respect stack sizes in furnace recipes
                }
            }
        } else {
            return false;
        }
    }

    private static boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<StoveRecipe> recipe, NonNullList<ItemStack> inventory, int maxStackSize, StoveContainer stove) {
        if (recipe != null && canBurn(registryAccess, recipe, inventory, maxStackSize, stove)) {
            CraftingInput.Positioned craftinginput$positioned = CraftingInput.ofPositioned(3, 3, stove.items);
            CraftingInput craftinginput = craftinginput$positioned.input();
            ItemStack result = recipe.value().assemble(craftinginput, registryAccess);
            ItemStack itemstack2 = inventory.get(9);
            if (itemstack2.isEmpty()) {
                inventory.set(9, result.copy());
            } else if (ItemStack.isSameItemSameComponents(itemstack2, result)) {
                itemstack2.grow(result.getCount());
            }

            int i = craftinginput$positioned.left();
            int j = craftinginput$positioned.top();
            NonNullList<ItemStack> nonnulllist = stove.blockEntity.getLevel().getRecipeManager().getRemainingItemsFor(CDRecipeTypes.STOVE.get(), craftinginput, stove.blockEntity.getLevel());

            for (int k = 0; k < craftinginput.height(); k++) {
                for (int l = 0; l < craftinginput.width(); l++) {
                    int i1 = l + i + (k + j) * 3;
                    ItemStack itemstack = stove.getItem(i1);
                    ItemStack itemstack1 = nonnulllist.get(l + k * craftinginput.width());
                    if (!itemstack.isEmpty()) {
                        stove.removeItem(i1, 1);
                        itemstack = stove.getItem(i1);
                    }

                    if (!itemstack1.isEmpty()) {
                        if (itemstack.isEmpty()) {
                            stove.setItem(i1, itemstack1);
                        } else if (ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                            itemstack1.grow(itemstack.getCount());
                            stove.setItem(i1, itemstack1);
                        } else {
                            BlockPos pos = stove.blockEntity.getBlockPos().above();
                            Containers.dropItemStack(stove.blockEntity.getLevel(), pos.getX(), pos.getY(), pos.getZ(), itemstack1);
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static int getTotalCookTime(Level level, StoveContainer blockEntity) {
        //SingleRecipeInput singlerecipeinput = new SingleRecipeInput(blockEntity.getItem(0));
        return 300;
                //blockEntity.quickCheck.getRecipeFor(singlerecipeinput, level) TODO
                //.map(p_300840_ -> p_300840_.value().getCookingTime()).orElse(200);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(null) > 0;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    /**
     * Returns {@code true} if automation can insert the given item in the given slot from the given side.
     */
    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStack);
    }

    /**
     * Returns {@code true} if automation can extract the given item in the given slot from the given side.
     */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && index == 1 ? stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET) : true;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setItem(int index, ItemStack stack) {
        ItemStack itemstack = this.items.get(index);
        boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, stack);
        this.items.set(index, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        if (index == 0 && !flag) {
            this.cookingTotalTime = getTotalCookTime(this.blockEntity.getLevel(), this);
            this.cookingProgress = 0;
            this.setChanged();
        }
    }

    /**
     * Returns {@code true} if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For guis use Slot.isItemValid
     */
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 2) {
            return false;
        } else if (index != 1) {
            return true;
        } else {
            ItemStack itemstack = this.items.get(1);
            return stack.getBurnTime(this.recipeType) > 0 || stack.is(Items.BUCKET) && !itemstack.is(Items.BUCKET);
        }
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {

    }

    @Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> items) {
    }

    @Override
    public void fillStackedContents(StackedContents helper) {
        for (ItemStack itemstack : this.items) {
            helper.accountStack(itemstack);
        }
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.getItems()) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.getItems().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), slot, amount);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.getItems(), slot);
    }

    @Override
    public void setChanged() {
        this.blockEntity.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(blockEntity, player);
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    @Override
    public Component getDisplayName() {
        return blockEntity.getBlockState().getBlock().asItem().getDescription();
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StoveMenu(containerId, playerInventory, this, dataAccess);
    }


}
