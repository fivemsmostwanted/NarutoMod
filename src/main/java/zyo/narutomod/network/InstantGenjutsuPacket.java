package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.jutsu.JutsuActions;

import java.util.function.Supplier;

public class InstantGenjutsuPacket {
    private final int slotId;

    public InstantGenjutsuPacket(int slotId) {
        this.slotId = slotId;
    }

    public InstantGenjutsuPacket(FriendlyByteBuf buf) {
        this.slotId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slotId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.isSharinganActive() && stats.getSharinganStage() >= 3) {
                    JutsuActions.executeInstant(this.slotId, player);
                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYour eyes are not mature enough for this..."), true);
                }
            });
        });
        context.setPacketHandled(true);
    }
}