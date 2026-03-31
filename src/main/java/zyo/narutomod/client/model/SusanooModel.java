package zyo.narutomod.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.entity.SusanooEntity;

public class SusanooModel extends GeoModel<SusanooEntity> {
    @Override
    public ResourceLocation getModelResource(SusanooEntity object) {
        if (object.getTier() == 1) {
            return ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "geo/susanoo_ribcage.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "geo/susano.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SusanooEntity object) {
        return ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/entity/susano.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SusanooEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "animations/susano.animation.json");
    }
}