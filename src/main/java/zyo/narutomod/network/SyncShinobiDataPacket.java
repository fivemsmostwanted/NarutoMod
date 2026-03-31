package zyo.narutomod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;

import java.util.function.Supplier;

public class SyncShinobiDataPacket {
    private final int entityId;
    private final CompoundTag data;

    public SyncShinobiDataPacket(int entityId, CompoundTag data) {
        this.entityId = entityId;
        this.data = data;
    }

    public SyncShinobiDataPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.data = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                net.minecraft.world.entity.Entity entity = mc.level.getEntity(this.entityId);
                if (entity instanceof net.minecraft.world.entity.player.Player player) {
                    player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                        stats.loadNBTData(this.data);
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }
}