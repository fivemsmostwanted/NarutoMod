package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncAnimationPacket {
    private final int entityId;
    private final String animationName;
    private final boolean play; // true = play, false = stop

    public SyncAnimationPacket(int entityId, String animationName, boolean play) {
        this.entityId = entityId;
        this.animationName = animationName;
        this.play = play;
    }

    public SyncAnimationPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.animationName = buf.readUtf();
        this.play = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.animationName);
        buf.writeBoolean(this.play);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Safely route to client-only code
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.handleAnimationSync(this.entityId, this.animationName, this.play)
            );
        });
        context.setPacketHandled(true);
    }
}