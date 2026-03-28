package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.jutsu.JutsuData;
import zyo.narutomod.jutsu.JutsuManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JutsuC2SPacket {
    private final List<Integer> sequence;

    public JutsuC2SPacket(List<Integer> sequence) {
        this.sequence = new ArrayList<>(sequence);
    }

    public JutsuC2SPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.sequence = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.sequence.add(buf.readVarInt());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.sequence.size());
        for (Integer sign : this.sequence) {
            buf.writeVarInt(sign);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            JutsuData castingJutsu = JutsuManager.LOADED_JUTSUS.values().stream()
                    .filter(data -> data.hand_signs.equals(this.sequence))
                    .findFirst()
                    .orElse(null);

            if (castingJutsu != null) {
                player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    if (stats.getChakra() >= castingJutsu.chakra_cost) {
                        float newChakra = stats.getChakra() - castingJutsu.chakra_cost;
                        stats.setChakra(newChakra);

                        PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new SyncChakraPacket(newChakra)
                        );

                        player.displayClientMessage(Component.literal("§bCasting: " + castingJutsu.name + "!"), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_CAST.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        zyo.narutomod.jutsu.JutsuActions.execute(castingJutsu.id, player);

                        PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new ClearHandSignsPacket()
                        );
                    } else {
                        player.displayClientMessage(Component.literal("§cNot enough Chakra! You need " + castingJutsu.chakra_cost), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new ClearHandSignsPacket()
                        );
                    }
                });
            } else {
                boolean isValidPrefix = JutsuManager.LOADED_JUTSUS.values().stream()
                        .anyMatch(data -> data.hand_signs.size() >= this.sequence.size() &&
                                data.hand_signs.subList(0, this.sequence.size()).equals(this.sequence));

                if (!isValidPrefix) {
                    player.displayClientMessage(Component.literal("§cInvalid Hand Sign sequence!"), true);
                    player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                    PacketHandler.INSTANCE.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                            new ClearHandSignsPacket()
                    );
                }
            }
        });
        context.setPacketHandled(true);
    }
}