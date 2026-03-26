package zyo.narutomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import zyo.narutomod.entity.TsukuyomiCrossModel;
import zyo.narutomod.events.ClientModEvents;
import zyo.narutomod.events.ModClientEvents;

public class TsukuyomiCrossLayer<T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>> extends RenderLayer<T, M> {

    // Using vanilla obsidian
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/obsidian.png");
    private final TsukuyomiCrossModel<T> crossModel;

    public TsukuyomiCrossLayer(RenderLayerParent<T, M> renderer, net.minecraft.client.model.geom.EntityModelSet modelSet) {
        super(renderer);
        this.crossModel = new TsukuyomiCrossModel<>(modelSet.bakeLayer(ClientModEvents.TSUKUYOMI_CROSS_LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (entity.hasEffect(MobEffects.BLINDNESS)) {

            // 1. RENDER THE CROSS
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.3D, 0.4D);
            VertexConsumer crossBuffer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            this.crossModel.renderToBuffer(poseStack, crossBuffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();

            // 2. RENDER THE T-POSE ARMS
            if (this.getParentModel() instanceof net.minecraft.client.model.HumanoidModel<?> model) {
                poseStack.pushPose();

                // Get the actual skin texture of the player/zombie
                ResourceLocation skin = this.getTextureLocation(entity);
                VertexConsumer armBuffer = buffer.getBuffer(RenderType.entityCutoutNoCull(skin));

                // Set the arms straight out (1.57 is roughly 90 degrees)
                model.rightArm.xRot = 0.0F;
                model.rightArm.yRot = 0.0F;
                model.rightArm.zRot = 1.57F;

                model.leftArm.xRot = 0.0F;
                model.leftArm.yRot = 0.0F;
                model.leftArm.zRot = -1.57F;

                // Make them temporarily visible to draw them
                model.rightArm.visible = true;
                model.leftArm.visible = true;

                model.rightArm.render(poseStack, armBuffer, packedLight, OverlayTexture.NO_OVERLAY);
                model.leftArm.render(poseStack, armBuffer, packedLight, OverlayTexture.NO_OVERLAY);

                // Do the exact same thing for the 3D sleeves if it's a Player
                if (model instanceof net.minecraft.client.model.PlayerModel<?> pModel) {
                    pModel.rightSleeve.xRot = 0.0F;
                    pModel.rightSleeve.yRot = 0.0F;
                    pModel.rightSleeve.zRot = 1.57F;

                    pModel.leftSleeve.xRot = 0.0F;
                    pModel.leftSleeve.yRot = 0.0F;
                    pModel.leftSleeve.zRot = -1.57F;

                    pModel.rightSleeve.visible = true;
                    pModel.leftSleeve.visible = true;

                    pModel.rightSleeve.render(poseStack, armBuffer, packedLight, OverlayTexture.NO_OVERLAY);
                    pModel.leftSleeve.render(poseStack, armBuffer, packedLight, OverlayTexture.NO_OVERLAY);

                    // Hide the sleeves again
                    pModel.rightSleeve.visible = false;
                    pModel.leftSleeve.visible = false;
                }

                // Hide the arms again so we don't glitch the renderer
                model.rightArm.visible = false;
                model.leftArm.visible = false;

                poseStack.popPose();
            }
        }
    }
}