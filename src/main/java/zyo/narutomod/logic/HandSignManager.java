package zyo.narutomod.logic;

import java.util.ArrayList;
import java.util.List;

public class HandSignManager {
    public static final List<Integer> currentSequence = new ArrayList<>();
    private static int comboTimer = 0;
    private static int inputCooldown = 0;

    public static void addSign(int signId) {
        if (inputCooldown > 0) return;

        currentSequence.add(signId);
        comboTimer = 30;

        inputCooldown = 1;

        System.out.println("Added Sign: " + signId + " | Current Sequence: " + currentSequence);

        zyo.narutomod.network.PacketHandler.INSTANCE.sendToServer(
                new zyo.narutomod.network.JutsuC2SPacket(currentSequence)
        );
    }

    public static java.util.List<Integer> getSigns() {
        return currentSequence;
    }

    public static void tick() {
        if (inputCooldown > 0) {
            inputCooldown--;
        }

        if (comboTimer > 0) {
            comboTimer--;
            if (comboTimer == 0) {
                currentSequence.clear();
                System.out.println("Combo timed out. Sequence cleared.");
            }
        }
    }
}