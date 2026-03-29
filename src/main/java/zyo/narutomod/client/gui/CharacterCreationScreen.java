package zyo.narutomod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.network.SetPlayerFactionPacket;
import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Village;

public class CharacterCreationScreen extends Screen {

    private Clan selectedClan = Clan.CLANLESS;
    private Village selectedVillage = Village.NONE;

    private Button clanButton;
    private Button villageButton;

    public CharacterCreationScreen() {
        super(Component.literal("Character Creation"));
    }

    @Override
    protected void init() {
        super.init();

        int startX = this.width / 2 - 100;
        int startY = this.height / 2 - 40;

        this.clanButton = this.addRenderableWidget(Button.builder(Component.literal("Clan: " + selectedClan.name()), button -> {
            cycleClan();
            button.setMessage(Component.literal("Clan: " + selectedClan.name()));
        }).bounds(startX, startY, 200, 20).build());

        this.villageButton = this.addRenderableWidget(Button.builder(Component.literal("Village: " + selectedVillage.name()), button -> {
            cycleVillage();
            button.setMessage(Component.literal("Village: " + selectedVillage.name()));
        }).bounds(startX, startY + 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("§aConfirm Journey"), button -> {
            PacketHandler.INSTANCE.sendToServer(new SetPlayerFactionPacket(this.selectedClan, this.selectedVillage));
            this.onClose(); // Close the screen
        }).bounds(startX, startY + 70, 200, 20).build());
    }

    private void cycleClan() {
        Clan[] clans = Clan.values();
        int nextOrdinal = (this.selectedClan.ordinal() + 1) % clans.length;
        this.selectedClan = clans[nextOrdinal];
    }

    private void cycleVillage() {
        Village[] villages = Village.values();
        int nextOrdinal = (this.selectedVillage.ordinal() + 1) % villages.length;
        this.selectedVillage = villages[nextOrdinal];
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}