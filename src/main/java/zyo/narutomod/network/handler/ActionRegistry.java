package zyo.narutomod.network.handler;

import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.jutsu.AbstractJutsu;
import zyo.narutomod.jutsu.JutsuRegistry;
import zyo.narutomod.network.ActionRequestPacket.Action;

import java.util.EnumMap;
import java.util.Map;

public class ActionRegistry {
    private static final Map<Action, IActionHandler> HANDLERS = new EnumMap<>(Action.class);

    static {
        HANDLERS.put(Action.CHAKRA_CHARGE, new ChakraChargeHandler());
        HANDLERS.put(Action.TOGGLE_SHARINGAN, new ToggleSharinganHandler());
        HANDLERS.put(Action.EVOLVE_SHARINGAN, new EvolveSharinganHandler());
        HANDLERS.put(Action.TOGGLE_SUSANOO, new ToggleSusanooHandler());
        HANDLERS.put(Action.TIER_SUSANOO, new TierSusanooHandler());

        // Route Dojutsus through the main Jutsu Bridge
        HANDLERS.put(Action.TSUKUYOMI, (player, stats, payload) -> {
            AbstractJutsu logic = JutsuRegistry.JUTSUS.get(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "tsukuyomi"));
            if (logic != null) logic.tryExecute(player, "narutomod:tsukuyomi");
        });

        HANDLERS.put(Action.AMENOTEJIKARA, (player, stats, payload) -> {
            AbstractJutsu logic = JutsuRegistry.JUTSUS.get(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "amenotejikara"));
            if (logic != null) logic.tryExecute(player, "narutomod:amenotejikara");
        });
    }

    public static IActionHandler getHandler(Action action) {
        return HANDLERS.getOrDefault(action, (player, stats, payload) -> {});
    }
}