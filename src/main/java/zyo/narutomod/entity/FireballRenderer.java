package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;

public class FireballRenderer extends EntityRenderer<FireballJutsuEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/fireball.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public FireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FireballJutsuEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float scale = entity.getVisualScale();
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        VertexConsumer vertexBuilder = buffer.getBuffer(RENDER_TYPE);
        PoseStack.Pose matrixEntry = poseStack.last();
        org.joml.Matrix4f matrix4f = matrixEntry.pose();
        org.joml.Matrix3f matrix3f = matrixEntry.normal();

        int fullLight = 15728880;

        vertex(vertexBuilder, matrix4f, matrix3f, fullLight, 0.0F, 0, 0, 1);
        vertex(vertexBuilder, matrix4f, matrix3f, fullLight, 1.0F, 0, 1, 1);
        vertex(vertexBuilder, matrix4f, matrix3f, fullLight, 1.0F, 1, 1, 0);
        vertex(vertexBuilder, matrix4f, matrix3f, fullLight, 0.0F, 1, 0, 0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer builder, org.joml.Matrix4f pose, org.joml.Matrix3f normal, int lightmapUV, float x, int y, int u, int v) {
        builder.vertex(pose, x - 0.5F, (float)y - 0.25F, 0.0F)
                .color(255, 255, 255, 255)
                .uv((float)u, (float)v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmapUV)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(FireballJutsuEntity entity) {
        return TEXTURE;
    }
}