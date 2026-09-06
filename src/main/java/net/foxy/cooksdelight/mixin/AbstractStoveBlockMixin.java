package net.foxy.cooksdelight.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.foxy.cooksdelight.base.CDBSP;
import net.foxy.cooksdelight.mixininterface.StoveContainerHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.utility.ItemUtils;

@Mixin(AbstractStoveBlock.class)
public class AbstractStoveBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("RETURN"))
    private void addState(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(CDBSP.CLOSED);
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lvectorwing/farmersdelight/common/block/AbstractStoveBlock;registerDefaultState(" +
                            "Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private BlockState modifyDefault(BlockState state) {
        return state.setValue(CDBSP.CLOSED, false);
    }

    @Inject(
            method = "onRemove",
            at = @At(
                    value = "INVOKE",
                    target = "Lvectorwing/farmersdelight/common/utility/ItemUtils;dropItems(" +
                            "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;" +
                            "Lnet/neoforged/neoforge/items/IItemHandler;)V"
            )
    )
    private void dropStoveContainer(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci, @Local AbstractStoveBlockEntity stoveBlockEntity) {
        Containers.dropContents(level, pos, ((StoveContainerHolder) stoveBlockEntity).cooksdelight$getStoveContainer());
    }
}
