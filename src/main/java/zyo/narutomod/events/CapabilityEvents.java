package zyo.narutomod.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.capability.ShinobiData;
import zyo.narutomod.capability.ShinobiDataProvider;

public class CapabilityEvents {

    // 1. REGISTER THE CAPABILITY (Runs on the MOD bus)
    @Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerCaps(RegisterCapabilitiesEvent event) {
            event.register(ShinobiData.class);
        }
    }

    // 2. ATTACH TO PLAYER & HANDLE DEATH (Runs on the FORGE bus)
    @Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
            // Only attach to Players
            if (event.getObject() instanceof Player) {
                if (!event.getObject().getCapability(ShinobiDataProvider.SHINOBI_DATA).isPresent()) {
                    // Give them their Chakra and Sharingan data!
                    event.addCapability(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "shinobi_properties"), new ShinobiDataProvider());
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            // If the player dies, copy their old data into their new respawned body
            if (event.isWasDeath()) {
                event.getOriginal().getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(oldStore -> {
                    event.getEntity().getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(newStore -> {
                        newStore.copyFrom(oldStore);
                    });
                });
            }
        }
    }
}