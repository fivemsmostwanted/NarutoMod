package zyo.narutomod.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.events.ServerEvents;

public class EvolveSharinganHandler implements IActionHandler {
    @Override
    public void execute(ServerPlayer player, IShinobiData stats, int payload) {
        if (payload > 0 && payload <= 6) {
            stats.setSharinganStage(payload);
            stats.setSharinganActive(true);

            int amplifier = (payload >= 4) ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, amplifier, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, amplifier, false, false));

            ServerEvents.syncPlayerDataToAllTracking(player);
        }
    }
}