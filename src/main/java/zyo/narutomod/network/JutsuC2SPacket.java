package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.jutsu.AbstractJutsu;
import zyo.narutomod.jutsu.JutsuData;
import zyo.narutomod.jutsu.JutsuManager;
import zyo.narutomod.jutsu.JutsuRegistry;
import zyo.narutomod.events.ServerEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JutsuC2SPacket {
    private final List<Integer> sequence;

    public JutsuC2SPacket(List<Integer> sequence) { this.sequence = new ArrayList<>(sequence); }
    public JutsuC2SPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.sequence = new ArrayList<>();
        for (int i = 0; i < size; i++) this.sequence.add(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.sequence.size());
        for (Integer sign : this.sequence) buf.writeVarInt(sign);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                var entry = JutsuManager.LOADED_JUTSUS.entrySet().stream()
                        .filter(e -> e.getValue().hand_signs != null && e.getValue().hand_signs.equals(this.sequence))
                        .findFirst()
                        .orElse(null);

                if (entry != null) {
                    ResourceLocation jutsuId = entry.getKey();
                    JutsuData data = entry.getValue();

                    if (!stats.hasJutsu(jutsuId.toString())) {
                        player.displayClientMessage(Component.literal("§cYou haven't learned " + data.name + "!"), true);
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClearHandSignsPacket());
                        return;
                    }

                    if (stats.isOnCooldown(jutsuId.toString())) {
                        player.displayClientMessage(Component.literal("§c" + data.name + " is on cooldown!"), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClearHandSignsPacket());
                        return;
                    }

                    if (stats.getChakra() >= data.chakra_cost) {
                        stats.setChakra(stats.getChakra() - data.chakra_cost);
                        stats.setCooldown(jutsuId.toString(), data.cooldown > 0 ? data.cooldown : 100);

                        AbstractJutsu logic = JutsuRegistry.JUTSUS.get(jutsuId);
                        if (logic != null) {
                            logic.execute(player);
                            player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_CAST.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        }

                        ServerEvents.syncPlayerDataToAllTracking(player);
                        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new ClearHandSignsPacket());
                    } else {
                        player.displayClientMessage(Component.literal("§cInsufficient Chakra!"), true);
                        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new ClearHandSignsPacket());
                    }
                } else {
                    boolean isValidPrefixForKnownJutsu = JutsuManager.LOADED_JUTSUS.entrySet().stream()
                            .anyMatch(e -> stats.hasJutsu(e.getKey().toString()) &&
                                    e.getValue().hand_signs != null &&
                                    e.getValue().hand_signs.size() >= this.sequence.size() &&
                                    e.getValue().hand_signs.subList(0, this.sequence.size()).equals(this.sequence));

                    if (!isValidPrefixForKnownJutsu) {
                        player.displayClientMessage(Component.literal("§cInvalid sequence or locked jutsu!"), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new ClearHandSignsPacket());
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }
}