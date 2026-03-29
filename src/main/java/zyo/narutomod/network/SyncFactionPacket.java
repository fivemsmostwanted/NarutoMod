package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Village;

import java.util.function.Supplier;

public class SyncFactionPacket {
    private final String clanName;
    private final String villageName;

    public SyncFactionPacket(Clan clan, Village village) {
        this.clanName = clan.name();
        this.villageName = village.name();
    }

    public SyncFactionPacket(FriendlyByteBuf buf) {
        this.clanName = buf.readUtf();
        this.villageName = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.clanName);
        buf.writeUtf(this.villageName);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                net.minecraft.client.Minecraft.getInstance().player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    try {
                        stats.setClan(Clan.valueOf(this.clanName));
                        stats.setVillage(Village.valueOf(this.villageName));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Received invalid Clan or Village from server sync.");
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}