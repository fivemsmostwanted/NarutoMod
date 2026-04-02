package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class AnimationC2SPacket {
    private final String animationName;
    private final boolean play;

    public AnimationC2SPacket(String animationName, boolean play) {
        this.animationName = animationName;
        this.play = play;
    }

    public AnimationC2SPacket(FriendlyByteBuf buf) {
        this.animationName = buf.readUtf();
        this.play = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.animationName);
        buf.writeBoolean(this.play);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // The server receives this and instantly megaphones it to everyone tracking you
                PacketHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> player),
                        new SyncAnimationPacket(player.getId(), this.animationName, this.play)
                );
            }
        });
        context.setPacketHandled(true);
    }
}