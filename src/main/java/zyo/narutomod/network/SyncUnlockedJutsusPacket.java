package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncUnlockedJutsusPacket {
    private final List<String> unlockedJutsus;

    public SyncUnlockedJutsusPacket(List<String> unlockedJutsus) {
        this.unlockedJutsus = unlockedJutsus;
    }

    public SyncUnlockedJutsusPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.unlockedJutsus = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.unlockedJutsus.add(buf.readUtf());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.unlockedJutsus.size());
        for (String jutsu : this.unlockedJutsus) {
            buf.writeUtf(jutsu);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                net.minecraft.client.Minecraft.getInstance().player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    stats.getUnlockedJutsus().clear();
                    for (String jutsu : this.unlockedJutsus) {
                        stats.unlockJutsu(jutsu);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}