package zyo.narutomod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;

import java.util.function.Supplier;

public class SyncChakraPacket {
    private final float chakra;

    public SyncChakraPacket(float chakra) {
        this.chakra = chakra;
    }

    public SyncChakraPacket(FriendlyByteBuf buf) {
        this.chakra = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(chakra);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                net.minecraft.client.Minecraft.getInstance().player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    stats.setChakra(this.chakra);
                });

                // NEW: The Jutsu was successful! Clear the hand signs automatically.
                zyo.narutomod.logic.HandSignManager.currentSequence.clear();
            }
        });
        context.setPacketHandled(true);
    }
}