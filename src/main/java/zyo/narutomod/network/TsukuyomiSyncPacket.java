package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TsukuyomiSyncPacket {
    private final int victimId;
    private final boolean isTrapped;

    public TsukuyomiSyncPacket(int victimId, boolean isTrapped) {
        this.victimId = victimId;
        this.isTrapped = isTrapped;
    }

    public TsukuyomiSyncPacket(FriendlyByteBuf buf) {
        this.victimId = buf.readInt();
        this.isTrapped = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(victimId);
        buf.writeBoolean(isTrapped);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().level != null) {
                net.minecraft.world.entity.Entity entity = net.minecraft.client.Minecraft.getInstance().level.getEntity(victimId);

                if (entity instanceof net.minecraft.world.entity.LivingEntity target) {
                    if (isTrapped) {
                        System.out.println("Applying NBT Tag to: " + target.getName().getString());
                        target.getPersistentData().putBoolean("TsukuyomiTrapped", true);
                    } else {
                        target.getPersistentData().putBoolean("TsukuyomiTrapped", false);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}