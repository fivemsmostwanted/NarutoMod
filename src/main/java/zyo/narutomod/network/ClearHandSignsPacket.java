package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.logic.HandSignManager;
import java.util.function.Supplier;

public class ClearHandSignsPacket {
    public ClearHandSignsPacket() {}
    public ClearHandSignsPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            HandSignManager.clearCombo("Server Packet Received Jutsu done");
        });
        context.setPacketHandled(true);
    }
}