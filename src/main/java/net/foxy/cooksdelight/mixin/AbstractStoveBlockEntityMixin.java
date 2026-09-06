package net.foxy.cooksdelight.mixin;

import net.foxy.cooksdelight.StoveContainer;
import net.foxy.cooksdelight.mixininterface.StoveContainerHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

@Mixin(AbstractStoveBlockEntity.class)
public class AbstractStoveBlockEntityMixin implements StoveContainerHolder {
    @Unique
    private final StoveContainer cooksdelight$stoveContainer = new StoveContainer((AbstractStoveBlockEntity) (Object) this);

    @Override
    public StoveContainer cooksdelight$getStoveContainer() {
        return cooksdelight$stoveContainer;
    }

    @Inject(
            method = "serverTick",
            at = @At("RETURN")
    )
    private static void tickStoveContainer(Level level, BlockPos pos, BlockState state,
                                           AbstractStoveBlockEntity stoveEntity, CallbackInfo ci) {
        StoveContainer.serverTick(level, pos, state, ((StoveContainerHolder) stoveEntity).cooksdelight$getStoveContainer());
    }

    @Inject(
            method = "loadAdditional",
            at = @At("RETURN")
    )
    private void loadStoveContainer(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        cooksdelight$stoveContainer.loadAdditional(tag.getCompound("cooksdelight_stove_container"), registries);
    }

    @Inject(
            method = "saveAdditional",
            at = @At("RETURN")
    )
    private void saveStoveContainer(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        CompoundTag stoveContainerTag = new CompoundTag();
        cooksdelight$stoveContainer.saveAdditional(stoveContainerTag, registries);
        tag.put("cooksdelight_stove_container", stoveContainerTag);
    }

    @Inject(
            method = "clearContent",
            at = @At("RETURN")
    )
    private void clearStoveContainer(CallbackInfo ci) {
        cooksdelight$stoveContainer.clearContent();
    }
}
