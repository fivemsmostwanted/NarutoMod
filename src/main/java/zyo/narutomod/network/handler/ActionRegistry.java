package zyo.narutomod.network.handler;

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
        HANDLERS.put(Action.TSUKUYOMI, new TsukuyomiHandler());
        HANDLERS.put(Action.AMENOTEJIKARA, new AmenotejikaraHandler());
    }

    public static IActionHandler getHandler(Action action) {
        return HANDLERS.getOrDefault(action, (player, stats, payload) -> {});
    }
}