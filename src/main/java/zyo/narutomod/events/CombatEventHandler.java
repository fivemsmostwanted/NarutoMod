package zyo.narutomod.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.capability.ShinobiDataProvider;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            attacker.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.isChidoriActive()) {
                    float extraDamage = 15.0f + (stats.getNinjutsuStat() * 0.5f);
                    event.setAmount(event.getAmount() + extraDamage);

                    attacker.level().playSound(null, attacker.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.2F);
                    if (attacker.level() instanceof ServerLevel level) {
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, event.getEntity().getX(), event.getEntity().getY() + 1, event.getEntity().getZ(), 20, 0.5, 0.5, 0.5, 0.2);
                    }

                    stats.setChidoriActive(false);
                    ServerEvents.syncPlayerDataToAllTracking(attacker);
                }
            });
        }
    }
}