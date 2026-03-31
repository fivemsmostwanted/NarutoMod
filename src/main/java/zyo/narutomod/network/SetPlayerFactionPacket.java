package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Village;
import zyo.narutomod.events.ServerEvents;
import java.util.function.Supplier;

public class SetPlayerFactionPacket {
    private final String clanName;
    private final String villageName;
    private final String initialNature; // Added nature field

    public SetPlayerFactionPacket(Clan clan, Village village, String nature) {
        this.clanName = clan.name();
        this.villageName = village.name();
        this.initialNature = nature;
    }

    public SetPlayerFactionPacket(FriendlyByteBuf buf) {
        this.clanName = buf.readUtf();
        this.villageName = buf.readUtf();
        this.initialNature = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.clanName);
        buf.writeUtf(this.villageName);
        buf.writeUtf(this.initialNature);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                try {
                    stats.setClan(Clan.valueOf(this.clanName));
                    stats.setVillage(Village.valueOf(this.villageName));
                    stats.addNatureMastery(this.initialNature, 1);
                    String rootId = "narutomod:root_" + this.initialNature.toLowerCase();
                    stats.unlockJutsu(rootId);

                    player.displayClientMessage(Component.literal("§aWelcome to the Shinobi World!"), true);

                    ServerEvents.syncPlayerDataToAllTracking(player);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid setup data submitted by " + player.getName().getString());
                }
            });
        });
        context.setPacketHandled(true);
    }
}