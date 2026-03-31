package zyo.narutomod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.entity.ShacklingStakeEntity;

public class ShacklingStakeModel extends EntityModel<ShacklingStakeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "shackling_stake"), "main");
    private final ModelPart mainSpike;

    public ShacklingStakeModel(ModelPart root) {
        this.mainSpike = root.getChild("mainSpike");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("mainSpike", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 24.0F, 4.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(ShacklingStakeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        mainSpike.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}