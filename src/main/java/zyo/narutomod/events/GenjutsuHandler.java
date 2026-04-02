package zyo.narutomod.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.entity.ShacklingStakeEntity;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GenjutsuHandler {

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity.getPersistentData().getBoolean("GenjutsuFrozen")) {
            int ticksLeft = entity.getPersistentData().getInt("FreezeTicks");

            if (ticksLeft > 0) {
                entity.getPersistentData().putInt("FreezeTicks", ticksLeft - 1);
                entity.setDeltaMovement(0, -0.05, 0);

                if (entity instanceof ServerPlayer sp) {
                    double x = entity.getPersistentData().getDouble("FreezeX");
                    double y = entity.getPersistentData().getDouble("FreezeY");
                    double z = entity.getPersistentData().getDouble("FreezeZ");
                    sp.connection.teleport(x, y, z, entity.getYRot(), entity.getXRot());
                }
            } else {
                thawEntity(entity);
            }
        }

        if (entity.getPersistentData().getBoolean("TsukuyomiTrapped")) {
            int tsukuyomiTicks = entity.getPersistentData().getInt("TsukuyomiTicks");

            if (tsukuyomiTicks > 0) {
                entity.getPersistentData().putInt("TsukuyomiTicks", tsukuyomiTicks - 1);

                double lockX = entity.getPersistentData().getDouble("TsukuyomiX");
                double lockY = entity.getPersistentData().getDouble("TsukuyomiY");
                double lockZ = entity.getPersistentData().getDouble("TsukuyomiZ");
                float lockYaw = entity.getPersistentData().getFloat("TsukuyomiYaw");
                float lockPitch = entity.getPersistentData().getFloat("TsukuyomiPitch");

                entity.setDeltaMovement(0, 0, 0);

                if (entity instanceof ServerPlayer sp) {
                    sp.connection.teleport(lockX, lockY, lockZ, lockYaw, lockPitch);
                } else {
                    entity.setPos(lockX, lockY, lockZ);
                    entity.setYRot(lockYaw);
                    entity.setXRot(lockPitch);
                    entity.setYHeadRot(lockYaw);
                }
            } else {
                entity.getPersistentData().putBoolean("TsukuyomiTrapped", false);

                entity.getPersistentData().remove("TsukuyomiTicks");
                entity.getPersistentData().remove("TsukuyomiCasterId");
                entity.getPersistentData().remove("TsukuyomiX");
                entity.getPersistentData().remove("TsukuyomiY");
                entity.getPersistentData().remove("TsukuyomiZ");
                entity.getPersistentData().remove("TsukuyomiYaw");
                entity.getPersistentData().remove("TsukuyomiPitch");

                zyo.narutomod.network.PacketHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new zyo.narutomod.network.SyncTsukuyomiPacket(entity.getId(), false, -1)
                );
            }
        }
    }

    public static float calculateGenjutsuMultiplier(net.minecraft.world.entity.player.Player caster) {
        final float[] multiplier = {1.0f};

        caster.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
            if (stats.getArchetype().name().equals("DESTROYER")) {
                multiplier[0] = 0.65f;
            } else if (stats.getArchetype().name().equals("ILLUSIONIST")) {
                multiplier[0] = 1.35f;
            }
        });

        return multiplier[0];
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getPersistentData().getBoolean("GenjutsuFrozen")) {
            clearStakes(entity);
            entity.getPersistentData().remove("GenjutsuFrozen");
        }
    }

    private static void thawEntity(LivingEntity entity) {
        entity.getPersistentData().remove("GenjutsuFrozen");
        entity.getPersistentData().remove("FreezeTicks");
        entity.getPersistentData().remove("FreezeX");
        entity.getPersistentData().remove("FreezeY");
        entity.getPersistentData().remove("FreezeZ");

        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
            mob.getNavigation().stop();
        }

        clearStakes(entity);
    }

    public static void clearStakes(LivingEntity target) {
        target.level().getEntitiesOfClass(ShacklingStakeEntity.class,
                target.getBoundingBox().inflate(2.0D),
                stake -> true
        ).forEach(Entity::discard);
    }
}