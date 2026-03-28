package zyo.narutomod;

import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Import the items from your item folder so this root file can see them!
import zyo.narutomod.item.SasukeKatanaItem;
import zyo.narutomod.item.AkatsukiCloakItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NarutoMod.MODID);

    public static final RegistryObject<Item> SASUKE_KATANA = ITEMS.register("sasuke_katana", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> AKATSUKI_CLOAK = ITEMS.register("akatsuki_cloak",
            () -> new AkatsukiCloakItem(
                    ArmorMaterials.NETHERITE,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1)
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}