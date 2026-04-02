package zyo.narutomod.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

    public static void handleOpenSetupScreen() {
        Minecraft.getInstance().setScreen(new zyo.narutomod.client.gui.CharacterCreationScreen());
    }

    // Add this inside ClientPacketHandler.java
    public static void handleAnimationSync(int entityId, String animationName, boolean play) {
        if (Minecraft.getInstance().level != null) {
            net.minecraft.world.entity.Entity entity = Minecraft.getInstance().level.getEntity(entityId);

            // Only play if it's another player (we already play our own animations locally for zero lag)
            if (entity instanceof net.minecraft.client.player.AbstractClientPlayer targetPlayer && targetPlayer.getId() != Minecraft.getInstance().player.getId()) {
                if (play) {
                    zyo.narutomod.client.PlayerAnimManager.playAnimation(targetPlayer, animationName);
                } else {
                    zyo.narutomod.client.PlayerAnimManager.stopAnimation(targetPlayer);
                }
            }
        }
    }

}