package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Village;

import java.util.function.Supplier;

public class SetPlayerFactionPacket {
    private final String clanName;
    private final String villageName;

    public SetPlayerFactionPacket(Clan clan, Village village) {
        this.clanName = clan.name();
        this.villageName = village.name();
    }

    public SetPlayerFactionPacket(FriendlyByteBuf buf) {
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
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                try {
                    stats.setClan(Clan.valueOf(this.clanName));
                    stats.setVillage(Village.valueOf(this.villageName));
                    player.displayClientMessage(Component.literal("§aWelcome to the Shinobi World!"), true);

                    PacketHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new SyncStatsPacket(stats.getNinjutsuStat(), stats.getGenjutsuStat())
                    );

                    PacketHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new SyncFactionPacket(stats.getClan(), stats.getVillage())
                    );

                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid Clan or Village submitted by " + player.getName().getString());
                }
            });
        });
        context.setPacketHandled(true);
    }
}