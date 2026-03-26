package zyo.narutomod.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import zyo.narutomod.NarutoMod;

import java.util.IdentityHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = NarutoMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PlayerAnimManager {

    // Tracks the animation layer attached to each specific player
    private static final Map<AbstractClientPlayer, ModifierLayer<IAnimation>> ANIMATION_LAYERS = new IdentityHashMap<>();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Hijacks the vanilla player model and injects a custom animation layer with priority 1000
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            animationStack.addAnimLayer(1000, layer);
            ANIMATION_LAYERS.put(player, layer);
        });
    }

    public static void playAnimation(AbstractClientPlayer player, String animationName) {
        ModifierLayer<IAnimation> layer = ANIMATION_LAYERS.get(player);
        if (layer == null) return;

        KeyframeAnimation animation = PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, animationName));

        if (animation != null) {
            layer.setAnimation(new KeyframeAnimationPlayer(animation));
        } else {
            System.out.println("Could not find animation: " + animationName);
        }
    }

    public static void stopAnimation(AbstractClientPlayer player) {
        ModifierLayer<IAnimation> layer = ANIMATION_LAYERS.get(player);
        if (layer != null) {
            layer.setAnimation(null); // Setting it to null cleanly returns control to vanilla walking/running
        }
    }
}