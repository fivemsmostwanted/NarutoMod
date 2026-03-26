package zyo.narutomod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class FireballJutsuEntity extends ThrowableProjectile {

    // 1. Create the Synched Data ID for the scale
    private static final EntityDataAccessor<Float> FIREBALL_SCALE = SynchedEntityData.defineId(FireballJutsuEntity.class, EntityDataSerializers.FLOAT);

    public FireballJutsuEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireballJutsuEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FIREBALL_JUTSU.get(), shooter, level);

        // 2. Set the scale based on the shooter's stats right when it spawns!
        if (!level.isClientSide && shooter instanceof net.minecraft.world.entity.player.Player player) {
            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                int ninjutsuLevel = stats.getNinjutsuStat();

                // Base size is 1.0. Every level adds 0.1 to the size. (Level 10 = 2.0x size)
                float visualScale = 1.0F + (ninjutsuLevel * 0.1F);

                // Cap the visual size at 4.0x so it doesn't blind the whole screen
                this.entityData.set(FIREBALL_SCALE, Math.min(visualScale, 4.0F));
            });
        }
    }

    @Override
    protected void defineSynchedData() {
        // 3. Register the default scale as 1.0F
        this.entityData.define(FIREBALL_SCALE, 1.0F);
    }

    // 4. A quick getter for the Renderer to use
    public float getVisualScale() {
        return this.entityData.get(FIREBALL_SCALE);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            // Scale the amount and spread of particles based on how big the fireball is!
            float scale = this.getVisualScale();
            int particleCount = (int) (5 * scale);

            for (int i = 0; i < particleCount; i++) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + (0.5D * scale), this.getZ(), 0, 0, 0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + (0.5D * scale), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            net.minecraft.world.phys.Vec3 hitPos = result.getLocation();

            // Default base radius (Level 1)
            final float[] finalRadius = {3.0F};

            if (this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    int ninjutsuLevel = stats.getNinjutsuStat();

                    // Math: Base 3.0 + 0.5 per level.
                    float scaledRadius = 3.0F + (ninjutsuLevel * 0.5F);
                    finalRadius[0] = Math.min(scaledRadius, 10.0F);
                });
            }

            this.level().explode(this, hitPos.x, hitPos.y, hitPos.z, finalRadius[0], Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}