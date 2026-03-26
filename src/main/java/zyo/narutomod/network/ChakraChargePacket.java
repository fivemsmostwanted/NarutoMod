package zyo.narutomod.network;

import net.minecraft.core.particles.ParticleTypes;
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
                float maxChakra = 100.0f;

                if (currentChakra < maxChakra) {
                    // Give 2 chakra per pulse (You can increase this to charge faster)
                    float newChakra = Math.min(currentChakra + 2.0f, maxChakra);
                    stats.setChakra(newChakra);

                    // Sync the new chakra back to the Client HUD
                    PacketHandler.INSTANCE.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                            new SyncChakraPacket(newChakra)
                    );

                    // Spawn Blue Aura Particles around the player!
                    if (player.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 3; i++) {
                            serverLevel.sendParticles(ParticleTypes.ENCHANT, // Looks like blue magic!
                                    player.getX() + (Math.random() - 0.5),
                                    player.getY() + (Math.random() * 2), // Random height around body
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