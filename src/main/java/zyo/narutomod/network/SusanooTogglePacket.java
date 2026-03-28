package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.entity.ModEntities;
import zyo.narutomod.entity.SusanooEntity;

import java.util.Optional;
import java.util.function.Supplier;

public class SusanooTogglePacket {
    public SusanooTogglePacket() {}
    public SusanooTogglePacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (!stats.isSharinganActive() || stats.getSharinganStage() < 4) {
                    player.displayClientMessage(Component.literal("§cYou need the Mangekyo Sharingan to cast Susanoo!"), true);
                    return;
                }

                Optional<Entity> existingSusanoo = player.getPassengers().stream()
                        .filter(e -> e instanceof SusanooEntity).findFirst();

                if (existingSusanoo.isPresent()) {
                    existingSusanoo.get().discard();
                    player.displayClientMessage(Component.literal("§8Susanoo Deactivated."), true);
                } else {
                    if (stats.getChakra() >= 100.0F) {
                        stats.setChakra(stats.getChakra() - 100.0F);

                        SusanooEntity susanoo = new SusanooEntity(ModEntities.SUSANOO.get(), player.level());
                        susanoo.setOwner(player);

                        susanoo.setPos(player.getX(), player.getY(), player.getZ());
                        susanoo.setYRot(player.getYRot());
                        susanoo.setXRot(player.getXRot());
                        susanoo.yBodyRot = player.yBodyRot;
                        susanoo.yHeadRot = player.yHeadRot;

                        susanoo.startRiding(player, true);
                        player.level().addFreshEntity(susanoo);

                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncChakraPacket(stats.getChakra()));
                        player.displayClientMessage(Component.literal("§5Susanoo Manifested!"), true);
                    } else {
                        player.displayClientMessage(Component.literal("§cNot enough chakra to manifest Susanoo! (Needs 100)"), true);
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }
}