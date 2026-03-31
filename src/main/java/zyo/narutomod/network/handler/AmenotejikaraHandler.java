package zyo.narutomod.network.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import zyo.narutomod.capability.IShinobiData;

import java.util.Optional;

public class AmenotejikaraHandler implements IActionHandler {
    @Override
    public void execute(ServerPlayer player, IShinobiData stats, int payload) {
        if (!stats.isSharinganActive() || stats.getSharinganStage() < 6) {
            player.displayClientMessage(Component.literal("§cYou need the Rinnegan active to use this!"), true);
            return;
        }

        double range = 30.0;
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(range));

        AABB aabb = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0D);
        Entity targetEntity = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : player.level().getEntities(player, aabb)) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity || entity instanceof ItemEntity || entity instanceof Projectile) {
                Optional<Vec3> hit = entity.getBoundingBox().inflate(0.5D).clip(eyePos, reachVec);
                if (hit.isPresent()) {
                    double dist = eyePos.distanceToSqr(hit.get());
                    if (dist < closestDist) {
                        closestDist = dist;
                        targetEntity = entity;
                    }
                }
            }
        }

        if (targetEntity != null) {
            Vec3 pPos = player.position();
            Vec3 tPos = targetEntity.position();

            player.teleportTo(tPos.x, tPos.y, tPos.z);
            targetEntity.teleportTo(pPos.x, pPos.y, pPos.z);
            playEffects(player.serverLevel(), pPos, tPos);
            return;
        }

        BlockHitResult blockHit = player.level().clip(new ClipContext(eyePos, reachVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos targetPos = blockHit.getBlockPos();
            BlockPos playerPos = player.blockPosition();
            BlockState targetState = player.level().getBlockState(targetPos);

            if (!targetState.isAir() && targetState.getDestroySpeed(player.level(), targetPos) >= 0) {
                player.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
                player.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                player.level().setBlock(playerPos, targetState, 3);
                playEffects(player.serverLevel(), Vec3.atCenterOf(playerPos), Vec3.atCenterOf(targetPos));
            }
        }
    }

    private void playEffects(ServerLevel level, Vec3 pos1, Vec3 pos2) {
        level.playSound(null, pos1.x, pos1.y, pos1.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, pos2.x, pos2.y, pos2.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, pos1.x, pos1.y + 1, pos1.z, 20, 0.2, 0.2, 0.2, 0.1);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, pos2.x, pos2.y + 1, pos2.z, 20, 0.2, 0.2, 0.2, 0.1);
    }
}