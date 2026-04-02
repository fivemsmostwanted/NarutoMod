package zyo.narutomod.events;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;
import zyo.narutomod.client.camera.OTSCameraManager;

import java.lang.reflect.Field;

public class CameraEvents {

    private Field posField = null;

    public CameraEvents() {
        try {
            // Scan for the ONLY Vec3 field in the 1.20.1 Camera class
            for (Field f : Camera.class.getDeclaredFields()) {
                if (f.getType() == Vec3.class) {
                    f.setAccessible(true);
                    posField = f;
                    System.out.println("!!! NARUTO MOD !!!: Camera Position Field UNLOCKED via Type-Scanning.");
                    break; // We found the one and only position field!
                }
            }

            if (posField == null) {
                System.err.println("!!! NARUTO MOD !!!: CRITICAL - No Vec3 field found in Camera class.");
            }
        } catch (Exception e) {
            System.err.println("!!! NARUTO MOD !!!: Camera reflection failed: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.END && mc.player != null) {
            if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        }
    }

    @SubscribeEvent
    public void onCameraUpdate(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();
        OTSCameraManager manager = OTSCameraManager.getInstance();

        if (manager.isActive() && camera.isDetached() && posField != null) {
            Vector3f forward = camera.getLookVector();
            Vector3f up = camera.getUpVector();
            Vector3f left = camera.getLeftVector();

            // Pulling your exact custom values from the manager
            double dForward = -manager.getOffsetZ();
            double dUp = manager.getOffsetY();
            double dLeft = manager.getOffsetX();

            // THE FIX: Anchor the camera strictly to the player's eyes, wiping out Vanilla F5 zoom.
            Vec3 eyePos = camera.getEntity().getEyePosition((float) event.getPartialTick());

            double newX = eyePos.x + (forward.x() * dForward) + (up.x() * dUp) + (left.x() * dLeft);
            double newY = eyePos.y + (forward.y() * dForward) + (up.y() * dUp) + (left.y() * dLeft);
            double newZ = eyePos.z + (forward.z() * dForward) + (up.z() * dUp) + (left.z() * dLeft);

            try {
                // Instantly snap the camera to your exact custom offset
                posField.set(camera, new Vec3(newX, newY, newZ));
            } catch (Exception e) {
                // Ignore silent errors during frame render
            }
        }
    }
}