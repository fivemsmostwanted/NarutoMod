package zyo.narutomod.jutsu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.capability.ShinobiDataProvider;

public abstract class AbstractJutsu {

    public final void tryExecute(ServerPlayer player, String jutsuId) {
        player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
            ResourceLocation id = ResourceLocation.tryParse(jutsuId);
            JutsuData data = JutsuManager.LOADED_JUTSUS.get(id);

            if (data == null) {
                player.displayClientMessage(Component.literal("§cError: Jutsu data not found!"), true);
                return;
            }

            if (stats.isOnCooldown(jutsuId)) {
                player.displayClientMessage(Component.literal("§c" + data.name + " is on cooldown!"), true);
                player.level().playSound(null, player.blockPosition(), zyo.narutomod.sound.ModSounds.JUTSU_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                return;
            }

            if (stats.getChakra() < data.chakra_cost) {
                player.displayClientMessage(Component.literal("§bNot enough Chakra!"), true);
                return;
            }

            boolean success = performJutsu(player, stats);

            if (success) {
                stats.setChakra(stats.getChakra() - data.chakra_cost);
                stats.setCooldown(jutsuId, data.cooldown);

                zyo.narutomod.events.ServerEvents.syncPlayerDataToAllTracking(player);
            }
        });
    }

    protected abstract boolean performJutsu(ServerPlayer player, zyo.narutomod.capability.IShinobiData stats);
}