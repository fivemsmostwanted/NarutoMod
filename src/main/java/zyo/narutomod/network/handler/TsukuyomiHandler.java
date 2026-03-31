package zyo.narutomod.network.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.capability.ShinobiDataProvider;

import java.util.Optional;

public class TsukuyomiHandler implements IActionHandler {
    @Override
    public void execute(ServerPlayer player, IShinobiData stats, int payload) {
        double range = 15.0;
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(range));
        AABB aabb = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0D);

        for (Entity entity : player.level().getEntities(player, aabb)) {
            if (entity instanceof LivingEntity target) {
                if (target.getBoundingBox().inflate(0.5D).clip(eyePos, reachVec).isPresent()) {
                    int attackerGen = stats.getGenjutsuStat();
                    int victimGen = target.getCapability(ShinobiDataProvider.SHINOBI_DATA).map(IShinobiData::getGenjutsuStat).orElse(0);

                    if (Math.random() <= (0.75f + ((attackerGen - victimGen) * 0.05f))) {
                        int duration = 100 + (attackerGen * 10);
                        target.getPersistentData().putBoolean("TsukuyomiTrapped", true);
                        target.getPersistentData().putInt("TsukuyomiTicks", duration);
                        target.getPersistentData().putDouble("TsukuyomiX", target.getX());
                        target.getPersistentData().putDouble("TsukuyomiY", target.getY());
                        target.getPersistentData().putDouble("TsukuyomiZ", target.getZ());
                        player.displayClientMessage(Component.literal("§4Genjutsu Successful!"), true);
                    } else {
                        player.displayClientMessage(Component.literal("§7Resisted!"), true);
                    }
                    return;
                }
            }
        }
    }
}