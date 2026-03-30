package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.Optional;
import java.util.function.Supplier;

public class AmenotejikaraPacket {
    public AmenotejikaraPacket() {}
    public AmenotejikaraPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (!stats.isSharinganActive() || stats.getSharinganStage() < 6) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou need the Rinnegan active to use this!"), true);
                    return;
                }

                double range = 30.0;
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 reachVec = eyePos.add(lookVec.scale(range));

                EntityHitResult entityHit = getEntityHitResult(player, eyePos, reachVec);
                if (entityHit != null) {
                    Entity target = entityHit.getEntity();
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
            });
        });
        context.setPacketHandled(true);
    }

    private EntityHitResult getEntityHitResult(ServerPlayer player, Vec3 start, Vec3 end) {
        AABB aabb = player.getBoundingBox().expandTowards(player.getLookAngle().scale(30.0)).inflate(2.0D);
        Entity closestEntity = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : player.level().getEntities(player, aabb)) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity ||
                    entity instanceof net.minecraft.world.entity.item.ItemEntity ||
                    entity instanceof net.minecraft.world.entity.projectile.Projectile) {

                AABB entityBB = entity.getBoundingBox().inflate(0.5D);
                Optional<Vec3> hit = entityBB.clip(start, end);

                if (hit.isPresent()) {
                    double dist = start.distanceToSqr(hit.get());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestEntity = entity;
                    }
                }
            }
        }
        return closestEntity != null ? new EntityHitResult(closestEntity) : null;
    }

    private void playAmenoEffects(ServerLevel level, Vec3 pos1, Vec3 pos2) {
        level.playSound(null, pos1.x, pos1.y, pos1.z, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, pos2.x, pos2.y, pos2.z, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, pos1.x, pos1.y + 1, pos1.z, 20, 0.2, 0.2, 0.2, 0.1);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, pos2.x, pos2.y + 1, pos2.z, 20, 0.2, 0.2, 0.2, 0.1);
    }
}