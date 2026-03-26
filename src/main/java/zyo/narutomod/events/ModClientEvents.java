package zyo.narutomod.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
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
import zyo.narutomod.network.TsukuyomiPacket;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {

    public static final java.util.Map<java.util.UUID, Boolean> activeSharingans = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> sharinganStages = new java.util.HashMap<>();

    // TSUKUYOMI VARIABLES
    public static final java.util.Map<Integer, Boolean> tsukuyomiVictims = new java.util.HashMap<>();
    public static int dimensionTimer = 0;

    private static zyo.narutomod.entity.TsukuyomiCrossModel<net.minecraft.world.entity.LivingEntity> crossModel;

    // Paste this inside your ModClientEvents class, replacing the old one
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (dimensionTimer > 0) {
                dimensionTimer--;
            }
            HandSignManager.tick();

            if (HandSignKeys.SIGN_1.consumeClick()) HandSignManager.addSign(1);
            if (HandSignKeys.SIGN_2.consumeClick()) HandSignManager.addSign(2);
            if (HandSignKeys.SIGN_3.consumeClick()) HandSignManager.addSign(3);
            if (HandSignKeys.SIGN_4.consumeClick()) HandSignManager.addSign(4);
            if (HandSignKeys.SIGN_5.consumeClick()) HandSignManager.addSign(5);
            if (HandSignKeys.SIGN_6.consumeClick()) HandSignManager.addSign(6);
            if (HandSignKeys.SIGN_7.consumeClick()) HandSignManager.addSign(7);

            net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && HandSignKeys.CHARGE_KEY.isDown()) {

                // Only send the packet once every 4 ticks (5 times a second) so we don't spam the server
                if (player.tickCount % 4 == 0) {
                    PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.ChakraChargePacket());
                }
            }

            while (HandSignKeys.SIGN_8.consumeClick()) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    if (stats.isSharinganActive()) {
                        if (stats.getSharinganStage() >= 3) {
                            int nextStage = stats.getSharinganStage() + 1;
                            if (nextStage > 6) nextStage = 3; // Loop 3 -> MS -> EMS -> Rinnegan -> 3
                            PacketHandler.INSTANCE.sendToServer(new SharinganTogglePacket(true, nextStage));
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§4Eyes Evolving..."), true);
                        } else {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cStage " + stats.getSharinganStage() + " cannot evolve yet."), true);
                        }
                    }
                    // If Sharingan is OFF, F does nothing now.
                });
            }

            // 3. THE 'R' KEY: Pure ON/OFF Toggle
            while (HandSignKeys.SHARINGAN_KEY.consumeClick()) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    boolean isNowActive = !stats.isSharinganActive();
                    // This now uses the synced stage, so it won't reset to 1!
                    PacketHandler.INSTANCE.sendToServer(new SharinganTogglePacket(isNowActive, stats.getSharinganStage()));
                });
            }

            // Inside onClientTick, replace the separate SIGN_4 and AMENO blocks with this:
            while (HandSignKeys.AMENO_KEY.consumeClick()) {
                // Priority 1: If eyes are Stage 6, do the Teleport
                if (isMySharinganActive() && mySharinganStage() == 6) {
                    PacketHandler.INSTANCE.sendToServer(new AmenotejikaraPacket());
                }
                // Priority 2: If eyes are active but too low, show the error message
                else if (isMySharinganActive()) {
                    Minecraft.getInstance().player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§cYour Sharingan is not evolved enough for this..."), true);
                }
                // Priority 3: If no eyes or not Stage 6, treat it as Hand Sign 4!
                else {
                    HandSignManager.addSign(4);
                }
            }

            while (HandSignKeys.GENJUTSU_KEY.consumeClick()) {
                if (isMySharinganActive() && mySharinganStage() >= 4) {
                    PacketHandler.INSTANCE.sendToServer(new TsukuyomiPacket());
                    dimensionTimer = 100;
                } else if (isMySharinganActive()) {
                    Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou need the Mangekyo Sharingan to cast Tsukuyomi!"), true);
                }
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
            // Translate the drawing matrix UP (-Y) by 35 pixels
            event.getGuiGraphics().pose().translate(0, -35, 0);
        }

        // 1. INTERCEPT AND KILL THE VANILLA HOTBAR
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {

            GuiGraphics graphics = event.getGuiGraphics();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            // === DRAW YOUR CUSTOM UI HERE BEFORE CANCELLING ===

            // A. Draw the Tsukuyomi / Sharingan Screen Tints (Kept from your old code)
            if (mc.player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) || dimensionTimer > 0) {
                graphics.fill(0, 0, screenWidth, screenHeight, 0x99440000);
            } else if (isMySharinganActive()) {
                long timeOfDay = mc.level.getDayTime() % 24000;
                if (timeOfDay >= 13000 && timeOfDay <= 23000) {
                    graphics.fill(0, 0, screenWidth, screenHeight, 0x15FF0000);
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

            // 2. DRAW THE STATIC CHEAT SHEET (Bottom Row)
            // 2. DRAW THE STATIC CHEAT SHEET (Bottom Row)
            for (int i = 0; i < handSignKeys.length; i++) {
                int currentX = startX + (i * slotSpacing);
                int centerX = currentX + 10;
                int centerY = startY + 10;

                if (i == 7 && isMySharinganActive()) {
                    int stage = mySharinganStage() - 1;
                    if (stage < 0 || stage > 5) stage = 0;

                    // 1. Draw Red Outer Ring
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 11, 0xFFFF0000);
                    // 2. Draw Dark Inner Background
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0xFF000000);

                    // 3. Reset the color so the image doesn't turn black
                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                    // 4. Force scale the full 64x64 image into the 20x20 slot
                    // Arguments: Texture, screenX, screenY, screenWidth, screenHeight, uOffset, vOffset, textureRegionWidth, textureRegionHeight, fullTextureWidth, fullTextureHeight
                    graphics.blit(EYE_ICONS[stage], currentX, startY, 20, 20, 0, 0, 64, 64, 64, 64);
                }
                else {
                    // NORMAL BEHAVIOR: For all other keys or when Sharingan is OFF
                    int borderColor = (i == 7) ? 0xFFFF0000 : 0xFFFFFFFF;

                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 11, borderColor);
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0x55000000);

                    String keyName = handSignKeys[i].getTranslatedKeyMessage().getString().toUpperCase();
                    int textWidth = mc.font.width(keyName);

                    String label = (i == 7) ? "EYE" : keyName;
                    graphics.drawString(mc.font, label, centerX - (mc.font.width(label) / 2), startY - 12, 0xFFFFFF, false);
                }
            }

            // 2. DRAW THE DYNAMIC COMBO BAR (Top Row)
            java.util.List<Integer> currentCombo = zyo.narutomod.logic.HandSignManager.getSigns();

            if (currentCombo != null && !currentCombo.isEmpty()) {
                int comboStartY = startY - 35; // Draw it 35 pixels above the cheat sheet
                int comboStartX = (screenWidth / 2) - (currentCombo.size() * slotSpacing / 2); // Center the current combo

                for (int i = 0; i < currentCombo.size(); i++) {
                    int currentX = comboStartX + (i * slotSpacing);
                    int centerX = currentX + 10;
                    int centerY = comboStartY + 10;

                    // Outer Gold Border for active combo
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 11, 0xFFFFD700);
                    // Inner Red Background
                    zyo.narutomod.util.RenderUtils.drawCircle(graphics, centerX, centerY, 10, 0x88FF0000);

                    // IMAGE RENDERING LOGIC FOR COMBO (Commented out)
                    /*
                    int signId = currentCombo.get(i);
                    int imageXOffset = (signId - 1) * 20;
                    graphics.blit(HANDSIGN_TEXTURE, currentX, comboStartY, imageXOffset, 0, 20, 20);
                    */
                }
            }

            // C. Draw the Player's Currently Held Weapon (So you aren't blind to your inventory)
            net.minecraft.world.item.ItemStack heldItem = mc.player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                // FIX: Changed 'keys.length' to 'handSignKeys.length'
                int weaponX = startX + (handSignKeys.length * slotSpacing) + 30; // Place it to the right of the jutsu slots
                graphics.renderItem(heldItem, weaponX, startY);
                graphics.drawString(mc.font, "Weapon", weaponX - 5, startY - 10, 0xAAAAAA, false);
            }

            // D. Draw the Chakra Bar (Moved to the bottom right)
            mc.player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                float currentChakra = stats.getChakra();
                float maxChakra = 100.0f; // Later you can scale this with stats!

                int barWidth = 100;
                int barHeight = 6;
                int currentWidth = (int) ((currentChakra / maxChakra) * barWidth);

                int chakraX = screenWidth - barWidth - 20;
                int chakraY = screenHeight - 15;

                // 1. DRAW THE NUMBERS ABOVE THE BAR
                String chakraText = (int)currentChakra + " / " + (int)maxChakra;
                int textWidth = mc.font.width(chakraText);
                // Center the text relative to the bar
                graphics.drawString(mc.font, chakraText, chakraX + (barWidth / 2) - (textWidth / 2), chakraY - 10, 0x00FFFF, false);

                // 2. DRAW THE ACTUAL BAR
                graphics.fill(chakraX, chakraY, chakraX + barWidth, chakraY + barHeight, 0xFF111144);
                graphics.fill(chakraX, chakraY, chakraX + currentWidth, chakraY + barHeight, 0xFF00FFFF);

                // Chakra Bar borders
                graphics.fill(chakraX - 1, chakraY - 1, chakraX + barWidth + 1, chakraY, 0xFF000000);
                graphics.fill(chakraX - 1, chakraY + barHeight, chakraX + barWidth + 1, chakraY + barHeight + 1, 0xFF000000);
                graphics.fill(chakraX - 1, chakraY, chakraX, chakraY + barHeight, 0xFF000000);
                graphics.fill(chakraX + barWidth, chakraY, chakraX + barWidth + 1, chakraY + barHeight, 0xFF000000);
            });

            // 2. FINALLY, TELL MINECRAFT TO ABORT DRAWING THE UGLY VANILLA BOXES
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event) {
        // If we moved this element up in Pre, we pop it back to normal in Post
        if (isSurvivalHudElement(event.getOverlay())) {
            event.getGuiGraphics().pose().popPose();
        }
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();

        if (message.startsWith("!sharingan ")) {
            event.setCanceled(true);
            try {
                int stage = Integer.parseInt(message.split(" ")[1]);
                if (stage >= 1 && stage <= 6) {
                    net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
                    if(player != null) {
                        // Update stage and send to server
                        PacketHandler.INSTANCE.sendToServer(new SharinganTogglePacket(isMySharinganActive(), stage));
                    }
                    Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cSharingan updated to Stage " + stage), false);
                }
            } catch (Exception e) {}

        } else if (message.equals("!tsukuyomi")) {
            event.setCanceled(true);
            PacketHandler.INSTANCE.sendToServer(new zyo.narutomod.network.TsukuyomiSelfPacket());
            Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("§4You are caught in your own Genjutsu..."), false);
        }
    }

    @SubscribeEvent
    public static void onLivingRenderPre(net.minecraftforge.client.event.RenderLivingEvent.Pre<?, ?> event) {
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();

        // FIX: Check for our custom NBT tag!
        if (entity.getPersistentData().getBoolean("TsukuyomiTrapped") || entity.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)) {
            event.getPoseStack().pushPose();
            event.getPoseStack().translate(0.0D, 0.8D, 0.0D);

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

        // FIX: Match the check here!
        if (entity.getPersistentData().getBoolean("TsukuyomiTrapped") || entity.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)) {
            event.getPoseStack().popPose();

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