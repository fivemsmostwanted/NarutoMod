package zyo.narutomod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class DebugOverlay {

    public static final IGuiOverlay HUD_DEBUG = (gui, guiGraphics, partialTick, width, height) -> {
        Minecraft mc = Minecraft.getInstance();

        // Don't render if the player has the vanilla F3 menu open
        if (mc.options.renderDebug) return;

        // 1. Calculate RAM (Converted from Bytes to Megabytes)
        long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;

        String ramColor = (usedMem > maxMem * 0.8) ? "§c" : "§a"; // Turns red if using >80% RAM
        String ramStr = "RAM: " + ramColor + usedMem + "MB §f/ " + maxMem + "MB";

        // 2. Get Client FPS
        String fpsStr = "FPS: §e" + mc.getFps();

        // 3. Calculate Server TPS & MSPT (Milliseconds Per Tick)
        String tpsStr = "TPS: §7Unknown (Multiplayer)";

        // Singleplayer runs an integrated server, so we can grab the tick time directly
        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            // How long it takes the server to process one tick (ideally < 50ms)
            float mspt = mc.getSingleplayerServer().getAverageTickTime();

            // Minecraft aims for 20 ticks per second.
            float tps = Math.min(20.0F, 1000.0F / mspt);

            String tpsColor = (tps >= 20.0F) ? "§a" : (tps > 15.0F) ? "§e" : "§c";
            tpsStr = String.format("TPS: %s%.1f §f(%.1f mspt)", tpsColor, tps, mspt);
        }

        // Render the text in the top-left corner (below where chat usually sits if scrolled up)
        int x = 10;
        int y = 10;

        guiGraphics.drawString(mc.font, fpsStr, x, y, 0xFFFFFF);
        guiGraphics.drawString(mc.font, ramStr, x, y + 10, 0xFFFFFF);
        guiGraphics.drawString(mc.font, tpsStr, x, y + 20, 0xFFFFFF);
    };
}