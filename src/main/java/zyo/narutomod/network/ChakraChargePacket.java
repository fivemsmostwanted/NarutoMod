package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;

import java.util.function.Supplier;

public class ChakraChargePacket {
    public ChakraChargePacket() {}

    public ChakraChargePacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                float currentChakra = stats.getChakra();
                float maxChakra = stats.getMaxChakra();

                if (currentChakra < maxChakra) {
                    float newChakra = Math.min(currentChakra + 2.0f, maxChakra);
                    stats.setChakra(newChakra);

                    PacketHandler.INSTANCE.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                            new SyncChakraPacket(newChakra)
                    );

                    if (player.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 3; i++) {
                            serverLevel.sendParticles(zyo.narutomod.particle.ModParticles.CUSTOM_CHAKRA.get(),
                                    player.getX() + (Math.random() - 0.5),
                            player.getY() + (Math.random() * 2),
                            player.getZ() + (Math.random() - 0.5),
                            1, 0.0, 0.1, 0.0, 0.1);
                        }
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }
}