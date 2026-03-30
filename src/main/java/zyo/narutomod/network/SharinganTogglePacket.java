package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.events.ServerEvents;

import java.util.function.Supplier;

public class SharinganTogglePacket {
    private final boolean activate;
    private final int stage;

    public SharinganTogglePacket(boolean activate, int stage) {
        this.activate = activate;
        this.stage = stage;
    }

    public SharinganTogglePacket(FriendlyByteBuf buf) {
        this.activate = buf.readBoolean();
        this.stage = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(activate);
        buf.writeInt(stage);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.getSharinganStage() > 0) {
                    stats.setSharinganActive(this.activate);
                    stats.setSharinganStage(this.stage);

                    if (this.activate) {
                        int amplifier = (this.stage >= 4) ? 1 : 0;
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, amplifier, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, amplifier, false, false));
                    } else {
                        player.removeEffect(MobEffects.MOVEMENT_SPEED);
                        player.removeEffect(MobEffects.DAMAGE_BOOST);

                        player.getPassengers().stream()
                                .filter(e -> e instanceof zyo.narutomod.entity.SusanooEntity)
                                .forEach(net.minecraft.world.entity.Entity::discard);
                    }

                    ServerEvents.activeSharingans.put(player.getUUID(), this.activate);
                    ServerEvents.sharinganStages.put(player.getUUID(), this.stage);

                    PacketHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                            new SharinganSyncPacket(player.getUUID(), this.activate, this.stage)
                    );
                }
            });
        });
        context.setPacketHandled(true);
    }
}