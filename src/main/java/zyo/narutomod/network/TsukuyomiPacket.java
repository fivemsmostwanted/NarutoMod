package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import zyo.narutomod.capability.ShinobiDataProvider;

import java.util.Optional;
import java.util.function.Supplier;

public class TsukuyomiPacket {
    public TsukuyomiPacket() {}
    public TsukuyomiPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer caster = context.getSender();
            if (caster == null) return;

            double range = 15.0;
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookVec = caster.getLookAngle();
            Vec3 reachVec = eyePos.add(lookVec.scale(range));
            AABB aabb = caster.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0D);

            for (net.minecraft.world.entity.Entity entity : caster.level().getEntities(caster, aabb)) {
                if (entity instanceof LivingEntity target) {
                    AABB entityBB = target.getBoundingBox().inflate(0.3D);
                    Optional<Vec3> hit = entityBB.clip(eyePos, reachVec);

                    if (hit.isPresent()) {
                        caster.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(cStats -> {
                            int attackerGen = cStats.getGenjutsuStat();
                            int victimGen = 0;
                            if (target instanceof ServerPlayer vPlayer) {
                                victimGen = vPlayer.getCapability(ShinobiDataProvider.SHINOBI_DATA)
                                        .map(s -> s.getGenjutsuStat()).orElse(0);
                            }

                            float successChance = 0.75f + ((attackerGen - victimGen) * 0.05f);

                            if (Math.random() < successChance) {
                                int duration = 100 + (attackerGen * 10);

                                float mentalDamage = 2.0f + (attackerGen * 0.5f);
                                target.hurt(caster.damageSources().magic(), mentalDamage);

                                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 255, false, false));
                                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, duration, 1, false, false));

                                target.getPersistentData().putBoolean("TsukuyomiTrapped", true);
                                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> caster), new TsukuyomiSyncPacket(target.getId(), true));

                                caster.getServer().tell(new net.minecraft.server.TickTask(caster.getServer().getTickCount() + duration, () -> {
                                    if (caster.connection != null) {
                                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> caster), new TsukuyomiSyncPacket(target.getId(), false));
                                    }
                                }));

                                caster.displayClientMessage(Component.literal("§4Genjutsu Successful!"), true);
                            } else {
                                caster.displayClientMessage(Component.literal("§7The victim resisted your Genjutsu..."), true);
                                if (target instanceof ServerPlayer) {
                                    ((ServerPlayer)target).displayClientMessage(Component.literal("§aYou resisted a Genjutsu!"), true);
                                }
                            }
                        });
                        break;
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}