package zyo.narutomod.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.DefaultPlayerSkin;

public class CrowCloneRenderer extends LivingEntityRenderer<CrowCloneEntity, PlayerModel<CrowCloneEntity>> {

    public CrowCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CrowCloneEntity entity) {
        return entity.getOwnerUUID()
                .map(uuid -> net.minecraft.client.Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(new com.mojang.authlib.GameProfile(uuid, null)))
                .orElse(DefaultPlayerSkin.getDefaultSkin());
    }
}