package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.entity.SusanooEntity;

import java.util.Optional;
import java.util.function.Supplier;

public class SusanooTierPacket {
    public SusanooTierPacket() {}
    public SusanooTierPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Optional<Entity> existingSusanoo = player.getPassengers().stream()
                    .filter(e -> e instanceof SusanooEntity).findFirst();

            if (existingSusanoo.isPresent()) {
                SusanooEntity susanoo = (SusanooEntity) existingSusanoo.get();

                int currentTier = susanoo.getTier();
                int nextTier = (currentTier == 1) ? 2 : 1;

                susanoo.setTier(nextTier);
                player.displayClientMessage(Component.literal("§5Susanoo Tier " + nextTier), true);
            }
        });
        context.setPacketHandled(true);
    }
}