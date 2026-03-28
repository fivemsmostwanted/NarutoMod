package zyo.narutomod.events;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.command.ModCommands;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModServerEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onAttributeCreate(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(zyo.narutomod.entity.ModEntities.SUSANOO.get(), zyo.narutomod.entity.SusanooEntity.createAttributes().build());
    }
}

