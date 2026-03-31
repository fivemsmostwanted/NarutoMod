package zyo.narutomod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import zyo.narutomod.ModItems;
import zyo.narutomod.NarutoMod;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NarutoMod.MODID);

    public static final RegistryObject<CreativeModeTab> NARUTO_TAB = CREATIVE_MODE_TABS.register("naruto_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.AKATSUKI_CLOAK.get())) // The icon of the tab
                    .title(Component.translatable("creativetab.naruto_tab"))
                    .displayItems((parameters, output) -> {
                        // Add all your items here manually
                        output.accept(ModItems.AKATSUKI_CLOAK.get());
                        output.accept(ModItems.KUSANAGI_SASUKE.get());
                        // output.accept(ModItems.FIREBALL_SCROLL.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}