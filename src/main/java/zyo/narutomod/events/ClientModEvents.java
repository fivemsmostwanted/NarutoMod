package zyo.narutomod.events;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.client.gui.DebugOverlay;
import zyo.narutomod.entity.*;
import zyo.narutomod.client.model.SubstitutionLogModel;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    public static final ModelLayerLocation TSUKUYOMI_CROSS_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "tsukuyomi_cross"), "main");
    public static final ModelLayerLocation CHIDORI_MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "chidori_orb"), "main");

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FIREBALL_JUTSU.get(), zyo.narutomod.entity.FireballRenderer::new);
        event.registerEntityRenderer(ModEntities.SHADOW_CLONE.get(), CrowCloneRenderer::new);
        event.registerEntityRenderer(ModEntities.SUBSTITUTION_LOG.get(), SubstitutionLogRenderer::new);
        event.registerEntityRenderer(zyo.narutomod.entity.ModEntities.SHACKLING_STAKE.get(), ShacklingStakeRenderer::new);
        event.registerEntityRenderer(ModEntities.SUSANOO.get(), zyo.narutomod.entity.SusanooRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TSUKUYOMI_CROSS_LAYER, zyo.narutomod.entity.TsukuyomiCrossModel::createBodyLayer);
        event.registerLayerDefinition(SubstitutionLogModel.LAYER_LOCATION, SubstitutionLogModel::createBodyLayer);
        event.registerLayerDefinition(zyo.narutomod.client.model.ShacklingStakeModel.LAYER_LOCATION, zyo.narutomod.client.model.ShacklingStakeModel::createBodyLayer);
        event.registerLayerDefinition(zyo.narutomod.client.model.AkatsukiCloakModel.LAYER_LOCATION, zyo.narutomod.client.model.AkatsukiCloakModel::createLayer);

        event.registerLayerDefinition(CHIDORI_MODEL_LAYER, ChidoriModel::createBodyLayer);
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        attachPlayerLayers(event, "default");
        attachPlayerLayers(event, "slim");
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        //event.registerAboveAll("shinobi_debug", DebugOverlay.HUD_DEBUG);
    }

    @SuppressWarnings("deprecation")
    private static void attachPlayerLayers(EntityRenderersEvent.AddLayers event, String skinName) {
        net.minecraft.client.renderer.entity.player.PlayerRenderer renderer = event.getSkin(skinName);
        if (renderer != null) {
            renderer.addLayer(new zyo.narutomod.client.SharinganEyeLayer(renderer));
            renderer.addLayer(new zyo.narutomod.client.TsukuyomiCrossLayer<>(renderer, event.getEntityModels()));

            renderer.addLayer(new ChidoriLayer(renderer));
        }
    }

    @SubscribeEvent
    public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        // enqueueWork ensures this runs safely on the main client thread
        event.enqueueWork(() -> {
            // 1. Register the hardcoded jutsu logic on the client
            zyo.narutomod.jutsu.JutsuRegistry.registerAll();

            // 2. Build the visual UI trees
            zyo.narutomod.jutsu.JutsuTreeManager.initializeTrees();
        });
    }
}