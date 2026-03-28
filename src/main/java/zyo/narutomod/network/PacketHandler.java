package zyo.narutomod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import zyo.narutomod.NarutoMod;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        INSTANCE.registerMessage(id++, ChakraChargePacket.class, ChakraChargePacket::encode, ChakraChargePacket::new, ChakraChargePacket::handle);
        INSTANCE.registerMessage(id++, JutsuC2SPacket.class, JutsuC2SPacket::encode, JutsuC2SPacket::new, JutsuC2SPacket::handle);
        INSTANCE.registerMessage(id++, SharinganTogglePacket.class, SharinganTogglePacket::encode, SharinganTogglePacket::new, SharinganTogglePacket::handle);
        INSTANCE.registerMessage(id++, SharinganSyncPacket.class, SharinganSyncPacket::encode, SharinganSyncPacket::new, SharinganSyncPacket::handle);
        INSTANCE.registerMessage(id++, AmenotejikaraPacket.class, AmenotejikaraPacket::encode, AmenotejikaraPacket::new, AmenotejikaraPacket::handle);
        INSTANCE.registerMessage(id++, TsukuyomiPacket.class, TsukuyomiPacket::encode, TsukuyomiPacket::new, TsukuyomiPacket::handle);
        INSTANCE.registerMessage(id++, TsukuyomiSyncPacket.class, TsukuyomiSyncPacket::encode, TsukuyomiSyncPacket::new, TsukuyomiSyncPacket::handle);
        INSTANCE.registerMessage(id++, SyncChakraPacket.class, SyncChakraPacket::encode, SyncChakraPacket::new, SyncChakraPacket::handle);
        INSTANCE.registerMessage(id++, ClearHandSignsPacket.class, ClearHandSignsPacket::encode, ClearHandSignsPacket::new, ClearHandSignsPacket::handle);
        INSTANCE.registerMessage(id++, InstantGenjutsuPacket.class, InstantGenjutsuPacket::encode, InstantGenjutsuPacket::new, InstantGenjutsuPacket::handle);
        INSTANCE.registerMessage(id++, StatUpgradePacket.class, StatUpgradePacket::encode, StatUpgradePacket::new, StatUpgradePacket::handle);
        INSTANCE.registerMessage(id++, SyncStatsPacket.class, SyncStatsPacket::encode, SyncStatsPacket::new, SyncStatsPacket::handle);
    }
}