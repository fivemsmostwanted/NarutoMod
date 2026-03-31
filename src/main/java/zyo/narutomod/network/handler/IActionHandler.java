package zyo.narutomod.network.handler;

import net.minecraft.server.level.ServerPlayer;
import zyo.narutomod.capability.IShinobiData;

public interface IActionHandler {
    void execute(ServerPlayer player, IShinobiData stats, int payload);
}