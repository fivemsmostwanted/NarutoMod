//package zyo.narutomod.client;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import net.minecraft.client.model.PlayerModel;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.RenderLayerParent;
//import net.minecraft.client.renderer.entity.layers.RenderLayer;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.resources.ResourceLocation;
//import zyo.narutomod.NarutoMod;
//import zyo.narutomod.capability.ShinobiDataProvider;
//
//public class MSBleedLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
//    private static final ResourceLocation BLOOD_TEXTURE = ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/player/ms_bleed.png");
//
//    public MSBleedLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
//        super(parent);
//    }
//
//    @Override
//    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
//        player.getCapability(ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
//            if (stats.getMsBleedTimer() > 0) {
//                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(BLOOD_TEXTURE));
//                this.getParentModel().head.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
//            }
//        });
//    }
//}