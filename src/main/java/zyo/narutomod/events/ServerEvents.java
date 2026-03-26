package zyo.narutomod.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.network.SharinganSyncPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Notice this is FORGE bus, but NOT Dist.CLIENT! This runs strictly on the Server.
@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    // The Server's memory!
    public static final Map<UUID, Boolean> activeSharingans = new HashMap<>();
    public static final Map<UUID, Integer> sharinganStages = new HashMap<>();

    // 1. This fires the exact moment a player loads in and "sees" another player
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        // "Target" is the player being looked at. "Tracker" is the player looking.
        if (event.getTarget() instanceof Player targetPlayer && event.getEntity() instanceof ServerPlayer tracker) {

            boolean isActive = activeSharingans.getOrDefault(targetPlayer.getUUID(), false);
            int stage = sharinganStages.getOrDefault(targetPlayer.getUUID(), 1);

            // If the target has their eyes active, secretly send a packet to the new player to sync them up!
            if (isActive) {
                PacketHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> tracker),
                        new SharinganSyncPacket(targetPlayer.getUUID(), isActive, stage)
                );
            }
        }
    }

    // 3. Passive Chakra Regeneration
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.side == net.minecraftforge.fml.LogicalSide.SERVER && event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            ServerPlayer player = (ServerPlayer) event.player;

            if (player.getPersistentData().getBoolean("TsukuyomiTrapped")) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    // Having high Genjutsu points makes the Blindness/Slowness expire faster
                    // (Note: Minecraft handles effect expiration automatically, but
                    // this is where you'd add logic to "shake off" effects manually if desired)
                });
            }

            // Minecraft runs at 20 ticks per second.
            // Using modulo 20 means this code only runs exactly once per second.
            if (player.tickCount % 20 == 0) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {

                    float currentChakra = stats.getChakra();
                    float maxChakra = 100.0f; // We can move this to ShinobiData later when players level up!

                    // If they are missing chakra...
                    if (currentChakra < maxChakra) {

                        // Regenerate 5 chakra per second (Change this number to whatever feels balanced!)
                        float regenAmount = 5.0f;
                        float newChakra = Math.min(currentChakra + regenAmount, maxChakra);

                        stats.setChakra(newChakra);

                        // Send the "Receipt" to the Client so the blue HUD bar visually goes up
                        zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player),
                                new zyo.narutomod.network.SyncChakraPacket(newChakra)
                        );
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacked(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            // Ignore damage that shouldn't be dodged
            if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.isSharinganActive()) {
                    // 1. Calculate Dodge Chance
                    float dodgeChance = stats.getSharinganStage() * 0.10f;
                    dodgeChance += (stats.getGenjutsuStat() / 10) * 0.02f;

                    if (Math.random() < dodgeChance) {
                        // 2. CANCEL THE DAMAGE
                        event.setCanceled(true);

                        // 3. CALCULATE DASH POSITION (Teleport)
                        double oldX = player.getX();
                        double oldY = player.getY();
                        double oldZ = player.getZ();

                        // Pick a random horizontal direction to "flicker" to (3 blocks away)
                        double angle = Math.random() * Math.PI * 2;
                        double dashDist = 3.0;
                        double newX = oldX + (Math.cos(angle) * dashDist);
                        double newZ = oldZ + (Math.sin(angle) * dashDist);

                        // 4. PERFORM THE TELEPORT
                        // Using teleportTo ensures the server and client stay in sync
                        player.teleportTo(newX, oldY, newZ);

                        // 5. SPAWN PARTICLES (Server-side)
                        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            // Particles at the OLD spot (where they were standing)
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                    oldX, oldY + 1, oldZ, 8, 0.1, 0.5, 0.1, 0.02);

                            // Particles at the NEW spot (where they appeared)
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                    newX, oldY + 1, newZ, 8, 0.1, 0.5, 0.1, 0.02);
                        }

                        // 6. SOUND EFFECT
                        player.level().playSound(null, player.blockPosition(),
                                net.minecraft.sounds.SoundEvents.CHORUS_FRUIT_TELEPORT,
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);

                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Sharingan Evade]"), true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
            player.removeEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST);
        }
        activeSharingans.remove(event.getEntity().getUUID());
        sharinganStages.remove(event.getEntity().getUUID());
    }

    // Add this to your ServerEvents.java
    @SubscribeEvent
    public static void onAddReloadListeners(net.minecraftforge.event.AddReloadListenerEvent event) {
        // Registers our JSON reader to run whenever the server starts or someone types /reload
        event.addListener(new zyo.narutomod.jutsu.JutsuManager());
        zyo.narutomod.jutsu.JutsuActions.registerAll();
    }

    // 4. Temporary Admin Command for testing RPG Stats
    @SubscribeEvent
    public static void onServerChat(net.minecraftforge.event.ServerChatEvent event) {
        // Get the raw message typed in chat
        String message = event.getRawText();

        if (message.startsWith("!stat ")) {
            // Stop the message from actually showing up in the public chat box
            event.setCanceled(true);

            net.minecraft.server.level.ServerPlayer player = event.getPlayer();

            try {
                // Split the message: "!stat" [0], "ninjutsu" [1], "10" [2]
                String[] parts = message.split(" ");
                String statType = parts[1].toLowerCase();
                int level = Integer.parseInt(parts[2]);

                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    if (statType.equals("ninjutsu")) {
                        stats.setNinjutsuStat(level);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Server] Ninjutsu upgraded to Level " + level));
                    } else if (statType.equals("genjutsu")) {
                        stats.setGenjutsuStat(level);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Server] Genjutsu upgraded to Level " + level));
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cUnknown stat! Use 'ninjutsu' or 'genjutsu'."));
                    }
                });
            } catch (Exception e) {
                // If they type it wrong (like "!stat ninjutsu abc")
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cError: Correct usage is !stat <type> <level> (e.g., !stat ninjutsu 5)"));
            }
        }
    }
}