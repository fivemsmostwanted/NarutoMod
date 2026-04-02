package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class ChidoriModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart shell1;
    private final ModelPart shell2;
    private float pulseScale = 1.0F;

    public ChidoriModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
        this.shell1 = root.getChild("shell1");
        this.shell2 = root.getChild("shell2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // The Core: A solid 6x6x6 box in the center
        partdefinition.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);

        // Shell 1: An 8x8x8 box that we will rotate diagonally
        partdefinition.addOrReplaceChild("shell1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);

        // Shell 2: A 9x9x9 box rotated on the opposite diagonal
        partdefinition.addOrReplaceChild("shell2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.5F, -4.5F, -4.5F, 9.0F, 9.0F, 9.0F), PartPose.ZERO);

        // WE ARE USING 64x64 HERE. (See the texture warning below!)
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Spin the shells at high speed to create a chaotic spherical blur
        this.shell1.xRot = ageInTicks * 0.5F;
        this.shell1.yRot = ageInTicks * 0.8F;
        this.shell1.zRot = ageInTicks * 0.4F;

        this.shell2.xRot = -ageInTicks * 0.7F;
        this.shell2.yRot = -ageInTicks * 0.5F;
        this.shell2.zRot = ageInTicks * 0.9F;

        // Make the whole sphere pulse slightly
        this.pulseScale = 1.0F + (float) Math.sin(ageInTicks * 0.6F) * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        poseStack.scale(pulseScale, pulseScale, pulseScale);
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }

    @Override
    public ModelPart root() { return root; }
}