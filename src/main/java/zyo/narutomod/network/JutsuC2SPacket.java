package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
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
                    .filter(data -> data.hand_signs != null && data.hand_signs.equals(this.sequence))
                    .findFirst()
                    .orElse(null);

            if (castingJutsu != null) {
                net.minecraft.resources.ResourceLocation jutsuKey = JutsuManager.LOADED_JUTSUS.entrySet().stream()
                        .filter(entry -> entry.getValue() == castingJutsu)
                        .map(java.util.Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    if (jutsuKey != null && zyo.narutomod.jutsu.JutsuTreeManager.ALL_NODES.containsKey(jutsuKey)) {
                        if (!stats.hasJutsu(jutsuKey.toString())) {
                            player.displayClientMessage(Component.literal("§cYou haven't learned this Jutsu yet!"), true);
                            player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                            zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                    new ClearHandSignsPacket()
                            );
                            return;
                        }
                    }

                    String jId = jutsuKey != null ? jutsuKey.toString() : String.valueOf(castingJutsu.id);
                    if (stats.isOnCooldown(jId)) {
                        player.displayClientMessage(Component.literal("§c" + castingJutsu.name + " is on cooldown!"), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new ClearHandSignsPacket()
                        );
                        return;
                    }

                    int masteryXP = stats.getNatureMastery(castingJutsu.nature);
                    int signsToSkip = masteryXP / 200;
                    int originalLength = castingJutsu.hand_signs.size();
                    int requiredLength = Math.max(1, originalLength - signsToSkip);
                    java.util.List<Integer> masteredSequence = castingJutsu.hand_signs.subList(0, requiredLength);
                    if (!this.sequence.equals(masteredSequence)) {
                        player.displayClientMessage(Component.literal("§cInvalid Hand Sign sequence!"), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new ClearHandSignsPacket()
                        );
                        return;
                    }

                    if (stats.getChakra() >= castingJutsu.chakra_cost) {
                        float newChakra = stats.getChakra() - castingJutsu.chakra_cost;
                        stats.setChakra(newChakra);

                        int cd = castingJutsu.cooldown > 0 ? castingJutsu.cooldown : 100;
                        stats.setCooldown(jId, cd);
                        ServerEvents.syncPlayerDataToAllTracking(player);

                        player.displayClientMessage(Component.literal("§bCasting: " + castingJutsu.name + "!"), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_CAST.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        AbstractJutsu logic = JutsuRegistry.JUTSUS.get(castingJutsu.id);
                        if (logic != null) {
                            logic.execute(player);
                        } else {
                            player.displayClientMessage(Component.literal("§4Error: Jutsu logic not registered!"), false);
                        }

                        zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new ClearHandSignsPacket()
                        );
                    } else {
                        player.displayClientMessage(Component.literal("§cNot enough Chakra! You need " + castingJutsu.chakra_cost), true);
                        player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new ClearHandSignsPacket()
                        );
                    }
                });
            } else {
                boolean isValidPrefix = JutsuManager.LOADED_JUTSUS.values().stream()
                        .anyMatch(data -> data.hand_signs != null &&
                                data.hand_signs.size() >= this.sequence.size() &&
                                data.hand_signs.subList(0, this.sequence.size()).equals(this.sequence));

                if (!isValidPrefix) {
                    player.displayClientMessage(Component.literal("§cInvalid Hand Sign sequence!"), true);
                    player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                    zyo.narutomod.network.PacketHandler.INSTANCE.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                            new ClearHandSignsPacket()
                    );
                }
            }
        });
        context.setPacketHandled(true);
    }
}