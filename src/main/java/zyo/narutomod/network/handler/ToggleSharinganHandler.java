package zyo.narutomod.network.handler;

import net.minecraft.server.level.ServerPlayer;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.events.ServerEvents;

public class ToggleSharinganHandler implements IActionHandler {
    @Override
    public void execute(ServerPlayer player, IShinobiData stats, int payload) {
        if (stats.getSharinganStage() > 0) {
            stats.setSharinganActive(!stats.isSharinganActive());
            ServerEvents.syncPlayerDataToAllTracking(player);
        }
    }
}