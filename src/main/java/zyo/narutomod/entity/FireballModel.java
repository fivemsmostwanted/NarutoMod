package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;

public class FireballModel<T extends FireballJutsuEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "fireball_jutsu"), "main");
    private final ModelPart mainSphere;

    public FireballModel(ModelPart root) {
        this.mainSphere = root.getChild("main_sphere");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("main_sphere", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F) // Core
                        .texOffs(0, 16).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 10.0F, 6.0F) // Y-Pillar (Top/Bottom)
                        .texOffs(24, 0).addBox(-5.0F, -3.0F, -3.0F, 10.0F, 6.0F, 6.0F) // X-Pillar (Left/Right)
                        .texOffs(0, 23).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 10.0F), // Z-Pillar (Front/Back)
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.mainSphere.xRot = ageInTicks * 0.15F;
        this.mainSphere.yRot = ageInTicks * 0.2F;
        this.mainSphere.zRot = ageInTicks * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        mainSphere.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}