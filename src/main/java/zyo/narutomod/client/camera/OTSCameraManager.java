package zyo.narutomod.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;

public class OTSCameraManager {
    private static final OTSCameraManager INSTANCE = new OTSCameraManager();

    private double offsetX = -0.63D;
    private double offsetY = 0.22D;
    private double offsetZ = 1.89D;

    private OTSCameraManager() {}

    public static OTSCameraManager getInstance() {
        return INSTANCE;
    }

    public boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        return mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK;
    }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }

    public void setOffsets(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }
}