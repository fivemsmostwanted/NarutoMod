package zyo.narutomod.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.keys.HandSignKeys;
import zyo.narutomod.logic.HandSignManager;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.network.ActionRequestPacket;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {

    public static final java.util.Map<Integer, Boolean> tsukuyomiVictims = new java.util.HashMap<>();
    public static int dimensionTimer = 0;
    public static boolean wasCharging = false;
    public static boolean wasSprinting = false;

    private static final ResourceLocation[] EYE_ICONS = {
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_1.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_2.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png")
    };

    private static final ResourceLocation[] HANDSIGN_ICONS = {
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/tiger.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/snake.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/ram.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/monkey.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/horse.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/rat.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/boar.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/gui/hare.png")
    };

    public static boolean isMySharinganActive() {
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        return player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA)
                .map(stats -> stats.isSharinganActive()).orElse(false);
    }

    public static int mySharinganStage() {
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 1;
        return player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA)
                .map(stats -> stats.getSharinganStage()).orElse(1);
    }

    private static void handleInput(int signId, boolean isGenjutsuMode) {
        long windowId = net.minecraft.client.Minecraft.getInstance().getWindow().getWindow();

        boolean isAltDown = com.mojang.blaze3d.platform.InputConstants.isKeyDown(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT) ||
                com.mojang.blaze3d.platform.InputConstants.isKeyDown(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT);

        boolean isCtrlDown = com.mojang.blaze3d.platform.InputConstants.isKeyDown(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) ||
                com.mojang.blaze3d.platform.InputConstants.isKeyDown(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL);

        if (isAltDown || isCtrlDown) {
            if (signId == 3) {
                if (isAltDown) {
                    PacketHandler.INSTANCE.sendToServer(new ActionRequestPacket(ActionRequestPacket.Action.TSUKUYOMI));
                }
            } else if (signId == 4) {
                if (isCtrlDown) {
                    PacketHandler.INSTANCE.sendToServer(new ActionRequestPacket(ActionRequestPacket.Action.AMENOTEJIKARA));
                } else if (isAltDown) {
                    PacketHandler.INSTANCE.sendToServer(new ActionRequestPacket(ActionRequestPacket.Action.TOGGLE_SUSANOO));
                }
            } else {
                if (isAltDown) {
                    PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.InstantGenjutsuPacket(signId));
                }
            }
        } else {
            HandSignManager.addSign(signId);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (dimensionTimer > 0) dimensionTimer--;
            HandSignManager.tick();

            Minecraft mc = Minecraft.getInstance();
            net.minecraft.client.player.LocalPlayer player = mc.player;
            if (player == null) return;

//            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
//                if (stats.getMsBleedTimer() > 0) {
//                    stats.setMsBleedTimer(stats.getMsBleedTimer() - 1);
//                }
//            });

            if (player.getPersistentData().getBoolean("TsukuyomiTrapped")) {
                float lockYaw = player.getPersistentData().getFloat("TsukuyomiYaw");
                float lockPitch = player.getPersistentData().getFloat("TsukuyomiPitch");
                player.setYRot(lockYaw);
                player.setXRot(lockPitch);
                player.yRotO = lockYaw;
                player.xRotO = lockPitch;
                player.setYHeadRot(lockYaw);
                return;
            }

            boolean isGenjutsuMode = HandSignKeys.GENJUTSU_MODIFIER.isDown();

            if (HandSignKeys.SIGN_1.consumeClick()) handleInput(1, isGenjutsuMode);
            if (HandSignKeys.SIGN_2.consumeClick()) handleInput(2, isGenjutsuMode);
            if (HandSignKeys.SIGN_3.consumeClick()) handleInput(3, isGenjutsuMode);
            if (HandSignKeys.SIGN_4.consumeClick()) handleInput(4, isGenjutsuMode);
            if (HandSignKeys.SIGN_5.consumeClick()) handleInput(5, isGenjutsuMode);
            if (HandSignKeys.SIGN_6.consumeClick()) handleInput(6, isGenjutsuMode);
            if (HandSignKeys.SIGN_7.consumeClick()) handleInput(7, isGenjutsuMode);

            if (HandSignKeys.CHARGE_KEY.isDown()) {
                if (player.tickCount % 4 == 0) {
                    PacketHandler.INSTANCE.sendToServer(new ActionRequestPacket(ActionRequestPacket.Action.CHAKRA_CHARGE));
                }
                if (!wasCharging) {
                    zyo.narutomod.client.PlayerAnimManager.playAnimation(player, "chakraanim");
                    wasCharging = true;
                }
            } else {
                if (wasCharging) {
                    if (HandSignManager.getComboTimer() == 0) {
                        zyo.narutomod.client.PlayerAnimManager.stopAnimation(player);
                    }
                    wasCharging = false;
                }
            }

            boolean isSprinting = player.isSprinting() && !player.isFallFlying() && !player.isPassenger() && !player.isSwimming() && !player.isInWater();
            boolean isAttacking = player.swingTime > 0;

            if (isSprinting && !isAttacking) {
                if (!wasSprinting) {
                    zyo.narutomod.client.PlayerAnimManager.playAnimation(player, "naruto_run");
                    wasSprinting = true;
                }
            } else {
                if (wasSprinting) {
                    zyo.narutomod.client.PlayerAnimManager.stopAnimation(player);
                    wasSprinting = false;
                }
            }

            while (HandSignKeys.SIGN_8.consumeClick()) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    if (stats.isSharinganActive()) {
                        int currentStage = stats.getSharinganStage();
                        if (currentStage >= 3) {
                            int maxAllowedStage = 3;
                            if (stats.hasJutsu("narutomod:rinnegan")) maxAllowedStage = 6;
                            else if (stats.hasJutsu("narutomod:eternal_mangekyou")) maxAllowedStage = 5;
                            else if (stats.hasJutsu("narutomod:mangekyou_sharingan")) maxAllowedStage = 4;

                            int nextStage = currentStage + 1;
                            if (nextStage > maxAllowedStage) nextStage = 3;

                            if (nextStage != currentStage) {
                                PacketHandler.INSTANCE.sendToServer(new ActionRequestPacket(ActionRequestPacket.Action.EVOLVE_SHARINGAN, nextStage));
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§4Eyes Evolving..."), true);
                            }
                        }
                    }
                });
            }

            while (HandSignKeys.SHARINGAN_KEY.consumeClick()) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    if (stats.getSharinganStage() > 0) {
                        PacketHandler.INSTANCE.sendToServer(new ActionRequestPacket(ActionRequestPacket.Action.TOGGLE_SHARINGAN));
                    }
                });
            }

            while (HandSignKeys.SUSANOO_TIER_KEY.consumeClick()) {
                PacketHandler.INSTANCE.sendToServer(new ActionRequestPacket(ActionRequestPacket.Action.TIER_SUSANOO));
            }

            while (HandSignKeys.MENU_KEY.consumeClick()) {
                mc.setScreen(new zyo.narutomod.client.gui.NinjaCardScreen());
            }

            while (HandSignKeys.TREE_KEY.consumeClick()) {
                mc.setScreen(new zyo.narutomod.client.gui.JutsuTreeScreen());
            }
        }
    }

    private static boolean isSurvivalHudElement(net.minecraftforge.client.gui.overlay.NamedGuiOverlay overlay) {
        return overlay == VanillaGuiOverlay.PLAYER_HEALTH.type() ||
                overlay == VanillaGuiOverlay.FOOD_LEVEL.type() ||
                overlay == VanillaGuiOverlay.ARMOR_LEVEL.type() ||
                overlay == VanillaGuiOverlay.AIR_LEVEL.type() ||
                overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type() ||
                overlay == VanillaGuiOverlay.JUMP_BAR.type() ||
                overlay == VanillaGuiOverlay.MOUNT_HEALTH.type();
    }

    @SubscribeEvent
    public static void onRenderGuiPre(net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (isSurvivalHudElement(event.getOverlay())) {
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(0, -35, 0);
        }

        if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
            event.setCanceled(true);
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            zyo.narutomod.client.gui.CrosshairRenderer.render(event.getGuiGraphics(), screenWidth, screenHeight);
        }

        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            GuiGraphics graphics = event.getGuiGraphics();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            if (mc.player.getPersistentData().getBoolean("TsukuyomiTrapped")) {
                graphics.fill(0, 0, screenWidth, screenHeight, 0x99440000);
            } else if (isMySharinganActive()) {
                boolean isCasting = mc.level.players().stream().anyMatch(p ->
                        p.getPersistentData().getBoolean("TsukuyomiTrapped") &&
                                p.getPersistentData().getInt("TsukuyomiCasterId") == mc.player.getId()
                );

                if (isCasting) {
                    graphics.fill(0, 0, screenWidth, screenHeight, 0x99440000);
                } else {
                    long timeOfDay = mc.level.getDayTime() % 24000;
                    if (timeOfDay >= 13000 && timeOfDay <= 23000) {
                        graphics.fill(0, 0, screenWidth, screenHeight, 0x15FF0000);
                    }
                }
            }

            net.minecraft.client.KeyMapping[] handSignKeys = {
                    HandSignKeys.SIGN_1, HandSignKeys.SIGN_2, HandSignKeys.SIGN_3,
                    HandSignKeys.SIGN_4, HandSignKeys.SIGN_5, HandSignKeys.SIGN_6,
                    HandSignKeys.SIGN_7, HandSignKeys.SIGN_8
            };

            int slotSpacing = 22;
            int startX = (screenWidth / 2) - (handSignKeys.length * slotSpacing / 2);
            int startY = screenHeight - 22;

            // 1. RENDER HOTBAR
            for (int i = 0; i < handSignKeys.length; i++) {
                int currentX = startX + (i * slotSpacing);
                int centerX = currentX + 10;
                int centerY = startY + 10;

                if (i == 7) {
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0xFFFF0000); // Shrunk radius 11 -> 10
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 9, isMySharinganActive() ? 0xFF000000 : 0x55000000); // Shrunk radius 10 -> 9

                    if (isMySharinganActive()) {
                        int stage = mySharinganStage() - 1;
                        if (stage < 0 || stage > 5) stage = 0;

                        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                        graphics.blit(EYE_ICONS[stage], currentX + 3, startY + 3, 14, 14, 0, 0, 64, 64, 64, 64);
                    } else {
                        graphics.drawString(mc.font, "F", centerX - (mc.font.width("F") / 2), startY - 10, 0xFFFFFF, false);
                    }

                } else {
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0xFFFFFFFF);
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 9, 0x55000000);

                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    com.mojang.blaze3d.systems.RenderSystem.enableBlend();

                    graphics.blit(HANDSIGN_ICONS[i], currentX + 3, startY + 3, 14, 14, 0, 0, 256, 256, 256, 256);

                    String label = handSignKeys[i].getTranslatedKeyMessage().getString().toUpperCase();
                    graphics.drawString(mc.font, label, centerX - (mc.font.width(label) / 2), startY - 10, 0xFFFFFF, false);
                }
            }

            // 2. RENDER COMBO SEQUENCE
            java.util.List<Integer> currentCombo = zyo.narutomod.logic.HandSignManager.getSigns();

            if (currentCombo != null && !currentCombo.isEmpty()) {
                int comboStartY = startY - 30;
                int comboStartX = (screenWidth / 2) - (currentCombo.size() * slotSpacing / 2);

                for (int i = 0; i < currentCombo.size(); i++) {
                    int currentX = comboStartX + (i * slotSpacing);
                    int centerX = currentX + 10;
                    int centerY = comboStartY + 10;

                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0xFFFFD700);
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 9, 0xFFFFFFFF);
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 9, 0x55000000);

                    int signId = currentCombo.get(i);
                    if (signId >= 1 && signId <= 7) {
                        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                        graphics.blit(HANDSIGN_ICONS[signId - 1], currentX + 3, comboStartY + 3, 14, 14, 0, 0, 256, 256, 256, 256);
                    }
                }
            }

            // 3. RENDER CHAKRA BAR
            mc.player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                float currentChakra = stats.getChakra();
                float maxChakra = stats.getMaxChakra();
                int barWidth = 100;
                int chakraX = screenWidth - barWidth - 20;
                int chakraY = screenHeight - 15;
                int currentWidth = (int) ((currentChakra / maxChakra) * barWidth);

                graphics.fill(chakraX, chakraY, chakraX + barWidth, chakraY + 6, 0xFF111144);
                graphics.fill(chakraX, chakraY, chakraX + currentWidth, chakraY + 6, 0xFF00FFFF);
                String txt = (int)currentChakra + " / " + (int)maxChakra;
                graphics.drawString(mc.font, txt, chakraX + (barWidth/2) - (mc.font.width(txt)/2), chakraY - 10, 0x00FFFF, false);
            });

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event) {
        if (isSurvivalHudElement(event.getOverlay())) {
            event.getGuiGraphics().pose().popPose();
        }
    }
}