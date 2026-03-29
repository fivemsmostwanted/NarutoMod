package zyo.narutomod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.player.Archetype;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("setarchetype")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .executes(ModCommands::setArchetype)
                )
        );

        dispatcher.register(Commands.literal("stat")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(ModCommands::setStat)
                        )
                )
        );

        dispatcher.register(Commands.literal("sharingan")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("stage", IntegerArgumentType.integer(1, 6))
                        .executes(ModCommands::setSharingan)
                )
        );

        dispatcher.register(Commands.literal("tsukuyomi")
                .requires(source -> source.hasPermission(2))
                .executes(ModCommands::triggerTsukuyomi)
        );

        dispatcher.register(Commands.literal("stats")
                .executes(ModCommands::viewStats)
        );
    }

    private static int setArchetype(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String typeString = StringArgumentType.getString(context, "type").toUpperCase();

        try {
            ServerPlayer player = source.getPlayerOrException();
            Archetype archetype = Archetype.valueOf(typeString);

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                stats.setArchetype(archetype);
                source.sendSuccess(() -> Component.literal("§aSuccessfully set archetype to: " + archetype.name()), false);
            });
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid archetype. Check Archetype.java for valid names (e.g., ILLUSIONIST, BRAWLER, NONE)."));
        } catch (Exception e) {}
        return 1;
    }

    private static int setStat(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String statType = StringArgumentType.getString(context, "type").toLowerCase();
        int level = IntegerArgumentType.getInteger(context, "level");

        try {
            ServerPlayer player = source.getPlayerOrException();
            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (statType.equals("ninjutsu")) {
                    stats.setNinjutsuStat(level);
                    source.sendSuccess(() -> Component.literal("§a[Server] Ninjutsu upgraded to Level " + level), false);
                } else if (statType.equals("genjutsu")) {
                    stats.setGenjutsuStat(level);
                    source.sendSuccess(() -> Component.literal("§a[Server] Genjutsu upgraded to Level " + level), false);
                } else {
                    source.sendFailure(Component.literal("§cUnknown stat! Use 'ninjutsu' or 'genjutsu'."));
                }
            });
        } catch (Exception e) {}
        return 1;
    }

    private static int setSharingan(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int stage = IntegerArgumentType.getInteger(context, "stage");

        try {
            ServerPlayer player = source.getPlayerOrException();
            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                stats.setSharinganStage(stage);
                zyo.narutomod.events.ServerEvents.sharinganStages.put(player.getUUID(), stage);

                zyo.narutomod.network.PacketHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new zyo.narutomod.network.SharinganSyncPacket(player.getUUID(), stats.isSharinganActive(), stage)
                );

                source.sendSuccess(() -> Component.literal("§cSharingan updated to Stage " + stage), false);
            });
        } catch (Exception e) {}
        return 1;
    }

    private static int triggerTsukuyomi(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            player.getPersistentData().putBoolean("TsukuyomiTrapped", true);
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0, false, false));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 4, false, false));

            context.getSource().sendSuccess(() -> Component.literal("§4You are caught in your own Genjutsu..."), false);
        } catch (Exception e) {}
        return 1;
    }

    private static int viewStats(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                player.sendSystemMessage(Component.literal("§6=== Shinobi Stats ==="));
                player.sendSystemMessage(Component.literal("§bChakra: §f" + stats.getChakra() + " / " + stats.getMaxChakra()));
                player.sendSystemMessage(Component.literal("§cSharingan Stage: §f" + stats.getSharinganStage()));
                player.sendSystemMessage(Component.literal("§eNinjutsu Level: §f" + stats.getNinjutsuStat()));
                player.sendSystemMessage(Component.literal("§dGenjutsu Level: §f" + stats.getGenjutsuStat()));
                // Add the rest here as you expand IShinobiData!
            });
        } catch (Exception e) {}
        return 1;
    }
}