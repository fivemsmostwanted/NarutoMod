package zyo.narutomod.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.client.model.ShacklingStakeModel;

public class ShacklingStakeRenderer extends EntityRenderer<ShacklingStakeEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/shackling_stake.png");
    private final ShacklingStakeModel model;

    public ShacklingStakeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShacklingStakeModel(context.bakeLayer(ShacklingStakeModel.LAYER_LOCATION));
    }

    @Override
    public void render(ShacklingStakeEntity entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(entity.getXRot()));

        com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ShacklingStakeEntity entity) {
        return TEXTURE;
    }
}