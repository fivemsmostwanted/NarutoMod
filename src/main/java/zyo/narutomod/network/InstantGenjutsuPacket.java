package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.jutsu.JutsuRegistry;

import java.util.function.Supplier;

public class InstantGenjutsuPacket {
    private final int slotId;

    public InstantGenjutsuPacket(int slotId) {
        this.slotId = slotId;
    }

    public InstantGenjutsuPacket(FriendlyByteBuf buf) {
        this.slotId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.slotId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                String equippedJutsuId = stats.getEquippedJutsu(this.slotId);

                if (equippedJutsuId == null) {
                    if (this.slotId == 1 && stats.hasJutsu("narutomod:shackling_stakes")) equippedJutsuId = "narutomod:shackling_stakes";
                    if (this.slotId == 2 && stats.hasJutsu("narutomod:crow_clone_feint")) equippedJutsuId = "narutomod:crow_clone_feint";
                }

                if (equippedJutsuId == null || !stats.hasJutsu(equippedJutsuId)) {
                    player.displayClientMessage(Component.literal("§cNo Jutsu equipped in this slot!"), true);
                    return;
                }

                if (equippedJutsuId.contains("shackling_stakes") || equippedJutsuId.contains("crow_clone")) {
                    if (!stats.isSharinganActive()) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou must activate your Sharingan first!"), true);
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        return;
                    }
                }

                ResourceLocation targetJutsu = ResourceLocation.tryParse(equippedJutsuId);
                if (targetJutsu != null) {
                    JutsuRegistry.executeInstant(targetJutsu, player);
                }
            });
        });
        context.setPacketHandled(true);
    }
}