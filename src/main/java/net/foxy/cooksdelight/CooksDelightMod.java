package net.foxy.cooksdelight;

import net.foxy.cooksdelight.base.CDMenus;
import net.foxy.cooksdelight.base.CDRecipeSerializers;
import net.foxy.cooksdelight.base.CDRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CooksDelightMod.MOD_ID)
public class CooksDelightMod {
    public static final String MOD_ID = "cooksdelight";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CooksDelightMod(IEventBus modEventBus, ModContainer modContainer) {
        CDMenus.MENUS.register(modEventBus);
        CDRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        CDRecipeTypes.RECIPE_TYPES.register(modEventBus);
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }
}
