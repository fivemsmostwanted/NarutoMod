package zyo.narutomod.jutsu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.entity.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JutsuRegistry {
    public static final Map<ResourceLocation, AbstractJutsu> JUTSUS = new HashMap<>();
    public static final Map<ResourceLocation, AbstractJutsu> INSTANT_JUTSUS = new HashMap<>();

    public static void registerAll() {
        // Standard / Combo Jutsus
        JUTSUS.put(rl("fireball"), new FireballJutsu());
        JUTSUS.put(rl("chidori"), new ChidoriJutsu());
        JUTSUS.put(rl("substitution"), new SubstitutionJutsu());
        JUTSUS.put(rl("shadow_clone"), new ShadowCloneJutsu());

        // Dojutsu Actions (Triggered by Hotkeys)
        JUTSUS.put(rl("tsukuyomi"), new TsukuyomiJutsu());
        JUTSUS.put(rl("amenotejikara"), new AmenotejikaraJutsu());

        // Genjutsu Slots (Triggered by Alt+X)
        INSTANT_JUTSUS.put(rl("shackling_stakes"), new ParalysisGenjutsu());
        INSTANT_JUTSUS.put(rl("crow_clone_feint"), new CrowFeintPrepJutsu());
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, path);
    }

    public static void executeInstant(ResourceLocation id, ServerPlayer player) {
        AbstractJutsu action = INSTANT_JUTSUS.get(id);
        if (action != null) {
            action.tryExecute(player, id.toString());
        }
    }

    public static class FireballJutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(ServerPlayer player, IShinobiData stats) {
            FireballJutsuEntity fireball = new FireballJutsuEntity(player.level(), player);
            fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(fireball);
            return true;
        }
    }

    public static class SubstitutionJutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(ServerPlayer player, IShinobiData stats) {
            double oldX = player.getX();
            double oldY = player.getY();
            double oldZ = player.getZ();

            double radius = 5.0 + (Math.random() * 3.0);
            double angle = Math.random() * Math.PI * 2;
            double newX = oldX + (Math.cos(angle) * radius);
            double newZ = oldZ + (Math.sin(angle) * radius);

            double safeY = oldY;
            BlockPos targetPos = new BlockPos((int)newX, (int)oldY, (int)newZ);

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

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, oldX, oldY + 1, oldZ, 15, 0.2, 0.5, 0.2, 0.05);
                serverLevel.sendParticles(ParticleTypes.POOF, newX, safeY + 1, newZ, 15, 0.2, 0.5, 0.2, 0.05);
                player.level().playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return true;
        }
    }

    public static class ShadowCloneJutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(ServerPlayer player, IShinobiData stats) {
            if (stats.isCloneInfusionReady()) {
                performCrowFeint(player);
                stats.setCloneInfusionReady(false);
                return true;
            }
            ServerLevel level = player.serverLevel();
            CrowCloneEntity clone = ModEntities.SHADOW_CLONE.get().create(level);
            if (clone != null) {
                clone.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                clone.setOwner(player);
                level.addFreshEntity(clone);
                level.sendParticles(ParticleTypes.POOF, clone.getX(), clone.getY() + 1.0D, clone.getZ(), 15, 0.5D, 1.0D, 0.5D, 0.05D);
                player.displayClientMessage(Component.literal("§eShadow Clone Jutsu!"), true);
                return true;
            }
            return false;
        }

        private void performCrowFeint(ServerPlayer player) {
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
        }
    }

    public static class CrowFeintPrepJutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(ServerPlayer player, IShinobiData stats) {
            stats.setCloneInfusionReady(true);
            player.displayClientMessage(Component.literal("§4Genjutsu Infusion Ready..."), true);
            return true;
        }
    }

    public static class ParalysisGenjutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(ServerPlayer player, IShinobiData stats) {
            double range = 15.0;
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 traceEnd = eyePos.add(lookVector.scale(range));
            AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(range)).inflate(1.0D);

            LivingEntity target = null;
            double closestDistance = range;

            for (Entity entity : player.level().getEntities(player, searchBox)) {
                if (entity instanceof LivingEntity livingTarget) {
                    AABB targetBox = livingTarget.getBoundingBox().inflate(0.3D);
                    Optional<Vec3> hitResult = targetBox.clip(eyePos, traceEnd);

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
                player.displayClientMessage(Component.literal("§cTarget caught in Shackling Stakes for " + (duration/20) + "s."), true);
                return true;
            } else {
                player.displayClientMessage(Component.literal("§cNo target in sight."), true);
                return false;
            }
        }
    }

    public static class TsukuyomiJutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(ServerPlayer player, IShinobiData stats) {
            if (!stats.isSharinganActive() || stats.getSharinganStage() < 4) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou need the Mangekyou Sharingan active to use this!"), true);
                return false;
            }

            double range = 15.0;
            net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
            net.minecraft.world.phys.Vec3 lookVec = player.getLookAngle();
            net.minecraft.world.phys.Vec3 reachVec = eyePos.add(lookVec.scale(range));
            net.minecraft.world.phys.AABB aabb = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0D);

            for (net.minecraft.world.entity.Entity entity : player.level().getEntities(player, aabb)) {
                if (entity instanceof net.minecraft.world.entity.LivingEntity target) {
                    if (target.getBoundingBox().inflate(0.5D).clip(eyePos, reachVec).isPresent()) {
                        int attackerGen = stats.getGenjutsuStat();
                        int victimGen = target.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).map(IShinobiData::getGenjutsuStat).orElse(0);

                        if (Math.random() <= (0.75f + ((attackerGen - victimGen) * 0.05f))) {
                            int duration = 100 + (attackerGen * 10);

                            target.getPersistentData().putBoolean("TsukuyomiTrapped", true);
                            target.getPersistentData().putInt("TsukuyomiTicks", duration);
                            target.getPersistentData().putDouble("TsukuyomiX", target.getX());
                            target.getPersistentData().putDouble("TsukuyomiY", target.getY());
                            target.getPersistentData().putDouble("TsukuyomiZ", target.getZ());

                            // FIX: Adding the missing parameters
                            target.getPersistentData().putFloat("TsukuyomiYaw", target.getYRot());
                            target.getPersistentData().putFloat("TsukuyomiPitch", target.getXRot());
                            target.getPersistentData().putInt("TsukuyomiCasterId", player.getId());

                            // FIX: Send the packet to tell the client to render the cross!
                            zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                    net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                                    new zyo.narutomod.network.SyncTsukuyomiPacket(target.getId(), true, player.getId())
                            );

                            player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.DOJUTSU.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§4Tsukuyomi Successful!"), true);
                        } else {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Resisted!"), true);
                        }
                        return true;
                    }
                }
            }
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNo target in sight."), true);
            return false;
        }
    }

    public static class ChidoriJutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(net.minecraft.server.level.ServerPlayer player, zyo.narutomod.capability.IShinobiData stats) {
            stats.setChidoriActive(true);

            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 200, 2, false, false));

            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§bChidori!"), true);
            player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 2.0F);

            zyo.narutomod.events.ServerEvents.syncPlayerDataToAllTracking(player);

            return true;
        }
    }

    public static class AmenotejikaraJutsu extends AbstractJutsu {
        @Override
        protected boolean performJutsu(ServerPlayer player, IShinobiData stats) {
            if (!stats.isSharinganActive() || stats.getSharinganStage() < 6) {
                player.displayClientMessage(Component.literal("§cYou need the Rinnegan active to use this!"), true);
                return false;
            }

            double range = 30.0;
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 reachVec = eyePos.add(lookVec.scale(range));

            AABB aabb = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0D);
            Entity targetEntity = null;
            double closestDist = Double.MAX_VALUE;

            for (Entity entity : player.level().getEntities(player, aabb)) {
                // We check for LivingEntities, Items, and specifically our Projectiles (like Fireball)
                if (entity instanceof LivingEntity || entity instanceof ItemEntity || entity instanceof Projectile) {

                    // We inflate the "selection" box for projectiles so they are easier to target mid-flight
                    float hitboxInflation = (entity instanceof FireballJutsuEntity) ? 2.0F : 0.5F;
                    Optional<Vec3> hit = entity.getBoundingBox().inflate(hitboxInflation).clip(eyePos, reachVec);

                    if (hit.isPresent()) {
                        double dist = eyePos.distanceToSqr(hit.get());
                        if (dist < closestDist) {
                            closestDist = dist;
                            targetEntity = entity;
                        }
                    }
                }
            }

            // Swap with Entity (Player, Mob, or Fireball)
            if (targetEntity != null) {
                Vec3 pPos = player.position();
                Vec3 tPos = targetEntity.position();

                player.teleportTo(tPos.x, tPos.y, tPos.z);
                targetEntity.teleportTo(pPos.x, pPos.y, pPos.z);

                // Keep the sound/particle logic clean
                playEffects(player.serverLevel(), pPos, tPos, player);
                return true;
            }

            // Fallback: Swap with Block
            BlockHitResult blockHit = player.level().clip(new ClipContext(eyePos, reachVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = blockHit.getBlockPos();
                BlockPos playerPos = player.blockPosition();
                BlockState targetState = player.level().getBlockState(targetPos);

                if (!targetState.isAir() && targetState.getDestroySpeed(player.level(), targetPos) >= 0) {
                    player.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
                    player.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                    player.level().setBlock(playerPos, targetState, 3);
                    playEffects(player.serverLevel(), Vec3.atCenterOf(playerPos), Vec3.atCenterOf(targetPos), player);
                    return true;
                }
            }
            return false;
        }

        private void playEffects(ServerLevel level, Vec3 pos1, Vec3 pos2, ServerPlayer player) {
            level.playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.DOJUTSU.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, pos1.x, pos1.y, pos1.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, pos2.x, pos2.y, pos2.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos1.x, pos1.y + 1, pos1.z, 20, 0.2, 0.2, 0.2, 0.1);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos2.x, pos2.y + 1, pos2.z, 20, 0.2, 0.2, 0.2, 0.1);
        }
    }
}