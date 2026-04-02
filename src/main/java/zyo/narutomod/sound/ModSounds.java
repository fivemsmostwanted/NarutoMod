package zyo.narutomod.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import zyo.narutomod.NarutoMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NarutoMod.MODID);

    public static final RegistryObject<SoundEvent> HANDSIGN = registerSoundEvent("handsign");
    public static final RegistryObject<SoundEvent> JUTSU_CAST = registerSoundEvent("jutsu_cast");
    public static final RegistryObject<SoundEvent> JUTSU_FAIL = registerSoundEvent("jutsu_fail");
    public static final RegistryObject<SoundEvent> CHAKRA_CHARGE = registerSoundEvent("chakra_charge");
    public static final RegistryObject<SoundEvent> DOJUTSU = registerSoundEvent("dojutsu");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}