package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;

import java.util.function.Supplier;

public class SyncStatsPacket {
    private final int ninjutsu;
    private final int genjutsu;

    public SyncStatsPacket(int ninjutsu, int genjutsu) {
        this.ninjutsu = ninjutsu;
        this.genjutsu = genjutsu;
    }

    public SyncStatsPacket(FriendlyByteBuf buf) {
        this.ninjutsu = buf.readInt();
        this.genjutsu = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(ninjutsu);
        buf.writeInt(genjutsu);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    stats.setNinjutsuStat(this.ninjutsu);
                    stats.setGenjutsuStat(this.genjutsu);
                });
            }
        });
        context.setPacketHandled(true);
    }
}