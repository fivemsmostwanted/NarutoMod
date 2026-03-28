package zyo.narutomod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import zyo.narutomod.NarutoMod;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NarutoMod.MODID);

    public static final RegistryObject<EntityType<FireballJutsuEntity>> FIREBALL_JUTSU = ENTITIES.register("fireball_jutsu",
            () -> EntityType.Builder.<FireballJutsuEntity>of(FireballJutsuEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("fireball_jutsu"));

    public static final RegistryObject<EntityType<SubstitutionLogEntity>> SUBSTITUTION_LOG = ENTITIES.register("substitution_log",
            () -> EntityType.Builder.<SubstitutionLogEntity>of(SubstitutionLogEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.2F)
                    .clientTrackingRange(8) // Tells the game to sync position up to 8 blocks away
                    .updateInterval(2)      // Sends a movement update every 2 ticks (10 times a second)
                    .build("substitution_log"));

    public static final RegistryObject<EntityType<CrowCloneEntity>> CROW_CLONE = ENTITIES.register("crow_clone",
            () -> EntityType.Builder.<CrowCloneEntity>of(CrowCloneEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("crow_clone"));

    public static final RegistryObject<EntityType<ShacklingStakeEntity>> SHACKLING_STAKE = ENTITIES.register("shackling_stake",
            () -> EntityType.Builder.<ShacklingStakeEntity>of(ShacklingStakeEntity::new, MobCategory.MISC)
                    .sized(0.5F, 2.0F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build("shackling_stake"));
}