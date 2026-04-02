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

        // Core Data Synchronization
        INSTANCE.registerMessage(id++, SyncShinobiDataPacket.class, SyncShinobiDataPacket::encode, SyncShinobiDataPacket::new, SyncShinobiDataPacket::handle);

        // Unified Action System
        INSTANCE.registerMessage(id++, ActionRequestPacket.class, ActionRequestPacket::encode, ActionRequestPacket::new, ActionRequestPacket::handle);
        INSTANCE.registerMessage(id++, SyncTsukuyomiPacket.class, SyncTsukuyomiPacket::encode, SyncTsukuyomiPacket::new, SyncTsukuyomiPacket::handle);
        INSTANCE.registerMessage(id++, AnimationC2SPacket.class, AnimationC2SPacket::encode, AnimationC2SPacket::new, AnimationC2SPacket::handle);
        INSTANCE.registerMessage(id++, SyncAnimationPacket.class, SyncAnimationPacket::encode, SyncAnimationPacket::new, SyncAnimationPacket::handle);

        // Jutsu & Combat Logic
        INSTANCE.registerMessage(id++, JutsuC2SPacket.class, JutsuC2SPacket::encode, JutsuC2SPacket::new, JutsuC2SPacket::handle);
        INSTANCE.registerMessage(id++, ClearHandSignsPacket.class, ClearHandSignsPacket::encode, ClearHandSignsPacket::new, ClearHandSignsPacket::handle);
        INSTANCE.registerMessage(id++, InstantGenjutsuPacket.class, InstantGenjutsuPacket::encode, InstantGenjutsuPacket::new, InstantGenjutsuPacket::handle);
        INSTANCE.registerMessage(id++, UnlockJutsuPacket.class, UnlockJutsuPacket::encode, UnlockJutsuPacket::new, UnlockJutsuPacket::handle);

        // ---> THIS IS THE MISSING LINE! <---
        INSTANCE.registerMessage(id++, SyncJutsuRegistryPacket.class, SyncJutsuRegistryPacket::encode, SyncJutsuRegistryPacket::new, SyncJutsuRegistryPacket::handle);

        // Menu & Progression Systems
        INSTANCE.registerMessage(id++, StatUpgradePacket.class, StatUpgradePacket::encode, StatUpgradePacket::new, StatUpgradePacket::handle);
        INSTANCE.registerMessage(id++, OpenSetupScreenPacket.class, OpenSetupScreenPacket::encode, OpenSetupScreenPacket::new, OpenSetupScreenPacket::handle);
        INSTANCE.registerMessage(id++, SetPlayerFactionPacket.class, SetPlayerFactionPacket::encode, SetPlayerFactionPacket::new, SetPlayerFactionPacket::handle);
    }
}