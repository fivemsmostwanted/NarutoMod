package zyo.narutomod.events;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.client.camera.OTSCameraManager;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CameraEvents {

    private static Method setPositionMethod = null;

    static {
        try {
            setPositionMethod = Camera.class.getDeclaredMethod("setPosition", double.class, double.class, double.class);
        } catch (Exception e1) {
            try {
                setPositionMethod = Camera.class.getDeclaredMethod("m_90576_", double.class, double.class, double.class);
            } catch (Exception e2) {
                System.out.println("NarutoMod: Failed to unlock Camera.setPosition via Reflection!");
            }
        }

        if (setPositionMethod != null) {
            setPositionMethod.setAccessible(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (event.phase == TickEvent.Phase.END && mc.player != null) {
            if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        }
    }

    @SubscribeEvent
    public static void onCameraUpdate(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();
        OTSCameraManager manager = OTSCameraManager.getInstance();

        if (manager.isActive() && camera.isDetached()) {
            Vector3f forward = camera.getLookVector();
            Vector3f up = camera.getUpVector();
            Vector3f left = camera.getLeftVector();

            double dForward = manager.getOffsetZ();
            double dUp = manager.getOffsetY();
            double dLeft = manager.getOffsetX();

            Vec3 pos = camera.getPosition();

            double newX = pos.x + (forward.x() * dForward) + (up.x() * dUp) + (left.x() * dLeft);
            double newY = pos.y + (forward.y() * dForward) + (up.y() * dUp) + (left.y() * dLeft);
            double newZ = pos.z + (forward.z() * dForward) + (up.z() * dUp) + (left.z() * dLeft);

            try {
                setPositionMethod.invoke(camera, newX, newY, newZ);
            } catch (Exception e) {
                // handle?
            }
        }
    }
}
