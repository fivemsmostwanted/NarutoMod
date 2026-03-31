package zyo.narutomod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.logic.StatType;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.network.StatUpgradePacket;

public class NinjaCardScreen extends Screen {

    private static final ResourceLocation INFO_BG = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/gui/ninja_card_info.png");
    private static final ResourceLocation STATS_BG = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/gui/ninja_card_stats.png");

    private final int imageWidth = 262;
    private final int imageHeight = 187;

    private int leftPos, topPos;
    private boolean showStats = false;

    public NinjaCardScreen() {
        super(Component.literal("Ninja Card"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        this.addRenderableWidget(Button.builder(Component.literal(showStats ? "Back to Info" : "View Stats"), btn -> {
            this.showStats = !this.showStats;
            this.rebuildWidgets();
        }).bounds(this.leftPos + 6, this.topPos + this.imageHeight - 25, 80, 20).build());

        if (showStats) {
            int buttonWidth = 5;
            int buttonHeight = 5;

            for (StatType stat : StatType.values()) {
                int btnX = 0;
                int btnY = 0;

                switch (stat) {
                    case NINJUTSU      -> { btnX = this.leftPos + 112; btnY = this.topPos + 14; }
                    case TAIJUTSU      -> { btnX = this.leftPos + 112; btnY = this.topPos + 34; }
                    case KENJUTSU      -> { btnX = this.leftPos + 112; btnY = this.topPos + 52; }
                    case SHURIKENJUTSU -> { btnX = this.leftPos + 112; btnY = this.topPos + 70; }
                    case SUMMONING     -> { btnX = this.leftPos + 112; btnY = this.topPos + 87; }
                    case KINJUTSU      -> { btnX = this.leftPos + 112; btnY = this.topPos + 106; }
                    case SENJUTSU      -> { btnX = this.leftPos + 112; btnY = this.topPos + 124; }
                    case MEDICINE      -> { btnX = this.leftPos + 112; btnY = this.topPos + 141; }
                    case SPEED         -> { btnX = this.leftPos + 112; btnY = this.topPos + 160; }

                    case JUTSU_POWER   -> { btnX = this.leftPos + 182; btnY = this.topPos + 13; }
                    case GENJUTSU      -> { btnX = this.leftPos + 182; btnY = this.topPos + 34; }
                    case IQ            -> { btnX = this.leftPos + 182; btnY = this.topPos + 52; }
                }

                this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
                    PacketHandler.INSTANCE.sendToServer(new StatUpgradePacket(stat));
                }).bounds(btnX, btnY, buttonWidth, buttonHeight).build());
            }
        }
    }

    private void renderStatNumbers(GuiGraphics graphics) {
        if (this.minecraft.player == null) return;

        this.minecraft.player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
            for (StatType stat : StatType.values()) {
                int textX = 0;
                int textY = 0;
                int statValue = 1;

                switch (stat) {
                    case NINJUTSU -> statValue = stats.getNinjutsuStat();
                    case GENJUTSU -> statValue = stats.getGenjutsuStat();
                }

                switch (stat) {
                    case NINJUTSU      -> { textX = this.leftPos + 152; textY = this.topPos + 13; }
                    case TAIJUTSU      -> { textX = this.leftPos + 152; textY = this.topPos + 33; }
                    case KENJUTSU      -> { textX = this.leftPos + 152; textY = this.topPos + 51; }
                    case SHURIKENJUTSU -> { textX = this.leftPos + 170; textY = this.topPos + 69; }
                    case SUMMONING     -> { textX = this.leftPos + 157; textY = this.topPos + 86; }
                    case KINJUTSU      -> { textX = this.leftPos + 152; textY = this.topPos + 105; }
                    case SENJUTSU      -> { textX = this.leftPos + 152; textY = this.topPos + 123; }
                    case MEDICINE      -> { textX = this.leftPos + 152; textY = this.topPos + 140; }
                    case SPEED         -> { textX = this.leftPos + 152; textY = this.topPos + 159; }

                    case JUTSU_POWER   -> { textX = this.leftPos + 235; textY = this.topPos + 12; }
                    case GENJUTSU      -> { textX = this.leftPos + 220; textY = this.topPos + 33; }
                    case IQ            -> { textX = this.leftPos + 200; textY = this.topPos + 51; }
                }

                graphics.drawString(this.font, String.valueOf(statValue), textX, textY, 0x000000, false);
            }
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        ResourceLocation currentBg = showStats ? STATS_BG : INFO_BG;
        graphics.blit(currentBg, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0, 0, 700, 500, 700, 500);

        int playerBoxCenterX = this.leftPos + 45;
        int playerBoxBottomY = this.topPos + 65;
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, playerBoxCenterX, playerBoxBottomY, 25, (float)(playerBoxCenterX - mouseX), (float)(playerBoxBottomY - 50 - mouseY), this.minecraft.player);

        if (!showStats) {
            renderInfoTextValues(graphics);
        } else {
            renderStatNumbers(graphics);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderInfoTextValues(GuiGraphics graphics) {
        if (this.minecraft.player == null) return;

        this.minecraft.player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
            String villageName = stats.getVillage().getDisplayName();
            String clanName = stats.getClan().getDisplayName();
            String archetype = stats.getArchetype().getDisplayName();

            String playerName = this.minecraft.player.getName().getString();

            graphics.drawString(this.font, ": " + villageName, this.leftPos + 40, this.topPos + 93, 0x000000, false);
            graphics.drawString(this.font, ": " + archetype, this.leftPos + 35, this.topPos + 116, 0x000000, false);
            graphics.drawString(this.font, ": " + clanName, this.leftPos + 35, this.topPos + 140, 0x000000, false);

            graphics.drawString(this.font, playerName, this.leftPos + 45 - (this.font.width(playerName) / 2), this.topPos + 10, 0x000000, false);
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}