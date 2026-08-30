package net.foxy.cooksdelight;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.foxy.cooksdelight.menu.StoveMenu;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StoveContainer implements WorldlyContainer, MenuProvider, RecipeCraftingHolder, StackedContentsCompatible {
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;
    protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    int litTime;
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
    private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
    public final AbstractStoveBlockEntity blockEntity;

    public StoveContainer(AbstractStoveBlockEntity blockEntity) {
        recipeType = RecipeType.SMELTING;
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
        CompoundTag compoundtag = tag.getCompound("RecipesUsed");

        for (String s : compoundtag.getAllKeys()) {
            this.recipesUsed.put(ResourceLocation.parse(s), compoundtag.getInt(s));
        }
    }

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("BurnTime", this.litTime);
        tag.putInt("CookTime", this.cookingProgress);
        tag.putInt("CookTimeTotal", this.cookingTotalTime);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        CompoundTag compoundtag = new CompoundTag();
        this.recipesUsed.forEach((p_187449_, p_187450_) -> compoundtag.putInt(p_187449_.toString(), p_187450_));
        tag.put("RecipesUsed", compoundtag);
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
        return this.litTime > 0;
    }


    public static void serverTick(Level level, BlockPos pos, BlockState state, StoveContainer blockEntity) {
        boolean flag = blockEntity.isLit();
        boolean flag1 = false;
        if (blockEntity.isLit()) {
            blockEntity.litTime--;
        }

        ItemStack itemstack = blockEntity.items.get(1);
        ItemStack itemstack1 = blockEntity.items.get(0);
        boolean flag2 = !itemstack1.isEmpty();
        boolean flag3 = !itemstack.isEmpty();
        if (blockEntity.isLit() || flag3 && flag2) {
            RecipeHolder<?> recipeholder;
            if (flag2) {
                recipeholder = blockEntity.quickCheck.getRecipeFor(new SingleRecipeInput(itemstack1), level).orElse(null);
            } else {
                recipeholder = null;
            }

            int i = blockEntity.getMaxStackSize();
            if (!blockEntity.isLit() && canBurn(level.registryAccess(), recipeholder, blockEntity.items, i, blockEntity)) {
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
            }

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
        } else if (!blockEntity.isLit() && blockEntity.cookingProgress > 0) {
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

    private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipe, NonNullList<ItemStack> inventory, int maxStackSize, StoveContainer furnace) {
        if (!inventory.get(0).isEmpty() && recipe != null) {
            ItemStack itemstack = ((RecipeHolder<? extends AbstractCookingRecipe>) recipe).value().assemble(new SingleRecipeInput(furnace.getItem(0)), registryAccess);
            if (itemstack.isEmpty()) {
                return false;
            } else {
                ItemStack itemstack1 = inventory.get(2);
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

    private static boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipe, NonNullList<ItemStack> inventory, int maxStackSize, StoveContainer furnace) {
        if (recipe != null && canBurn(registryAccess, recipe, inventory, maxStackSize, furnace)) {
            ItemStack itemstack = inventory.get(0);
            ItemStack itemstack1 = ((RecipeHolder<? extends AbstractCookingRecipe>) recipe).value().assemble(new SingleRecipeInput(furnace.getItem(0)), registryAccess);
            ItemStack itemstack2 = inventory.get(2);
            if (itemstack2.isEmpty()) {
                inventory.set(2, itemstack1.copy());
            } else if (ItemStack.isSameItemSameComponents(itemstack2, itemstack1)) {
                itemstack2.grow(itemstack1.getCount());
            }

            if (itemstack.is(Blocks.WET_SPONGE.asItem()) && !inventory.get(1).isEmpty() && inventory.get(1).is(Items.BUCKET)) {
                inventory.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemstack.shrink(1);
            return true;
        } else {
            return false;
        }
    }

    private static int getTotalCookTime(Level level, StoveContainer blockEntity) {
        SingleRecipeInput singlerecipeinput = new SingleRecipeInput(blockEntity.getItem(0));
        return blockEntity.quickCheck.getRecipeFor(singlerecipeinput, level).map(p_300840_ -> p_300840_.value().getCookingTime()).orElse(200);
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
        if (recipe != null) {
            ResourceLocation resourcelocation = recipe.id();
            this.recipesUsed.addTo(resourcelocation, 1);
        }
    }

    @Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> items) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        List<RecipeHolder<?>> list = this.getRecipesToAwardAndPopExperience(player.serverLevel(), player.position());
        player.awardRecipes(list);

        for (RecipeHolder<?> recipeholder : list) {
            if (recipeholder != null) {
                player.triggerRecipeCrafted(recipeholder, this.items);
            }
        }

        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 popVec) {
        List<RecipeHolder<?>> list = Lists.newArrayList();

        for (Object2IntMap.Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
            level.getRecipeManager().byKey(entry.getKey()).ifPresent(p_300839_ -> {
                list.add((RecipeHolder<?>)p_300839_);
                createExperience(level, popVec, entry.getIntValue(), ((AbstractCookingRecipe)p_300839_.value()).getExperience());
            });
        }

        return list;
    }

    private static void createExperience(ServerLevel level, Vec3 popVec, int recipeIndex, float experience) {
        int i = Mth.floor((float)recipeIndex * experience);
        float f = Mth.frac((float)recipeIndex * experience);
        if (f != 0.0F && Math.random() < (double)f) {
            i++;
        }

        ExperienceOrb.award(level, popVec, i);
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
