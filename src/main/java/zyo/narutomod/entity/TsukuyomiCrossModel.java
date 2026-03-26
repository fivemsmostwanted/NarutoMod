package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class TsukuyomiCrossModel<T extends LivingEntity> extends EntityModel<T> {
    private final ModelPart verticalBeam;
    private final ModelPart horizontalBeam;

    public TsukuyomiCrossModel(ModelPart root) {
        this.verticalBeam = root.getChild("verticalBeam");
        this.horizontalBeam = root.getChild("horizontalBeam");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // The tall vertical piece of the cross
        partdefinition.addOrReplaceChild("verticalBeam", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -24.0F, 0.0F, 8.0F, 48.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 4.0F));

        // The horizontal piece where the arms go
        partdefinition.addOrReplaceChild("horizontalBeam", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-20.0F, -12.0F, 0.0F, 40.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 4.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // The cross doesn't move, so no animation needed!
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        verticalBeam.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        horizontalBeam.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}