package zyo.narutomod.jutsu;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.entity.*;

import java.util.HashMap;
import java.util.Map;

public class JutsuRegistry {
    public static final Map<ResourceLocation, AbstractJutsu> JUTSUS = new HashMap<>();
    public static final Map<ResourceLocation, AbstractJutsu> INSTANT_JUTSUS = new HashMap<>();

    public static void registerAll() {
        JUTSUS.put(rl("fireball"), new FireballJutsu());
        JUTSUS.put(rl("substitution"), new SubstitutionJutsu());
        JUTSUS.put(rl("shadow_clone"), new ShadowCloneJutsu());

        INSTANT_JUTSUS.put(rl("shackling_stakes"), new ParalysisGenjutsu());
        INSTANT_JUTSUS.put(rl("crow_clone_feint"), new CrowFeintPrepJutsu());
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, path);
    }

    public static void executeInstant(ResourceLocation id, ServerPlayer player) {
        AbstractJutsu action = INSTANT_JUTSUS.get(id);
        if (action != null) action.execute(player);
    }

    public static class FireballJutsu extends AbstractJutsu {
        @Override
        public void execute(ServerPlayer player) {
            FireballJutsuEntity fireball = new FireballJutsuEntity(player.level(), player);
            fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(fireball);
        }
    }

    public static class SubstitutionJutsu extends AbstractJutsu {
        @Override
        public void execute(ServerPlayer player) {
            double oldX = player.getX();
            double oldY = player.getY();
            double oldZ = player.getZ();

            double radius = 5.0 + (Math.random() * 3.0);
            double angle = Math.random() * Math.PI * 2;

            double newX = oldX + (Math.cos(angle) * radius);
            double newZ = oldZ + (Math.sin(angle) * radius);

            double safeY = oldY;
            net.minecraft.core.BlockPos targetPos = new net.minecraft.core.BlockPos((int)newX, (int)oldY, (int)newZ);

            while (!player.level().getBlockState(targetPos).isAir() && targetPos.getY() < player.level().getMaxBuildHeight()) {
                targetPos = targetPos.above();
                safeY = targetPos.getY();
            }

            player.teleportTo(newX, safeY, newZ);

            SubstitutionLogEntity log = new SubstitutionLogEntity(ModEntities.SUBSTITUTION_LOG.get(), player.level());
            log.moveTo(oldX, oldY, oldZ, player.getYRot(), 0);
            log.setDeltaMovement(0, 0.1, 0);
            log.hasImpulse = true;
            player.level().addFreshEntity(log);

            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, oldX, oldY + 1, oldZ, 15, 0.2, 0.5, 0.2, 0.05);
                serverLevel.sendParticles(ParticleTypes.POOF, newX, safeY + 1, newZ, 15, 0.2, 0.5, 0.2, 0.05);
                player.level().playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public static class ShadowCloneJutsu extends AbstractJutsu {
        @Override
        public void execute(ServerPlayer player) {
            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.isCloneInfusionReady()) {
                    performCrowFeint(player);
                    stats.setCloneInfusionReady(false);
                    return;
                }
                ServerLevel level = player.serverLevel();
                CrowCloneEntity clone = ModEntities.SHADOW_CLONE.get().create(level);
                if (clone != null) {
                    clone.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    clone.setOwner(player);
                    level.addFreshEntity(clone);
                    level.sendParticles(ParticleTypes.POOF, clone.getX(), clone.getY() + 1.0D, clone.getZ(), 15, 0.5D, 1.0D, 0.5D, 0.05D);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eShadow Clone Jutsu!"), true);
                }
            });
        }

        private void performCrowFeint(ServerPlayer player) {
            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                ServerLevel level = player.serverLevel();
                CrowCloneEntity clone = ModEntities.SHADOW_CLONE.get().create(level);
                if (clone != null) {
                    clone.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    clone.setOwner(player);
                    level.addFreshEntity(clone);

                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));
                    double bX = player.getX() - (player.getLookAngle().x * 4.0);
                    double bZ = player.getZ() - (player.getLookAngle().z * 4.0);
                    player.teleportTo(bX, player.getY(), bZ);

                    level.sendParticles(ParticleTypes.POOF, clone.getX(), clone.getY() + 1.0D, clone.getZ(), 15, 0.5D, 1.0D, 0.5D, 0.05D);
                    level.playSound(null, clone.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            });
        }
    }

    public static class CrowFeintPrepJutsu extends AbstractJutsu {
        @Override
        public void execute(ServerPlayer player) {
            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                stats.setCloneInfusionReady(true);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§4Genjutsu Infusion Ready..."), true);
            });
        }
    }

    public static class ParalysisGenjutsu extends AbstractJutsu {
        @Override
        public void execute(ServerPlayer player) {
            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                double range = 15.0;
                net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
                net.minecraft.world.phys.Vec3 lookVector = player.getLookAngle();
                net.minecraft.world.phys.Vec3 traceEnd = eyePos.add(lookVector.scale(range));
                net.minecraft.world.phys.AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(range)).inflate(1.0D);

                net.minecraft.world.entity.LivingEntity target = null;
                double closestDistance = range;

                for (net.minecraft.world.entity.Entity entity : player.level().getEntities(player, searchBox)) {
                    if (entity instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                        net.minecraft.world.phys.AABB targetBox = livingTarget.getBoundingBox().inflate(0.3D);
                        java.util.Optional<net.minecraft.world.phys.Vec3> hitResult = targetBox.clip(eyePos, traceEnd);

                        if (hitResult.isPresent()) {
                            double distanceToHit = eyePos.distanceTo(hitResult.get());
                            if (distanceToHit < closestDistance) {
                                target = livingTarget;
                                closestDistance = distanceToHit;
                            }
                        }
                    }
                }

                if (target != null) {
                    int duration = 60 + (stats.getGenjutsuStat() * 20);
                    target.getPersistentData().putBoolean("GenjutsuFrozen", true);
                    target.getPersistentData().putInt("FreezeTicks", duration);
                    target.getPersistentData().putDouble("FreezeX", target.getX());
                    target.getPersistentData().putDouble("FreezeY", target.getY());
                    target.getPersistentData().putDouble("FreezeZ", target.getZ());

                    if (target instanceof net.minecraft.world.entity.Mob mob) {
                        mob.setNoAi(true);
                    }
                    target.setDeltaMovement(0, 0, 0);
                    target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false, true));

                    float[] spikePitches = {45.0F, 30.0F, 60.0F};
                    float[] spikeYaws = {0.0F, 120.0F, 240.0F};

                    for (int i = 0; i < 4; i++) {
                        ShacklingStakeEntity spike = ModEntities.SHACKLING_STAKE.get().create(player.level());
                        if (spike != null) {
                            spike.moveTo(target.getX(), target.getY() + 1.0D, target.getZ(), target.getYRot() + spikeYaws[i], spikePitches[i]);
                            spike.getPersistentData().putInt("Lifespan", duration);
                            player.level().addFreshEntity(spike);
                        }
                    }

                    player.level().playSound(null, target.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§Target caught in Shackling Stakes for " + (duration/20) + "s."), true);
                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNo target in sight."), true);
                }
            });
        }
    }
}