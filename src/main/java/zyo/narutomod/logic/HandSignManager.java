package zyo.narutomod.logic;

import java.util.ArrayList;
import java.util.List;
import zyo.narutomod.network.PacketHandler;
import zyo.narutomod.network.JutsuC2SPacket;

public class HandSignManager {
    public static final List<Integer> currentSequence = new ArrayList<>();
    private static int comboTimer = 0;
    private static int inputCooldown = 0;

    public static void addSign(int signId) {
        if (inputCooldown > 0) return;

        currentSequence.add(signId);
        comboTimer = 70;
        inputCooldown = 1;

        if (net.minecraft.client.Minecraft.getInstance().player != null) {
            zyo.narutomod.client.PlayerAnimManager.playAnimation(
                    net.minecraft.client.Minecraft.getInstance().player,
                    "handanim"
            );

            net.minecraft.client.Minecraft.getInstance().player.playSound(zyo.narutomod.sound.ModSounds.HANDSIGN.get(), 1.0F, 1.0F);
        }

        PacketHandler.INSTANCE.sendToServer(new JutsuC2SPacket(new ArrayList<>(currentSequence)));
    }

    public static void clearCombo(String reason) {
        currentSequence.clear();
        comboTimer = 0;

        if (net.minecraft.client.Minecraft.getInstance().player != null) {
            zyo.narutomod.client.PlayerAnimManager.stopAnimation(net.minecraft.client.Minecraft.getInstance().player);
        }
    }

    public static void tick() {
        if (inputCooldown > 0) inputCooldown--;

        if (comboTimer > 0) {
            comboTimer--;
            if (comboTimer == 0 && !currentSequence.isEmpty()) {
                clearCombo("Timer experied too slow");
            }
        }
    }

    public static int getComboTimer() {
        return comboTimer;
    }

    public static List<Integer> getSigns() {
        return currentSequence;
    }
}