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

        // 1. GET THE DYNAMIC SCALE FROM THE ENTITY
        float scale = entity.getVisualScale();

        // 2. APPLY THE SCALE (Multiplies the X, Y, and Z size)
        poseStack.scale(scale, scale, scale);

        // Keep your translation to center it
        poseStack.translate(0.0D, 0.5D, 0.0D);

        // Setup the spinning animation
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(this.getTextureLocation(entity)));

        int glowingLight = 15728640;
        this.model.renderToBuffer(poseStack, vertexConsumer, glowingLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FireballJutsuEntity entity) {
        return TEXTURE;
    }
}