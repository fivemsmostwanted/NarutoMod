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
        // 1. Jutsu casting (Fireballl
        INSTANCE.registerMessage(id++, JutsuC2SPacket.class, JutsuC2SPacket::encode, JutsuC2SPacket::new, JutsuC2SPacket::handle);

        // 2. Toggling the Sharingan (Client -> Server)
        INSTANCE.registerMessage(id++, SharinganTogglePacket.class, SharinganTogglePacket::encode, SharinganTogglePacket::new, SharinganTogglePacket::handle);

        // 3. Syncing the Sharingan to everyone (Server -> Client)
        INSTANCE.registerMessage(id++, SharinganSyncPacket.class, SharinganSyncPacket::encode, SharinganSyncPacket::new, SharinganSyncPacket::handle);

        // 4. Rinnegan Teleport
        INSTANCE.registerMessage(id++, AmenotejikaraPacket.class, AmenotejikaraPacket::encode, AmenotejikaraPacket::new, AmenotejikaraPacket::handle);

        // 5. Tsukuyomi (Casting on a victim)
        INSTANCE.registerMessage(id++, TsukuyomiPacket.class, TsukuyomiPacket::encode, TsukuyomiPacket::new, TsukuyomiPacket::handle);

        // 6. Tsukuyomi Sync (Telling the caster to show the cross)
        INSTANCE.registerMessage(id++, TsukuyomiSyncPacket.class, TsukuyomiSyncPacket::encode, TsukuyomiSyncPacket::new, TsukuyomiSyncPacket::handle);

        INSTANCE.registerMessage(id++, SyncChakraPacket.class, SyncChakraPacket::encode, SyncChakraPacket::new, SyncChakraPacket::handle);
        INSTANCE.registerMessage(id++, ClearHandSignsPacket.class, ClearHandSignsPacket::encode, ClearHandSignsPacket::new, ClearHandSignsPacket::handle);
    }
}