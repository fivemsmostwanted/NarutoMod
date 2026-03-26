package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.client.model.SubstitutionLogModel;
import zyo.narutomod.entity.SubstitutionLogEntity;

public class SubstitutionLogRenderer extends EntityRenderer<SubstitutionLogEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/substitution_log.png");
    private final SubstitutionLogModel<SubstitutionLogEntity> model;

    public SubstitutionLogRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SubstitutionLogModel<>(context.bakeLayer(SubstitutionLogModel.LAYER_LOCATION));
    }

    @Override
    public void render(SubstitutionLogEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, -0.8D, 0.0D);

        // Match fireball style: setup animation and render with vertex consumer
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SubstitutionLogEntity entity) {
        return TEXTURE;
    }
}