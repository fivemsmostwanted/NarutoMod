package zyo.narutomod.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.network.SyncShinobiDataPacket;
import zyo.narutomod.player.Archetype;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    public static void syncPlayerData(ServerPlayer target, ServerPlayer receiver) {
        target.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            if (stats instanceof zyo.narutomod.capability.ShinobiData) {
                ((zyo.narutomod.capability.ShinobiData) stats).saveNBTData(tag);
                PacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> receiver),
                        new SyncShinobiDataPacket(target.getId(), tag)
                );
            }
        });
    }

    public static void syncPlayerDataToAllTracking(ServerPlayer player) {
        player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            if (stats instanceof zyo.narutomod.capability.ShinobiData) {
                ((zyo.narutomod.capability.ShinobiData) stats).saveNBTData(tag);
                PacketHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new SyncShinobiDataPacket(player.getId(), tag)
                );
            }
        });
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer targetPlayer && event.getEntity() instanceof ServerPlayer tracker) {
            syncPlayerData(targetPlayer, tracker);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.side == net.minecraftforge.fml.LogicalSide.SERVER && event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            ServerPlayer player = (ServerPlayer) event.player;

            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                stats.tickCooldowns();
            });

            if (player.getPersistentData().getBoolean("TsukuyomiTrapped")) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    // TODO: Genjutsu resistance logic
                });
            }

            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.getClan() == zyo.narutomod.player.Clan.UCHIHA && stats.getSharinganStage() == 0) {
                    int uchihaTicks = player.getPersistentData().getInt("UchihaTicks");
                    uchihaTicks++;
                    player.getPersistentData().putInt("UchihaTicks", uchihaTicks);

                    if (uchihaTicks >= 1000) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0, false, false));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 200, 0, false, false));

                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYour eyes feel different..."), true);
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ELDER_GUARDIAN_CURSE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        stats.setSharinganStage(1);
                        stats.unlockJutsu("narutomod:sharingan_root");
                        stats.unlockJutsu("narutomod:sharingan_1");

                        stats.setChakra(stats.getMaxChakra());
                        stats.setSharinganActive(true);
                        syncPlayerDataToAllTracking(player);
                    }
                }
            });

            if (player.tickCount % 20 == 0) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    float currentChakra = stats.getChakra();
                    float maxChakra = stats.getMaxChakra();
                    boolean needsSync = false;

                    if (currentChakra > maxChakra) {
                        currentChakra = maxChakra;
                        stats.setChakra(maxChakra);
                        needsSync = true;
                    }

                    if (stats.isSharinganActive()) {
                        float baseDrain = switch (stats.getSharinganStage()) {
                            case 1 -> 4.0f;
                            case 2 -> 8.0f;
                            case 3 -> 15.0f;
                            case 4 -> 35.0f;
                            case 5 -> 15.0f;
                            case 6 -> 40.0f;
                            default -> 4.0f;
                        };

                        if (stats.getArchetype() == Archetype.DESTROYER) {
                            baseDrain *= 1.5f;
                        }

                        boolean hasSusanoo = player.getPassengers().stream().anyMatch(e -> e instanceof zyo.narutomod.entity.SusanooEntity);
                        if (hasSusanoo) {
                            baseDrain += 25.0f;
                        }

                        float newChakra = currentChakra - baseDrain;

                        if (newChakra <= 0) {
                            stats.setChakra(0);
                            stats.setSharinganActive(false);

                            player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
                            player.removeEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST);

                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cChakra exhausted. Sharingan deactivated."), true);
                        } else {
                            stats.setChakra(newChakra);
                        }
                        needsSync = true;

                    } else if (currentChakra < maxChakra) {
                        float regenAmount = 5.0f + (stats.getSharinganStage() * 1.5f);
                        stats.setChakra(Math.min(currentChakra + regenAmount, maxChakra));
                        needsSync = true;
                    }

                    if (needsSync) {
                        syncPlayerDataToAllTracking(player);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacked(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

            if (event.getSource().getEntity() == player) return;
            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.isSharinganActive()) {
                    float dodgeChance = stats.getSharinganStage() * 0.10f;
                    dodgeChance += (stats.getGenjutsuStat() / 10) * 0.02f;

                    if (Math.random() < dodgeChance) {
                        event.setCanceled(true);

                        double oldX = player.getX();
                        double oldY = player.getY();
                        double oldZ = player.getZ();

                        double angle = Math.random() * Math.PI * 2;
                        double dashDist = 3.0;
                        double newX = oldX + (Math.cos(angle) * dashDist);
                        double newZ = oldZ + (Math.sin(angle) * dashDist);

                        player.teleportTo(newX, oldY, newZ);

                        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                    oldX, oldY + 1, oldZ, 8, 0.1, 0.5, 0.1, 0.02);

                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                    newX, oldY + 1, newZ, 8, 0.1, 0.5, 0.1, 0.02);
                        }

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
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            zyo.narutomod.jutsu.JutsuTreeManager.initializeTrees();
            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.getClan() == zyo.narutomod.player.Clan.CLANLESS && stats.getVillage() == zyo.narutomod.player.Village.NONE) {
                    zyo.narutomod.config.NarutoConfig.SelectionMode mode = zyo.narutomod.config.NarutoConfig.SELECTION_MODE.get();

                    if (mode == zyo.narutomod.config.NarutoConfig.SelectionMode.MENU_CHOICE) {
                        zyo.narutomod.network.PacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                                new zyo.narutomod.network.OpenSetupScreenPacket()
                        );
                    }
                }

                syncPlayerDataToAllTracking(player);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
            player.removeEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();

        event.getOriginal().getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(oldStats -> {
            event.getEntity().getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(newStats -> {
                newStats.copyFrom(oldStats);
            });
        });
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                if (stats.isSharinganActive()) {
                    stats.setSharinganActive(false);
                }
                syncPlayerDataToAllTracking(player);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayerDataToAllTracking(player);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListeners(net.minecraftforge.event.AddReloadListenerEvent event) {
        event.addListener(new zyo.narutomod.jutsu.JutsuManager());
        zyo.narutomod.jutsu.JutsuRegistry.registerAll();
        zyo.narutomod.jutsu.JutsuTreeManager.initializeTrees();
    }
}