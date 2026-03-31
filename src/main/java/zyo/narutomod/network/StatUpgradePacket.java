package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.logic.StatType;
import zyo.narutomod.events.ServerEvents;

import java.util.function.Supplier;

public class StatUpgradePacket {
    private final StatType statToUpgrade;

    public StatUpgradePacket(StatType statToUpgrade) {
        this.statToUpgrade = statToUpgrade;
    }

    public StatUpgradePacket(FriendlyByteBuf buf) {
        this.statToUpgrade = buf.readEnum(StatType.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(statToUpgrade);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                switch (this.statToUpgrade) {
                    case NINJUTSU -> stats.setNinjutsuStat(stats.getNinjutsuStat() + 1);
                    case GENJUTSU -> stats.setGenjutsuStat(stats.getGenjutsuStat() + 1);
                }
                ServerEvents.syncPlayerDataToAllTracking(player);
            });
        });
        context.setPacketHandled(true);
    }
}