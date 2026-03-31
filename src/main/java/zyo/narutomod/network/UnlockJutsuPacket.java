package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiData;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.jutsu.JutsuData;
import zyo.narutomod.jutsu.JutsuManager;
import zyo.narutomod.jutsu.JutsuNode;
import zyo.narutomod.jutsu.JutsuTreeManager;
import zyo.narutomod.events.ServerEvents;

import java.util.function.Supplier;

public class UnlockJutsuPacket {
    private final ResourceLocation jutsuId;

    public UnlockJutsuPacket(ResourceLocation jutsuId) {
        this.jutsuId = jutsuId;
    }

    public UnlockJutsuPacket(FriendlyByteBuf buf) {
        this.jutsuId = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(jutsuId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            JutsuNode node = JutsuTreeManager.ALL_NODES.get(this.jutsuId);
            if (node == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (this.jutsuId.getPath().equals("sharingan_root")) {
                    player.displayClientMessage(Component.literal("§cYou cannot force an awakening. It must happen naturally."), true);
                    return;
                }

                if (stats.hasJutsu(jutsuId.toString())) {
                    player.displayClientMessage(Component.literal("§cYou already know this Jutsu!"), true);
                    return;
                }

                if (node.getParent() != null && !stats.hasJutsu(node.getParent().getJutsuId().toString())) {
                    player.displayClientMessage(Component.literal("§cYou must unlock the previous Jutsu first!"), true);
                    return;
                }

                if (node.getRequiredClan() != zyo.narutomod.player.Clan.CLANLESS && stats.getClan() != node.getRequiredClan()) {
                    player.displayClientMessage(Component.literal("§cYour clan cannot learn this!"), true);
                    return;
                }

                JutsuData data = JutsuManager.LOADED_JUTSUS.get(this.jutsuId);
                int cost = data != null ? data.xp_cost : node.getXpCost();
                if (player.experienceLevel >= cost) {
                    stats.unlockJutsu(jutsuId.toString());
                    ShinobiData sData = (ShinobiData) stats;
                    if (jutsuId.getPath().equals("sharingan_2")) {
                        sData.addPermanentChakra(150.0f);
                        stats.setSharinganStage(2);
                    } else if (jutsuId.getPath().equals("sharingan_3")) {
                        sData.addPermanentChakra(250.0f);
                        stats.setSharinganStage(3);
                    } else if (jutsuId.getPath().equals("mangekyou_sharingan")) {
                        sData.addPermanentChakra(250.0f);
                        stats.setSharinganStage(4);
                    } else if (jutsuId.getPath().equals("eternal_mangekyou")) {
                        sData.addPermanentChakra(250.0f);
                        stats.setSharinganStage(5);
                    }

                    ServerEvents.syncPlayerDataToAllTracking(player);
                } else {
                    player.displayClientMessage(Component.literal("§cNot enough XP levels! Need: " + cost), true);
                }
            });
        });
        context.setPacketHandled(true);
    }
}