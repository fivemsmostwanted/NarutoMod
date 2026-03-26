package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;

public class FireballRenderer extends EntityRenderer<FireballJutsuEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/fireball_texture.png");
    private final FireballModel<FireballJutsuEntity> model;

    public FireballRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FireballModel<>(context.bakeLayer(FireballModel.LAYER_LOCATION));
    }

    @Override
    public void render(FireballJutsuEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float scale = entity.getVisualScale();
        poseStack.scale(scale, scale, scale);

        // Removed the translation here so it stays perfectly centered!

        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);

        // Swapped to CutoutNoCull to prevent overlapping texture glitches
        VertexConsumer vertexConsumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));

        this.model.renderToBuffer(poseStack, vertexConsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FireballJutsuEntity entity) {
        return TEXTURE;
    }
}