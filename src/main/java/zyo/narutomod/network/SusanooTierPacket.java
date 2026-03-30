package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.entity.SusanooEntity;

import java.util.Optional;
import java.util.function.Supplier;

public class SusanooTierPacket {
    public SusanooTierPacket() {}
    public SusanooTierPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                Optional<Entity> existingSusanoo = player.getPassengers().stream()
                        .filter(e -> e instanceof SusanooEntity).findFirst();

                if (existingSusanoo.isPresent()) {
                    SusanooEntity susanoo = (SusanooEntity) existingSusanoo.get();
                    int currentTier = susanoo.getTier();
                    int maxTier = 1;
                    if (stats.hasJutsu("narutomod:susanoo_skeletal")) maxTier = 2;
                    if (stats.hasJutsu("narutomod:susanoo_humanoid")) maxTier = 3;
                    if (stats.hasJutsu("narutomod:susanoo_armored")) maxTier = 4;

                    int nextTier = currentTier + 1;
                    if (nextTier > maxTier) {
                        nextTier = 1;
                    }

                    if (nextTier != currentTier) {
                        susanoo.setTier(nextTier);
                        String formName = switch (nextTier) {
                            case 1 -> "Ribcage";
                            case 2 -> "Skeletal";
                            case 3 -> "Humanoid";
                            case 4 -> "Armored";
                            case 5 -> "Perfect";
                            default -> "Unknown";
                        };
                        player.displayClientMessage(Component.literal("§5Susanoo Form: " + formName), true);
                    } else {
                        player.displayClientMessage(Component.literal("§cYou have not unlocked the next Susanoo form yet."), true);
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }
}