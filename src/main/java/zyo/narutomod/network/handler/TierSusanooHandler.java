package zyo.narutomod.network.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.entity.SusanooEntity;

import java.util.Optional;

public class TierSusanooHandler implements IActionHandler {
    @Override
    public void execute(ServerPlayer player, IShinobiData stats, int payload) {
        Optional<Entity> existingSusanoo = player.getPassengers().stream().filter(e -> e instanceof SusanooEntity).findFirst();

        if (existingSusanoo.isPresent()) {
            SusanooEntity susanoo = (SusanooEntity) existingSusanoo.get();
            int currentTier = susanoo.getTier();
            int maxTier = 1;

            if (stats.hasJutsu("narutomod:susanoo_skeletal")) maxTier = 2;
            if (stats.hasJutsu("narutomod:susanoo_humanoid")) maxTier = 3;

            int nextTier = (currentTier >= maxTier) ? 1 : currentTier + 1;

            if (nextTier != currentTier) {
                susanoo.setTier(nextTier);
                String formName = switch (nextTier) {
                    case 1 -> "Ribcage";
                    case 2 -> "Skeletal";
                    case 3 -> "Humanoid";
                    default -> "Unknown";
                };
                player.displayClientMessage(Component.literal("§5Susanoo Form: " + formName), true);
            } else {
                player.displayClientMessage(Component.literal("§cNext Susanoo form not unlocked."), true);
            }
        }
    }
}