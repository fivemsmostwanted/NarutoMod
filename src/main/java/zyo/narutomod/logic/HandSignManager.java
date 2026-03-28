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

        System.out.println("HANDMANAGER Added Sign: " + signId + ". Current sequence: " + currentSequence);
        PacketHandler.INSTANCE.sendToServer(new JutsuC2SPacket(new ArrayList<>(currentSequence)));
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

    public static void clearCombo(String reason) {
        System.out.println("HANDMANAGER Sequence cleared, reason: " + reason);
        currentSequence.clear();
        comboTimer = 0;
    }

    public static int getComboTimer() {
        return comboTimer;
    }

    public static List<Integer> getSigns() {
        return currentSequence;
    }
}