package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TsukuyomiSyncPacket {
    private final int victimId;
    private final int casterId;
    private final boolean isTrapped;
    private final float lockYaw;
    private final float lockPitch;

    public TsukuyomiSyncPacket(int victimId, int casterId, boolean isTrapped, float lockYaw, float lockPitch) {
        this.victimId = victimId;
        this.casterId = casterId;
        this.isTrapped = isTrapped;
        this.lockYaw = lockYaw;
        this.lockPitch = lockPitch;
    }

    public TsukuyomiSyncPacket(FriendlyByteBuf buf) {
        this.victimId = buf.readInt();
        this.casterId = buf.readInt();
        this.isTrapped = buf.readBoolean();
        this.lockYaw = buf.readFloat();
        this.lockPitch = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(victimId);
        buf.writeInt(casterId);
        buf.writeBoolean(isTrapped);
        buf.writeFloat(lockYaw);
        buf.writeFloat(lockPitch);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            net.minecraft.client.player.LocalPlayer localPlayer = net.minecraft.client.Minecraft.getInstance().player;
            if (localPlayer == null) return;

            if (localPlayer.getId() == victimId) {
                localPlayer.getPersistentData().putBoolean("TsukuyomiTrapped", isTrapped);
                if (isTrapped) {
                    localPlayer.getPersistentData().putInt("TsukuyomiCasterId", casterId);
                    localPlayer.getPersistentData().putFloat("TsukuyomiYaw", lockYaw);
                    localPlayer.getPersistentData().putFloat("TsukuyomiPitch", lockPitch);
                } else {
                    localPlayer.getPersistentData().remove("TsukuyomiCasterId");
                    localPlayer.getPersistentData().remove("TsukuyomiYaw");
                    localPlayer.getPersistentData().remove("TsukuyomiPitch");
                }
            }

            if (net.minecraft.client.Minecraft.getInstance().level != null) {
                net.minecraft.world.entity.Entity victim = net.minecraft.client.Minecraft.getInstance().level.getEntity(victimId);
                if (victim instanceof net.minecraft.world.entity.LivingEntity target) {
                    target.getPersistentData().putBoolean("TsukuyomiTrapped", isTrapped);
                    if (isTrapped) {
                        target.getPersistentData().putInt("TsukuyomiCasterId", casterId);
                        target.getPersistentData().putFloat("TsukuyomiYaw", lockYaw);
                        target.getPersistentData().putFloat("TsukuyomiPitch", lockPitch);
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