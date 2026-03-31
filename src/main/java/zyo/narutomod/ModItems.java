package zyo.narutomod;

import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers; // Added
import net.minecraft.world.item.SwordItem; // Added
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import zyo.narutomod.item.KusanagiSasukeItem; // Corrected import
import zyo.narutomod.item.AkatsukiCloakItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NarutoMod.MODID);

    public static final RegistryObject<Item> KUSANAGI_SASUKE = ITEMS.register("kusanagi_sasuke",
            () -> new KusanagiSasukeItem(Tiers.DIAMOND, 3, -2.4F, new net.minecraft.world.item.Item.Properties()));

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