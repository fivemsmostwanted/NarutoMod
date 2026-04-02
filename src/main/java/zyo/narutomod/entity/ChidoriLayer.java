package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.capability.ShinobiDataProvider;

public class ChidoriLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/chidori_orb.png");

    public ChidoriLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
            if (stats.isChidoriActive()) {
                poseStack.pushPose();

                // 1. Attach to Arm & move to palm
                this.getParentModel().rightArm.translateAndRotate(poseStack);
                poseStack.translate(-0.1, 0.65, 0.0);

                // 2. Pulsing Scale
                float pulse = 1.0F + (float) Math.sin(ageInTicks * 0.5F) * 0.1F;
                poseStack.scale(pulse, pulse, pulse);

                // 3. Spin the entire structure
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(ageInTicks * 20.0F));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(ageInTicks * 15.0F));

                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
                float size = 0.55F;

                // 4. Draw 6 planes (every 30 degrees) for a denser, less transparent sphere
                for (int i = 0; i < 6; i++) {
                    drawQuad(poseStack, vertexConsumer, size);
                    // Draw it a second time slightly smaller to "stack" the opacity and make it brighter
                    drawQuad(poseStack, vertexConsumer, size * 0.8F);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(30.0F));
                }

                // 5. Add a horizontal cap
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
                drawQuad(poseStack, vertexConsumer, size);
                drawQuad(poseStack, vertexConsumer, size * 0.8F);

                poseStack.popPose();
            }
        });
    }

    private void drawQuad(PoseStack poseStack, VertexConsumer buffer, float size) {
        PoseStack.Pose last = poseStack.last();
        org.joml.Matrix4f matrix4f = last.pose();
        org.joml.Matrix3f matrix3f = last.normal();

        buffer.vertex(matrix4f, -size, -size, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix4f, size, -size, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix4f, size, size, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix4f, -size, size, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }
}