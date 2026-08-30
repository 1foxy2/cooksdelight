package net.foxy.cooksdelight.event;

import net.foxy.cooksdelight.base.CDMenus;
import net.foxy.cooksdelight.screen.StoveScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(Dist.CLIENT)
public class CDClientEvents {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CDMenus.STOVE.get(), StoveScreen::new);
    }
}
