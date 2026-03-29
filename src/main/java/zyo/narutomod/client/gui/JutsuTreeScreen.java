package zyo.narutomod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import zyo.narutomod.capability.IShinobiData;
import zyo.narutomod.capability.ShinobiDataProvider;
import zyo.narutomod.jutsu.JutsuData;
import zyo.narutomod.jutsu.JutsuManager;
import zyo.narutomod.jutsu.JutsuNode;
import zyo.narutomod.jutsu.JutsuTreeManager;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.network.UnlockJutsuPacket;

public class JutsuTreeScreen extends Screen {

    private double panX = 0;
    private double panY = 0;
    private float zoom = 1.0f;

    private boolean isDragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    private static final int GRID_SPACING = 60;
    private static final int NODE_SIZE = 26;

    private JutsuNode hoveredNode = null;

    public JutsuTreeScreen() {
        super(Component.literal("Jutsu Skill Tree"));
    }

    @Override
    protected void init() {
        super.init();
        this.panX = this.width / 2.0;
        this.panY = this.height / 2.0;
    }

    private boolean canSeeNodeBranch(IShinobiData stats, JutsuNode node) {
        if (node.getRequiredClan() != zyo.narutomod.player.Clan.CLANLESS && stats.getClan() != node.getRequiredClan()) return false;
        if (node.getRequiredVillage() != zyo.narutomod.player.Village.NONE && stats.getVillage() != node.getRequiredVillage()) return false;
        if (node.getRequiredSharinganStage() > 0 && stats.getSharinganStage() < node.getRequiredSharinganStage()) return false;

        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.hoveredNode = null;

        graphics.pose().pushPose();

        graphics.pose().translate(this.panX, this.panY, 0);
        graphics.pose().scale(this.zoom, this.zoom, 1.0f);

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                for (JutsuNode root : JutsuTreeManager.ROOT_NODES) {
                    if (canSeeNodeBranch(stats, root)) {
                        drawConnectionsRecursive(graphics, root, stats);
                    }
                }
                for (JutsuNode root : JutsuTreeManager.ROOT_NODES) {
                    if (canSeeNodeBranch(stats, root)) {
                        drawNodesRecursive(graphics, root, stats, mouseX, mouseY);
                    }
                }

                graphics.pose().popPose();

                if (this.hoveredNode != null) {
                    renderNodeTooltip(graphics, mouseX, mouseY, this.hoveredNode, stats);
                }
            });
        } else {
            graphics.pose().popPose();
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawConnectionsRecursive(GuiGraphics graphics, JutsuNode node, IShinobiData stats) {
        int startX = (node.getGridX() * GRID_SPACING) + (NODE_SIZE / 2);
        int startY = (node.getGridY() * GRID_SPACING) + (NODE_SIZE / 2);

        for (JutsuNode child : node.getChildren()) {
            if (!canSeeNodeBranch(stats, child)) continue;

            int endX = (child.getGridX() * GRID_SPACING) + (NODE_SIZE / 2);
            int endY = (child.getGridY() * GRID_SPACING) + (NODE_SIZE / 2);

            int lineColor = stats.hasJutsu(child.getJutsuId().toString()) ? 0xFF00BFFF : 0xFF555555;

            graphics.fill(startX, startY - 2, endX, startY + 2, lineColor);
            graphics.fill(endX - 2, Math.min(startY, endY), endX + 2, Math.max(startY, endY), lineColor);

            drawConnectionsRecursive(graphics, child, stats);
        }
    }

    private void drawNodesRecursive(GuiGraphics graphics, JutsuNode node, IShinobiData stats, int mouseX, int mouseY) {
        int x = node.getGridX() * GRID_SPACING;
        int y = node.getGridY() * GRID_SPACING;

        boolean isUnlocked = stats.hasJutsu(node.getJutsuId().toString());
        boolean canUnlock = !isUnlocked && (node.getParent() == null || stats.hasJutsu(node.getParent().getJutsuId().toString()));

        int frameColor = isUnlocked ? 0xFF00BFFF : (canUnlock ? 0xFFAAAAAA : 0xFF333333);
        int bgColor = isUnlocked ? 0xFF222222 : 0xFF111111;

        graphics.fill(x - 2, y - 2, x + NODE_SIZE + 2, y + NODE_SIZE + 2, frameColor);
        graphics.fill(x, y, x + NODE_SIZE, y + NODE_SIZE, bgColor);

        ResourceLocation iconLocation;
        boolean applyOverlay = false;

        if (isUnlocked) {
            iconLocation = node.getCustomIcon() != null ? node.getCustomIcon() :
                    ResourceLocation.fromNamespaceAndPath(node.getJutsuId().getNamespace(), "textures/gui/jutsus/" + node.getJutsuId().getPath() + ".png");
        } else {
            if (node.getLockedIcon() != null) {
                iconLocation = node.getLockedIcon();
            } else {
                iconLocation = node.getCustomIcon() != null ? node.getCustomIcon() :
                        ResourceLocation.fromNamespaceAndPath(node.getJutsuId().getNamespace(), "textures/gui/jutsus/" + node.getJutsuId().getPath() + ".png");
                applyOverlay = true;
            }
        }

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        graphics.blit(iconLocation, x + 1, y + 1, 0, 0, NODE_SIZE - 2, NODE_SIZE - 2, NODE_SIZE - 2, NODE_SIZE - 2);

        if (applyOverlay) {
            graphics.fill(x + 1, y + 1, x + NODE_SIZE - 1, y + NODE_SIZE - 1, 0xAA111111);
        }

        double adjMouseX = (mouseX - this.panX) / this.zoom;
        double adjMouseY = (mouseY - this.panY) / this.zoom;

        if (adjMouseX >= x && adjMouseX <= x + NODE_SIZE && adjMouseY >= y && adjMouseY <= y + NODE_SIZE) {
            this.hoveredNode = node;
            graphics.fill(x, y, x + NODE_SIZE, y + NODE_SIZE, 0x44FFFFFF);
        }

        for (JutsuNode child : node.getChildren()) {
            if (canSeeNodeBranch(stats, child)) {
                drawNodesRecursive(graphics, child, stats, mouseX, mouseY);
            }
        }
    }

    private void renderNodeTooltip(GuiGraphics graphics, int mouseX, int mouseY, JutsuNode node, IShinobiData stats) {
        JutsuData data = JutsuManager.LOADED_JUTSUS.get(node.getJutsuId());

        String name = data != null ? data.name : "Unknown Jutsu";
        int cost = data != null ? data.xp_cost : node.getXpCost();
        boolean isUnlocked = stats.hasJutsu(node.getJutsuId().toString());

        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("§b" + name));
        tooltip.add(Component.literal(isUnlocked ? "§aLearned" : "§7Cost: §a" + cost + " XP Levels"));
        tooltip.add(Component.literal("§7Req Ninjutsu Lvl: §e" + node.getRequiredNinjutsuLevel()));

        if (isUnlocked && data != null && data.hand_signs != null) {
            String signs = data.hand_signs.toString().replaceAll("[\\[\\]]", "");
            tooltip.add(Component.literal("§dHand Signs: §f" + signs));
        } else if (!isUnlocked) {
            tooltip.add(Component.literal("§8Hand Signs: ???"));
        }

        graphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.zoom += (float) (delta * 0.1f);
        this.zoom = Mth.clamp(this.zoom, 0.25f, 2.5f);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.hoveredNode != null) {
                PacketHandler.INSTANCE.sendToServer(new UnlockJutsuPacket(this.hoveredNode.getJutsuId()));
                return true;
            }
            this.isDragging = true;
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDragging) {
            this.panX += (mouseX - this.lastMouseX);
            this.panY += (mouseY - this.lastMouseY);
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}