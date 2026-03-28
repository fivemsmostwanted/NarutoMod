package zyo.narutomod;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.particle.ModParticles;
import zyo.narutomod.sound.ModSounds;
import zyo.narutomod.entity.ModEntities;

@Mod(NarutoMod.MODID)
public class NarutoMod {
    public static final String MODID = "narutomod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NarutoMod(IEventBus modEventBus) {
        GeckoLib.initialize();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
        
        PacketHandler.register();
        ModItems.register(modEventBus);
        ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("Naruto Mod Initialized");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.SASUKE_KATANA);
            event.accept(ModItems.AKATSUKI_CLOAK);
        }
    }
}