package zyo.narutomod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTsukuyomiPacket {
    private final int targetId;
    private final boolean isTrapped;
    private final int casterId;

    public SyncTsukuyomiPacket(int targetId, boolean isTrapped, int casterId) {
        this.targetId = targetId;
        this.isTrapped = isTrapped;
        this.casterId = casterId;
    }

    public SyncTsukuyomiPacket(FriendlyByteBuf buf) {
        this.targetId = buf.readInt();
        this.isTrapped = buf.readBoolean();
        this.casterId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.targetId);
        buf.writeBoolean(this.isTrapped);
        buf.writeInt(this.casterId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Entity target = Minecraft.getInstance().level.getEntity(this.targetId);
                if (target != null) {
                    target.getPersistentData().putBoolean("TsukuyomiTrapped", this.isTrapped);
                    if (this.isTrapped) {
                        target.getPersistentData().putInt("TsukuyomiCasterId", this.casterId);
                        target.getPersistentData().putFloat("TsukuyomiYaw", target.getYRot());
                        target.getPersistentData().putFloat("TsukuyomiPitch", target.getXRot());
                    } else {
                        target.getPersistentData().remove("TsukuyomiCasterId");
                        target.getPersistentData().remove("TsukuyomiYaw");
                        target.getPersistentData().remove("TsukuyomiPitch");
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}