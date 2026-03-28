package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import zyo.narutomod.client.model.SusanooModel;

public class SusanooRenderer extends GeoEntityRenderer<SusanooEntity> {
    public SusanooRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SusanooModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(SusanooEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void render(SusanooEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}