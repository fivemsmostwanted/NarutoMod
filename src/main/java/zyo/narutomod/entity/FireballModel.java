package zyo.narutomod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;

public class FireballModel<T extends FireballJutsuEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "fireball_jutsu"), "main");

    // Define your layers here!
    private final ModelPart outer_shell;
    private final ModelPart inner_core;

    public FireballModel(ModelPart root) {
        // Make sure these names match what you type in createBodyLayer()
        this.outer_shell = root.getChild("outer_shell");
        this.inner_core = root.getChild("inner_core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // REPLACE THIS SECTION WITH YOUR BLOCKBENCH EXPORT!
        // Make sure you have one large shape (outer_shell) and one smaller shape inside it (inner_core)
        partdefinition.addOrReplaceChild("outer_shell", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("inner_core", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    // THE MAGIC TRICK #2: Spin the layers!
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Spin the outer shell slowly in one direction
        this.outer_shell.xRot = ageInTicks * 0.1F;
        this.outer_shell.yRot = ageInTicks * 0.1F;
        this.outer_shell.zRot = ageInTicks * 0.05F;

        // Spin the inner core a bit faster in the opposite direction
        this.inner_core.xRot = -ageInTicks * 0.2F;
        this.inner_core.yRot = -ageInTicks * 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        outer_shell.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        inner_core.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}