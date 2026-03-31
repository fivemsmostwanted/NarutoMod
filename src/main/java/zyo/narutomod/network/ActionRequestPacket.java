package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.network.handler.ActionRegistry;

import java.util.function.Supplier;

public class ActionRequestPacket {

    public enum Action {
        CHAKRA_CHARGE,
        TOGGLE_SHARINGAN,
        EVOLVE_SHARINGAN,
        TOGGLE_SUSANOO,
        TIER_SUSANOO,
        TSUKUYOMI,
        AMENOTEJIKARA
    }

    private final Action action;
    private final int payload;

    public ActionRequestPacket(Action action) { this(action, 0); }
    public ActionRequestPacket(Action action, int payload) {
        this.action = action;
        this.payload = payload;
    }

    public ActionRequestPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
        this.payload = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
        buf.writeInt(this.payload);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                ActionRegistry.getHandler(this.action).execute(player, stats, this.payload);
            });
        });
        context.setPacketHandled(true);
    }
}