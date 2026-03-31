package zyo.narutomod.network.handler;

import net.minecraft.server.level.ServerPlayer;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.events.ServerEvents;

public class ChakraChargeHandler implements IActionHandler {
    @Override
    public void execute(ServerPlayer player, IShinobiData stats, int payload) {
        if (stats.getChakra() < stats.getMaxChakra()) {
            float newChakra = Math.min(stats.getChakra() + 10.0F, stats.getMaxChakra());
            stats.setChakra(newChakra);
            ServerEvents.syncPlayerDataToAllTracking(player);
        }
    }
}