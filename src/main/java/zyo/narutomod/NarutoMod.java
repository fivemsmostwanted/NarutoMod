package zyo.narutomod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(NarutoMod.MODID)
public class NarutoMod {
    public static final String MODID = "narutomod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NarutoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        zyo.narutomod.network.PacketHandler.register();
        zyo.narutomod.entity.ModEntities.ENTITIES.register(modEventBus);
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("Naruto Mod Initialized");
    }
}