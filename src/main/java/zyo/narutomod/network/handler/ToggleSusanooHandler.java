package zyo.narutomod.network.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.entity.ModEntities;
import zyo.narutomod.entity.SusanooEntity;
import zyo.narutomod.events.ServerEvents;

import java.util.Optional;

public class ToggleSusanooHandler implements IActionHandler {
    @Override
    public void execute(ServerPlayer player, IShinobiData stats, int payload) {
        if (!stats.isSharinganActive() || stats.getSharinganStage() < 4) {
            player.displayClientMessage(Component.literal("§cYou need the Mangekyo Sharingan to cast Susanoo!"), true);
            return;
        }
        if (!stats.hasJutsu("narutomod:susanoo")) {
            player.displayClientMessage(Component.literal("§cYou haven't unlocked the Susanoo yet!"), true);
            return;
        }

        Optional<Entity> existingSusanoo = player.getPassengers().stream().filter(e -> e instanceof SusanooEntity).findFirst();

        if (existingSusanoo.isPresent()) {
            existingSusanoo.get().discard();
            player.displayClientMessage(Component.literal("§8Susanoo Deactivated."), true);
        } else {
            if (player.isCreative() || stats.getChakra() >= 100.0F) {
                if (!player.isCreative()) stats.setChakra(stats.getChakra() - 100.0F);

                SusanooEntity susanoo = new SusanooEntity(ModEntities.SUSANOO.get(), player.level());
                susanoo.setOwner(player);
                susanoo.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                susanoo.startRiding(player, true);
                player.level().addFreshEntity(susanoo);

                ServerEvents.syncPlayerDataToAllTracking(player);
                player.displayClientMessage(Component.literal("§5Susanoo Manifested!"), true);
            } else {
                player.displayClientMessage(Component.literal("§cNot enough chakra! (Needs 100)"), true);
            }
        }
    }
}