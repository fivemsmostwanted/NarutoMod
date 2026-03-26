package zyo.narutomod.client;

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
import zyo.narutomod.events.ModClientEvents;

public class SharinganEyeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation[] SHARINGAN_TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/eyes/sharingan_1.png"),
            ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/eyes/sharingan_2.png"),
            ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/eyes/sharingan_3.png"),
            ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/eyes/sharingan_ms.png"),
            ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/eyes/sharingan_ems.png"),
            ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/eyes/sharingan_ems_rinne.png")
    };

    public SharinganEyeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        // CHECK UUID OF THE PLAYER BEING RENDERED
        boolean hasSharingan = ModClientEvents.activeSharingans.getOrDefault(player.getUUID(), false);

        if (!hasSharingan) {
            return;
        }

        int stage = ModClientEvents.sharinganStages.getOrDefault(player.getUUID(), 1) - 1;
        if (stage < 0 || stage > 5) stage = 0;

        long time = player.level().getDayTime() % 24000;
        boolean isNight = time >= 13000 && time <= 23000;

        RenderType rt = isNight ?
                RenderType.eyes(SHARINGAN_TEXTURES[stage]) :
                RenderType.entityTranslucent(SHARINGAN_TEXTURES[stage]);

        VertexConsumer vertexConsumer = buffer.getBuffer(rt);

        poseStack.pushPose();
        poseStack.scale(1.001f, 1.001f, 1.001f);

        if (isNight) {
            poseStack.translate(0, 0, -0.005f);
        }

        int lightToUse = isNight ? 15728640 : packedLight;

        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                lightToUse,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        poseStack.popPose();
    }
}