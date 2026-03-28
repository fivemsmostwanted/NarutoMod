package zyo.narutomod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import zyo.narutomod.NarutoMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NarutoMod.MODID);

    public static final RegistryObject<Item> SASUKE_KATANA = ITEMS.register("sasuke_katana",
            () -> new SasukeKatanaItem());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}