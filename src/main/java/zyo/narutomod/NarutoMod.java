package zyo.narutomod;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;
import zyo.narutomod.item.ModCreativeTabs;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.particle.ModParticles;
import zyo.narutomod.sound.ModSounds;
import zyo.narutomod.entity.ModEntities;

@Mod(NarutoMod.MODID)
public class NarutoMod {
    public static final String MODID = "narutomod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NarutoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, zyo.narutomod.config.NarutoConfig.SPEC);
        GeckoLib.initialize();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        PacketHandler.register();
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);

        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new zyo.narutomod.events.CameraEvents());
            LOGGER.info("!!! NARUTO MOD !!!: Camera logic registered.");
        }
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("Naruto Mod Initialized");
    }
}