package net.foxy.cooksdelight.event;

import net.foxy.cooksdelight.base.CDMenus;
import net.foxy.cooksdelight.mixininterface.StoveContainerHolder;
import net.foxy.cooksdelight.screen.StoveScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

@EventBusSubscriber
public class CDEvents {
    @SubscribeEvent
    public static void registerMenuScreens(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.getBlockEntity(event.getPos()) instanceof StoveContainerHolder blockEntity &&
                event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(blockEntity.cooksdelight$getStoveContainer());
        }
    }
}
