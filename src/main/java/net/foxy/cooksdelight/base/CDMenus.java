package net.foxy.cooksdelight.base;

import net.foxy.cooksdelight.CooksDelightMod;
import net.foxy.cooksdelight.menu.StoveMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CDMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CooksDelightMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<StoveMenu>> STOVE =
            MENUS.register("stove", () -> IMenuTypeExtension.create(StoveMenu::new));
}
