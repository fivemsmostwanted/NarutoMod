package zyo.narutomod.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "narutomod", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HandSignKeys {
    public static final String CATEGORY = "key.categories.naruto_mod";

    public static final KeyMapping CHARGE_KEY = new KeyMapping("key.naruto_mod.charge", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);

    public static final KeyMapping SIGN_1 = new KeyMapping("key.narutomod.sign_1", GLFW.GLFW_KEY_Z, CATEGORY);
    public static final KeyMapping SIGN_2 = new KeyMapping("key.narutomod.sign_2", GLFW.GLFW_KEY_X, CATEGORY);
    public static final KeyMapping SIGN_3 = new KeyMapping("key.narutomod.sign_3", GLFW.GLFW_KEY_C, CATEGORY);
    public static final KeyMapping SIGN_4 = new KeyMapping("key.narutomod.sign_4", GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping SIGN_5 = new KeyMapping("key.narutomod.sign_5", GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping SIGN_6 = new KeyMapping("key.narutomod.sign_6", GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping SIGN_7 = new KeyMapping("key.narutomod.sign_7", GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping SIGN_8 = new KeyMapping("key.narutomod.sign_8", GLFW.GLFW_KEY_F, CATEGORY);

    public static final KeyMapping SHARINGAN_KEY = new KeyMapping("key.naruto_mod.sharingan", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping AMENO_KEY = new KeyMapping("key.naruto_mod.ameno", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    public static final KeyMapping GENJUTSU_MODIFIER = new KeyMapping("key.naruto_mod.genjutsu_modifier", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping MENU_KEY = new KeyMapping("key.naruto_mod.menu_key", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_INSERT, CATEGORY);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CHARGE_KEY);
        event.register(SIGN_1);
        event.register(SIGN_2);
        event.register(SIGN_3);
        event.register(SIGN_4);
        event.register(SIGN_5);
        event.register(SIGN_6);
        event.register(SIGN_7);
        event.register(SIGN_8);
        event.register(SHARINGAN_KEY);
        event.register(AMENO_KEY);
        event.register(GENJUTSU_MODIFIER);
        event.register(MENU_KEY);
    }
}