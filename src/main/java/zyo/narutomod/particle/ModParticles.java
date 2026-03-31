package zyo.narutomod.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import zyo.narutomod.NarutoMod;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, NarutoMod.MODID);

    public static final RegistryObject<SimpleParticleType> CUSTOM_FLAME =
            PARTICLES.register("flame", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> CUSTOM_CHAKRA =
            PARTICLES.register("chakra", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        // Switch from FlameParticle.Provider to your new FireballFlameParticle.Provider
        event.registerSpriteSet(CUSTOM_FLAME.get(), FireballFlameParticle.Provider::new);
        event.registerSpriteSet(CUSTOM_CHAKRA.get(), net.minecraft.client.particle.FlameParticle.Provider::new);
    }
}