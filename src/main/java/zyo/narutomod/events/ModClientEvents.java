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
import zyo.narutomod.network.SharinganTogglePacket;
import zyo.narutomod.network.AmenotejikaraPacket;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {

    public static final java.util.Map<java.util.UUID, Boolean> activeSharingans = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> sharinganStages = new java.util.HashMap<>();

    public static final java.util.Map<Integer, Boolean> tsukuyomiVictims = new java.util.HashMap<>();
    public static int dimensionTimer = 0;
    public static boolean wasCharging = false;

    private static zyo.narutomod.entity.TsukuyomiCrossModel<net.minecraft.world.entity.LivingEntity> crossModel;

    private static final ResourceLocation[] EYE_ICONS = {
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_1.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_2.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png"),

            // Fallbacks for the missing Mangekyo stages
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png"),
            ResourceLocation.parse(NarutoMod.MODID + ":textures/hud/sharingan_3.png")
    };

    public static boolean isMySharinganActive() {
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        return player != null && activeSharingans.getOrDefault(player.getUUID(), false);
    }

    public static int mySharinganStage() {
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        return player != null ? sharinganStages.getOrDefault(player.getUUID(), 1) : 1;
    }

    private static void handleInput(int signId, boolean isGenjutsuMode) {
        if (isGenjutsuMode) {
            if (signId == 3) {
                net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
                if (isMySharinganActive() && mySharinganStage() >= 4) {
                    zyo.narutomod.network.PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.TsukuyomiPacket());
                    dimensionTimer = 100;
                } else if (isMySharinganActive()) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou need the Mangekyo Sharingan to cast Tsukuyomi!"), true);
                }
            } else if (signId == 4) {
                zyo.narutomod.network.PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.SusanooTogglePacket());
            } else {
                zyo.narutomod.network.PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.InstantGenjutsuPacket(signId));
            }
        } else {
            zyo.narutomod.logic.HandSignManager.addSign(signId);
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

            // 1. Handle Hand Signs (Only call consumeClick ONCE)
            if (HandSignKeys.SIGN_1.consumeClick()) handleInput(1, isGenjutsuMode);
            if (HandSignKeys.SIGN_2.consumeClick()) handleInput(2, isGenjutsuMode);
            if (HandSignKeys.SIGN_3.consumeClick()) handleInput(3, isGenjutsuMode);
            if (HandSignKeys.SIGN_4.consumeClick()) {
                if (isMySharinganActive() && mySharinganStage() == 6) {
                    PacketHandler.INSTANCE.sendToServer(new AmenotejikaraPacket());
                }
                else {
                    handleInput(4, isGenjutsuMode);
                }
            }
            if (HandSignKeys.SIGN_5.consumeClick()) handleInput(5, isGenjutsuMode);
            if (HandSignKeys.SIGN_6.consumeClick()) handleInput(6, isGenjutsuMode);
            if (HandSignKeys.SIGN_7.consumeClick()) handleInput(7, isGenjutsuMode);

            // 2. Chakra Charging
            if (HandSignKeys.CHARGE_KEY.isDown()) {
                if (player.tickCount % 4 == 0) {
                    PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.ChakraChargePacket());
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

            // 3. Eye Evolution (F Key)
            while (HandSignKeys.SIGN_8.consumeClick()) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    if (stats.isSharinganActive()) {
                        if (stats.getSharinganStage() >= 3) {
                            int nextStage = stats.getSharinganStage() + 1;
                            if (nextStage > 6) nextStage = 3;
                            PacketHandler.INSTANCE.sendToServer(new SharinganTogglePacket(true, nextStage));
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§4Eyes Evolving..."), true);
                        } else {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cStage " + stats.getSharinganStage() + " cannot evolve yet."), true);
                        }
                    }
                });
            }

            // 4. Sharingan Toggle
            while (HandSignKeys.SHARINGAN_KEY.consumeClick()) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    boolean isNowActive = !stats.isSharinganActive();
                    PacketHandler.INSTANCE.sendToServer(new SharinganTogglePacket(isNowActive, stats.getSharinganStage()));
                });
            }

            while (zyo.narutomod.keys.HandSignKeys.SUSANOO_TIER_KEY.consumeClick()) {
                zyo.narutomod.network.PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.SusanooTierPacket());
            }

            while (HandSignKeys.MENU_KEY.consumeClick()) {
                mc.setScreen(new zyo.narutomod.client.gui.NinjaCardScreen());
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

            renderCustomCrosshair(event.getGuiGraphics(), screenWidth, screenHeight);
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

            // ==========================================
            // IMAGE SETUP (Commented out for now)
            // ResourceLocation HANDSIGN_TEXTURE = new ResourceLocation("narutomod", "textures/gui/handsigns.png");
            // ==========================================

            net.minecraft.client.KeyMapping[] handSignKeys = {
                    zyo.narutomod.keys.HandSignKeys.SIGN_1,
                    zyo.narutomod.keys.HandSignKeys.SIGN_2,
                    zyo.narutomod.keys.HandSignKeys.SIGN_3,
                    zyo.narutomod.keys.HandSignKeys.SIGN_4,
                    zyo.narutomod.keys.HandSignKeys.SIGN_5,
                    zyo.narutomod.keys.HandSignKeys.SIGN_6,
                    zyo.narutomod.keys.HandSignKeys.SIGN_7,
                    zyo.narutomod.keys.HandSignKeys.SIGN_8
            };
            int slotSpacing = 26;
            int startX = (screenWidth / 2) - (handSignKeys.length * slotSpacing / 2);
            int startY = screenHeight - 25;

            for (int i = 0; i < handSignKeys.length; i++) {
                int currentX = startX + (i * slotSpacing);
                int centerX = currentX + 10;
                int centerY = startY + 10;

                if (i == 7 && isMySharinganActive()) {
                    int stage = mySharinganStage() - 1;
                    if (stage < 0 || stage > 5) stage = 0;

                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 11, 0xFFFF0000);
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0xFF000000);

                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    graphics.blit(EYE_ICONS[stage], currentX, startY, 20, 20, 0, 0, 64, 64, 64, 64);
                }
                else {
                    int borderColor = (i == 7) ? 0xFFFF0000 : 0xFFFFFFFF;

                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 11, borderColor);
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0x55000000);

                    String keyName = handSignKeys[i].getTranslatedKeyMessage().getString().toUpperCase();
                    int textWidth = mc.font.width(keyName);

                    String label = (i == 7) ? "F" : keyName;
                    graphics.drawString(mc.font, label, centerX - (mc.font.width(label) / 2), startY - 12, 0xFFFFFF, false);
                }
            }

            java.util.List<Integer> currentCombo = zyo.narutomod.logic.HandSignManager.getSigns();

            if (currentCombo != null && !currentCombo.isEmpty()) {
                int comboStartY = startY - 35;
                int comboStartX = (screenWidth / 2) - (currentCombo.size() * slotSpacing / 2);

                for (int i = 0; i < currentCombo.size(); i++) {
                    int currentX = comboStartX + (i * slotSpacing);
                    int centerX = currentX + 10;
                    int centerY = comboStartY + 10;

                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 11, 0xFFFFD700);
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0x88FF0000);

                    // IMAGE RENDERING LOGIC FOR COMBO (Commented out)
                    /*
                    int signId = currentCombo.get(i);
                    int imageXOffset = (signId - 1) * 20;
                    graphics.blit(HANDSIGN_TEXTURE, currentX, comboStartY, imageXOffset, 0, 20, 20);
                    */
                }
            }

            net.minecraft.world.item.ItemStack heldItem = mc.player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                int weaponX = startX + (handSignKeys.length * slotSpacing) + 30;
                graphics.renderItem(heldItem, weaponX, startY);
                graphics.drawString(mc.font, "Weapon", weaponX - 5, startY - 10, 0xAAAAAA, false);
            }

            mc.player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                float currentChakra = stats.getChakra();
                float maxChakra = stats.getMaxChakra();

                int barWidth = 100;
                int barHeight = 6;
                int currentWidth = (int) ((currentChakra / maxChakra) * barWidth);

                int chakraX = screenWidth - barWidth - 20;
                int chakraY = screenHeight - 15;

                String chakraText = (int)currentChakra + " / " + (int)maxChakra;
                int textWidth = mc.font.width(chakraText);
                graphics.drawString(mc.font, chakraText, chakraX + (barWidth / 2) - (textWidth / 2), chakraY - 10, 0x00FFFF, false);

                graphics.fill(chakraX, chakraY, chakraX + barWidth, chakraY + barHeight, 0xFF111144);
                graphics.fill(chakraX, chakraY, chakraX + currentWidth, chakraY + barHeight, 0xFF00FFFF);

                graphics.fill(chakraX - 1, chakraY - 1, chakraX + barWidth + 1, chakraY, 0xFF000000);
                graphics.fill(chakraX - 1, chakraY + barHeight, chakraX + barWidth + 1, chakraY + barHeight + 1, 0xFF000000);
                graphics.fill(chakraX - 1, chakraY, chakraX, chakraY + barHeight, 0xFF000000);
                graphics.fill(chakraX + barWidth, chakraY, chakraX + barWidth + 1, chakraY + barHeight, 0xFF000000);
            });

            // Just for testing, add this inside your RenderGuiOverlayEvent
            graphics.drawString(mc.font, "Combo Timer: " + HandSignManager.getComboTimer(), 10, 10, 0xFFFFFF);
            event.setCanceled(true);
        }
    }

    private static void renderCustomCrosshair(GuiGraphics graphics, int width, int height) {
        zyo.narutomod.client.gui.CrosshairRenderer.render(graphics, width, height);
    }

    @SubscribeEvent
    public static void onRenderGuiPost(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event) {
        if (isSurvivalHudElement(event.getOverlay())) {
            event.getGuiGraphics().pose().popPose();
        }
    }

    @SubscribeEvent
    public static void onLivingRenderPre(net.minecraftforge.client.event.RenderLivingEvent.Pre<?, ?> event) {
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();
        net.minecraft.client.player.LocalPlayer localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null) return;

        if (entity.getPersistentData().getBoolean("TsukuyomiTrapped") &&
                (localPlayer.getId() == entity.getId() || localPlayer.getId() == entity.getPersistentData().getInt("TsukuyomiCasterId"))) {
            if (event.getRenderer().getModel() instanceof net.minecraft.client.model.HumanoidModel<?> model) {
                model.rightArm.visible = false;
                model.leftArm.visible = false;

                if (model instanceof net.minecraft.client.model.PlayerModel<?> pModel) {
                    pModel.rightSleeve.visible = false;
                    pModel.leftSleeve.visible = false;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingRenderPost(net.minecraftforge.client.event.RenderLivingEvent.Post<?, ?> event) {
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();
        net.minecraft.client.player.LocalPlayer localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null) return;
        if (entity.getPersistentData().getBoolean("TsukuyomiTrapped") &&
                (localPlayer.getId() == entity.getId() || localPlayer.getId() == entity.getPersistentData().getInt("TsukuyomiCasterId"))) {

            if (event.getRenderer().getModel() instanceof net.minecraft.client.model.HumanoidModel<?> model) {
                model.rightArm.visible = true;
                model.leftArm.visible = true;

                if (model instanceof net.minecraft.client.model.PlayerModel<?> pModel) {
                    pModel.rightSleeve.visible = true;
                    pModel.leftSleeve.visible = true;
                }
            }
        }
    }
}