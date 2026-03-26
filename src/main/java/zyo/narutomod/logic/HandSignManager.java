package zyo.narutomod.logic;

import java.util.ArrayList;
import java.util.List;

public class HandSignManager {
    public static final List<Integer> currentSequence = new ArrayList<>();
    private static int comboTimer = 0;

    // NEW: The Input Blocker!
    private static int inputCooldown = 0;

    public static void addSign(int signId) {
        // If the cooldown is active, ignore the keystroke entirely
        if (inputCooldown > 0) return;

        currentSequence.add(signId);
        comboTimer = 60;

        // Block all other inputs for 5 ticks (0.25 seconds).
        // You can increase this to 10 if you want the typing to feel slower/more deliberate.
        inputCooldown = 1;

        System.out.println("Added Sign: " + signId + " | Current Sequence: " + currentSequence);

        zyo.narutomod.network.PacketHandler.INSTANCE.sendToServer(
                new zyo.narutomod.network.JutsuC2SPacket(currentSequence)
        );
    }

    public static java.util.List<Integer> getSigns() {
        return currentSequence; // or whatever your list variable is called!
    }

    public static void tick() {
        // Tick down the input blocker
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