package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.events.ModClientEvents;

import java.util.UUID;
import java.util.function.Supplier;

public class SharinganSyncPacket {
    private final UUID playerId;
    private final boolean isActive;
    private final int stage;

    public SharinganSyncPacket(UUID playerId, boolean isActive, int stage) {
        this.playerId = playerId;
        this.isActive = isActive;
        this.stage = stage;
    }

    public SharinganSyncPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.isActive = buf.readBoolean();
        this.stage = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeBoolean(isActive);
        buf.writeInt(stage);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null) {
                // Update the Capability so the 'R' key reads the correct stage!
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    stats.setSharinganActive(isActive);
                    stats.setSharinganStage(stage);
                });
            }

            // Keep the maps updated for the Renderer/Tints
            ModClientEvents.activeSharingans.put(playerId, isActive);
            ModClientEvents.sharinganStages.put(playerId, stage);
        });
        context.setPacketHandled(true);
    }
}