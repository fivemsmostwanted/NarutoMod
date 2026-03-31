package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.events.ServerEvents;
import zyo.narutomod.entity.ModEntities;
import zyo.narutomod.entity.SusanooEntity;

import java.util.Optional;
import java.util.function.Supplier;

public class ActionRequestPacket {

    public enum Action {
        CHAKRA_CHARGE,
        TOGGLE_SHARINGAN,
        EVOLVE_SHARINGAN,
        TOGGLE_SUSANOO,
        TIER_SUSANOO,
        TSUKUYOMI,
        AMENOTEJIKARA
    }

    private final Action action;
    private final int payload;

    public ActionRequestPacket(Action action) {
        this(action, 0);
    }

    public ActionRequestPacket(Action action, int payload) {
        this.action = action;
        this.payload = payload;
    }

    public ActionRequestPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
        this.payload = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
        buf.writeInt(this.payload);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                switch (this.action) {

                    case CHAKRA_CHARGE -> {
                        if (stats.getChakra() < stats.getMaxChakra()) {
                            float newChakra = Math.min(stats.getChakra() + 10.0F, stats.getMaxChakra());
                            stats.setChakra(newChakra);
                            ServerEvents.syncPlayerDataToAllTracking(player);
                        }
                    }

                    case TOGGLE_SHARINGAN -> {
                        if (stats.getSharinganStage() > 0) {
                            boolean activate = !stats.isSharinganActive();
                            stats.setSharinganActive(activate);

                            if (activate) {
                                int amplifier = (stats.getSharinganStage() >= 4) ? 1 : 0;
                                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, amplifier, false, false));
                                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, amplifier, false, false));
                            } else {
                                player.removeEffect(MobEffects.MOVEMENT_SPEED);
                                player.removeEffect(MobEffects.DAMAGE_BOOST);
                                player.getPassengers().stream().filter(e -> e instanceof SusanooEntity).forEach(Entity::discard);
                            }
                            ServerEvents.syncPlayerDataToAllTracking(player);
                        }
                    }

                    case EVOLVE_SHARINGAN -> {
                        int stage = this.payload;
                        if (stage > 0 && stage <= 6) {
                            stats.setSharinganStage(stage);
                            stats.setSharinganActive(true);

                            int amplifier = (stage >= 4) ? 1 : 0;
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, amplifier, false, false));
                            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, amplifier, false, false));

                            ServerEvents.syncPlayerDataToAllTracking(player);
                        }
                    }

                    case TOGGLE_SUSANOO -> {
                        if (!stats.isSharinganActive() || stats.getSharinganStage() < 4) {
                            player.displayClientMessage(Component.literal("§cYou need the Mangekyo Sharingan to cast Susanoo!"), true);
                            return;
                        }
                        if (!stats.hasJutsu("narutomod:susanoo")) {
                            player.displayClientMessage(Component.literal("§cYou haven't unlocked the Susanoo yet!"), true);
                            return;
                        }
                        Optional<Entity> existingSusanoo = player.getPassengers().stream().filter(e -> e instanceof SusanooEntity).findFirst();

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

                                ServerEvents.syncPlayerDataToAllTracking(player);
                                player.displayClientMessage(Component.literal("§5Susanoo Manifested!"), true);
                            } else {
                                player.displayClientMessage(Component.literal("§cNot enough chakra to manifest Susanoo! (Needs 100)"), true);
                            }
                        }
                    }

                    case TSUKUYOMI -> {
                        double range = 15.0;
                        Vec3 eyePos = player.getEyePosition();
                        Vec3 lookVec = player.getLookAngle();
                        Vec3 reachVec = eyePos.add(lookVec.scale(range));
                        AABB aabb = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0D);
                        boolean targetHit = false;

                        for (Entity entity : player.level().getEntities(player, aabb)) {
                            if (entity instanceof LivingEntity target) {
                                AABB entityBB = target.getBoundingBox().inflate(0.5D);
                                Optional<Vec3> hit = entityBB.clip(eyePos, reachVec);

                                if (hit.isPresent()) {
                                    targetHit = true;
                                    int attackerGen = stats.getGenjutsuStat();
                                    int victimGen = 0;
                                    if (target instanceof ServerPlayer vPlayer) {
                                        victimGen = vPlayer.getCapability(ShinobiDataProvider.SHINOBI_DATA).map(s -> s.getGenjutsuStat()).orElse(0);
                                    }

                                    float successChance = 0.75f + ((attackerGen - victimGen) * 0.05f);
                                    if (Math.random() <= successChance) {
                                        int duration = 100 + (attackerGen * 10);
                                        float mentalDamage = 2.0f + (attackerGen * 0.5f);
                                        target.hurt(player.damageSources().magic(), mentalDamage);

                                        double dX = player.getX() - target.getX();
                                        double dZ = player.getZ() - target.getZ();
                                        float lockYaw = (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90.0F);
                                        float lockPitch = 0.0F;

                                        target.getPersistentData().putBoolean("TsukuyomiTrapped", true);
                                        target.getPersistentData().putInt("TsukuyomiCasterId", player.getId());
                                        target.getPersistentData().putInt("TsukuyomiTicks", duration);
                                        target.getPersistentData().putDouble("TsukuyomiX", target.getX());
                                        target.getPersistentData().putDouble("TsukuyomiY", target.getY());
                                        target.getPersistentData().putDouble("TsukuyomiZ", target.getZ());
                                        target.getPersistentData().putFloat("TsukuyomiYaw", lockYaw);
                                        target.getPersistentData().putFloat("TsukuyomiPitch", lockPitch);

                                        player.displayClientMessage(Component.literal("§4Genjutsu Successful!"), true);
                                    } else {
                                        player.displayClientMessage(Component.literal("§7The victim resisted your Genjutsu..."), true);
                                        if (target instanceof ServerPlayer targetPlayer) {
                                            targetPlayer.displayClientMessage(Component.literal("§aYou resisted a Genjutsu!"), true);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if (!targetHit) player.displayClientMessage(Component.literal("§cNo target found."), true);
                    }

                    case AMENOTEJIKARA -> {
                        if (!stats.isSharinganActive() || stats.getSharinganStage() < 6) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou need the Rinnegan active to use this!"), true);
                            return;
                        }

                        double range = 30.0;
                        Vec3 eyePos = player.getEyePosition();
                        Vec3 lookVec = player.getLookAngle();
                        Vec3 reachVec = eyePos.add(lookVec.scale(range));

                        AABB aabb = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0D);
                        Entity closestEntity = null;
                        double closestDist = Double.MAX_VALUE;

                        for (Entity entity : player.level().getEntities(player, aabb)) {
                            if (entity instanceof net.minecraft.world.entity.LivingEntity ||
                                    entity instanceof net.minecraft.world.entity.item.ItemEntity ||
                                    entity instanceof net.minecraft.world.entity.projectile.Projectile) {

                                AABB entityBB = entity.getBoundingBox().inflate(0.5D);
                                Optional<Vec3> hit = entityBB.clip(eyePos, reachVec);

                                if (hit.isPresent()) {
                                    double dist = eyePos.distanceToSqr(hit.get());
                                    if (dist < closestDist) {
                                        closestDist = dist;
                                        closestEntity = entity;
                                    }
                                }
                            }
                        }

                        if (closestEntity != null) {
                            Entity target = closestEntity;
                            Vec3 playerPos = player.position();
                            Vec3 targetPos = target.position();

                            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                            target.teleportTo(playerPos.x, playerPos.y, playerPos.z);

                            playAmenoEffects(player.serverLevel(), playerPos, targetPos);
                            return;
                        }

                        BlockHitResult blockHit = player.level().clip(new net.minecraft.world.level.ClipContext(
                                eyePos, reachVec, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));

                        if (blockHit.getType() == HitResult.Type.BLOCK) {
                            net.minecraft.core.BlockPos targetPos = blockHit.getBlockPos();
                            net.minecraft.core.BlockPos playerPos = player.blockPosition();

                            net.minecraft.world.level.block.state.BlockState targetState = player.level().getBlockState(targetPos);

                            if (!targetState.isAir() && targetState.getDestroySpeed(player.level(), targetPos) >= 0) {
                                player.level().setBlock(targetPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                                player.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                                player.level().setBlock(playerPos, targetState, 3);
                                playAmenoEffects(player.serverLevel(), Vec3.atCenterOf(playerPos), Vec3.atCenterOf(targetPos));
                            }
                        }
                    }

                    case TIER_SUSANOO -> {
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
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }

    private void playAmenoEffects(ServerLevel level, Vec3 pos1, Vec3 pos2) {
        level.playSound(null, pos1.x, pos1.y, pos1.z, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, pos2.x, pos2.y, pos2.z, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, pos1.x, pos1.y + 1, pos1.z, 20, 0.2, 0.2, 0.2, 0.1);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, pos2.x, pos2.y + 1, pos2.z, 20, 0.2, 0.2, 0.2, 0.1);
    }
}