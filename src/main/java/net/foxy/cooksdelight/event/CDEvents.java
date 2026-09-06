package net.foxy.cooksdelight.event;

import net.foxy.cooksdelight.mixininterface.StoveContainerHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class CDEvents {
    @SubscribeEvent
    public static void registerMenuScreens(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.getBlockEntity(event.getPos()) instanceof StoveContainerHolder blockEntity) {
            ItemStack stack = event.getItemStack();
            if (event.getEntity().isShiftKeyDown() && !stack.isEmpty()) {
                return;
            }
            if (blockEntity.cooksdelight$getStoveContainer().blockEntity.getBlockState().getValue(BlockStateProperties.LIT)) {
                if (stack.canPerformAction(ItemAbilities.SHOVEL_DIG) || stack.is(Tags.Items.BUCKETS_WATER)) {
                    return;
                }
            } else {
                if (stack.getItem() instanceof FlintAndSteelItem || stack.getItem() instanceof FireChargeItem) {
                    return;
                }
            }
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(blockEntity.cooksdelight$getStoveContainer());
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        }
    }
}
