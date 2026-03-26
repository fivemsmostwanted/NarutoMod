package zyo.narutomod.jutsu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import zyo.narutomod.entity.ModEntities;
import zyo.narutomod.entity.SubstitutionLogEntity;
import zyo.narutomod.entity.FireballJutsuEntity;

import java.util.HashMap;
import java.util.Map;

public class JutsuActions {

    public interface Action {
        void execute(ServerPlayer player);
    }

    private static final Map<Integer, Action> ACTIONS = new HashMap<>();

    public static void registerAll() {
        ACTIONS.put(1, player -> {
            FireballJutsuEntity fireball = new FireballJutsuEntity(player.level(), player);
            fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(fireball);
        });

        ACTIONS.put(2, player -> {
            double oldX = player.getX();
            double oldY = player.getY();
            double oldZ = player.getZ();

            Vec3 look = player.getLookAngle();
            player.teleportTo(oldX + (look.x * 5), oldY, oldZ + (look.z * 5));

            SubstitutionLogEntity log = new SubstitutionLogEntity(ModEntities.SUBSTITUTION_LOG.get(), player.level());
            log.moveTo(oldX, oldY, oldZ, player.getYRot(), 0);
            log.setDeltaMovement(0, 0.1, 0);
            log.hasImpulse = true;
            player.level().addFreshEntity(log);

            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                        oldX, oldY + 1, oldZ, 15, 0.2, 0.5, 0.2, 0.05);
                player.level().playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.CHORUS_FRUIT_TELEPORT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        });

    }

    public static void execute(int id, ServerPlayer player) {
        Action action = ACTIONS.get(id);
        if (action != null) {
            action.execute(player);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cJutsu logic missing for ID: " + id), false);
        }
    }
}