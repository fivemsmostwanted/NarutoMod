package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.jutsu.JutsuActions;

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
                if (this.slotId == 1 || this.slotId == 2) {
                    if (!stats.isSharinganActive()) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou must activate your Sharingan first!"), true);
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        return;
                    }
                }

                boolean canCast = false;

                if (this.slotId == 1 && stats.hasJutsu("narutomod:shackling_stakes")) canCast = true;
                if (this.slotId == 2 && stats.hasJutsu("narutomod:crow_clone_feint")) canCast = true;

                if (canCast) {
                    JutsuActions.executeInstant(this.slotId, player);
                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou haven't learned this Genjutsu yet!"), true);
                    player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            });
        });
        context.setPacketHandled(true);
    }
}