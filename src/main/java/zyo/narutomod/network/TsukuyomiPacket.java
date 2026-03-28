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
            AABB aabb = caster.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0D);

            boolean targetHit = false;

            for (net.minecraft.world.entity.Entity entity : caster.level().getEntities(caster, aabb)) {
                if (entity instanceof LivingEntity target) {

                    AABB entityBB = target.getBoundingBox().inflate(0.5D);
                    Optional<Vec3> hit = entityBB.clip(eyePos, reachVec);

                    if (hit.isPresent()) {
                        targetHit = true;

                        caster.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(cStats -> {
                            int attackerGen = cStats.getGenjutsuStat();
                            int victimGen = 0;
                            if (target instanceof ServerPlayer vPlayer) {
                                victimGen = vPlayer.getCapability(ShinobiDataProvider.SHINOBI_DATA)
                                        .map(s -> s.getGenjutsuStat()).orElse(0);
                            }

                            float successChance = 0.75f + ((attackerGen - victimGen) * 0.05f);

                            if (Math.random() <= successChance) {
                                int duration = 100 + (attackerGen * 10);
                                float mentalDamage = 2.0f + (attackerGen * 0.5f);

                                target.hurt(caster.damageSources().magic(), mentalDamage);

                                double dX = caster.getX() - target.getX();
                                double dZ = caster.getZ() - target.getZ();
                                float lockYaw = (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90.0F);
                                float lockPitch = 0.0F;

                                target.getPersistentData().putBoolean("TsukuyomiTrapped", true);
                                target.getPersistentData().putInt("TsukuyomiCasterId", caster.getId());
                                target.getPersistentData().putInt("TsukuyomiTicks", duration);
                                target.getPersistentData().putDouble("TsukuyomiX", target.getX());
                                target.getPersistentData().putDouble("TsukuyomiY", target.getY());
                                target.getPersistentData().putDouble("TsukuyomiZ", target.getZ());
                                target.getPersistentData().putFloat("TsukuyomiYaw", lockYaw);
                                target.getPersistentData().putFloat("TsukuyomiPitch", lockPitch);

                                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> caster), new TsukuyomiSyncPacket(target.getId(), caster.getId(), true, lockYaw, lockPitch));

                                if (target instanceof ServerPlayer victimPlayer) {
                                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> victimPlayer), new TsukuyomiSyncPacket(target.getId(), caster.getId(), true, lockYaw, lockPitch));
                                }

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

            if (!targetHit) {
                caster.displayClientMessage(Component.literal("§cNo target found."), true);
            }
        });
        context.setPacketHandled(true);
    }
}