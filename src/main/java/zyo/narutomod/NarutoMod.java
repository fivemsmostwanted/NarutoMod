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
import zyo.narutomod.sound.ModSounds;

@Mod(NarutoMod.MODID)
public class NarutoMod {
    public static final String MODID = "narutomod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NarutoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        software.bernie.geckolib.GeckoLib.initialize();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
        zyo.narutomod.network.PacketHandler.register();
        zyo.narutomod.item.ModItems.register(modEventBus);
        zyo.narutomod.particle.ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);
        zyo.narutomod.entity.ModEntities.ENTITIES.register(modEventBus);
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("Naruto Mod Initialized");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(zyo.narutomod.item.ModItems.SASUKE_KATANA);
        }
    }
}