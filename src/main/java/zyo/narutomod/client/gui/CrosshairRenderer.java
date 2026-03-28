package zyo.narutomod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.events.ModClientEvents;

import java.util.Optional;

public class CrosshairRenderer {
    private static final ResourceLocation CROSSHAIR = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/gui/crosshair.png");

    private static float lerpX = -1;
    private static float lerpY = -1;
    private static final float SMOOTHNESS = 0.35f;

    public static Entity lockedTarget = null;
    private static boolean middleMouseWasDown = false;

    public static void render(GuiGraphics graphics, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        handleLockOnInput(mc);

        if (lockedTarget != null && (!lockedTarget.isAlive() || lockedTarget.distanceTo(mc.player) > 40)) {
            lockedTarget = null;
        }

        float targetX = width / 2.0f;
        float targetY = height / 2.0f;
        boolean isLocked = (lockedTarget != null);

        if (isLocked) {
            Vec3 targetPos = lockedTarget.position().add(0, lockedTarget.getBbHeight() / 2.0, 0);
            Vector4f screenPos = projectToScreen(targetPos, mc, width, height);

            if (screenPos.w() > 0) {
                targetX = screenPos.x();
                targetY = screenPos.y();
            } else {
                lockedTarget = null;
                isLocked = false;
            }
        }

        if (lerpX == -1) {
            lerpX = targetX;
            lerpY = targetY;
        } else {
            lerpX += (targetX - lerpX) * SMOOTHNESS;
            lerpY += (targetY - lerpY) * SMOOTHNESS;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(lerpX, lerpY, 0);

        if (ModClientEvents.isMySharinganActive()) {
            float rotation = (mc.level.getGameTime() + mc.getFrameTime()) * 2.5f;
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
        }

        graphics.pose().scale(0.6f, 0.6f, 1.0f);

        if (isLocked) {
            RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, 1.0F);
        }

        graphics.blit(CROSSHAIR, -7, -7, 0, 0, 15, 15, 15, 15);

        if (isLocked) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        graphics.pose().popPose();
    }

    private static void handleLockOnInput(Minecraft mc) {
        boolean isMiddleMouseDown = mc.mouseHandler.isMiddlePressed();

        if (isMiddleMouseDown && !middleMouseWasDown) {
            if (lockedTarget != null) {
                lockedTarget = null;
            } else {
                lockedTarget = findTarget(mc);
            }
        }
        middleMouseWasDown = isMiddleMouseDown;
    }

    private static Entity findTarget(Minecraft mc) {
        double distance = 30.0D;
        Vec3 eyePos = mc.player.getEyePosition(1.0F);
        Vec3 lookVec = mc.player.getViewVector(1.0F);
        Vec3 traceEnd = eyePos.add(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);

        AABB searchBox = mc.player.getBoundingBox().expandTowards(lookVec.scale(distance)).inflate(2.0D);

        Entity closestEntity = null;
        double closestDistance = distance * distance;

        for (Entity entity : mc.level.getEntities(mc.player, searchBox, e -> e instanceof LivingEntity && e.isAlive())) {
            AABB hitBox = entity.getBoundingBox().inflate(0.5D);
            Optional<Vec3> hitResult = hitBox.clip(eyePos, traceEnd);

            if (hitResult.isPresent()) {
                double distToEntity = eyePos.distanceToSqr(hitResult.get());
                if (distToEntity < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distToEntity;
                }
            }
        }

        return closestEntity;
    }

    private static Vector4f projectToScreen(Vec3 pos, Minecraft mc, int width, int height) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        float rx = (float) (pos.x - camPos.x);
        float ry = (float) (pos.y - camPos.y);
        float rz = (float) (pos.z - camPos.z);

        float fov = (float) mc.options.fov().get().doubleValue();
        float aspect = (float) mc.getWindow().getWidth() / (float) mc.getWindow().getHeight();
        Matrix4f proj = new Matrix4f().setPerspective((float) Math.toRadians(fov), aspect, 0.05f, 1000f);

        Matrix4f view = new Matrix4f()
                .rotationX((float) Math.toRadians(camera.getXRot()))
                .rotateY((float) Math.toRadians(camera.getYRot() + 180.0F));

        Vector4f result = new Vector4f(rx, ry, rz, 1.0F);
        view.transform(result);
        proj.transform(result);

        if (result.w() > 0) {
            float guiX = (result.x() / result.w() + 1.0F) * 0.5F * (float) width;
            float guiY = (1.0F - (result.y() / result.w() + 1.0F) * 0.5F) * (float) height;
            return new Vector4f(guiX, guiY, 0, result.w());
        }
        return new Vector4f(0, 0, 0, -1);
    }
}