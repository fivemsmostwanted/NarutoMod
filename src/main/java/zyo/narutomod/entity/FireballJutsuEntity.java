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

    private static final EntityDataAccessor<Float> FIREBALL_SCALE = SynchedEntityData.defineId(FireballJutsuEntity.class, EntityDataSerializers.FLOAT);

    public FireballJutsuEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireballJutsuEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FIREBALL_JUTSU.get(), shooter, level);

        if (!level.isClientSide && shooter instanceof net.minecraft.world.entity.player.Player player) {
            player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                int ninjutsuLevel = stats.getNinjutsuStat();
                float visualScale = 1.0F + (ninjutsuLevel * 0.1F);

                this.entityData.set(FIREBALL_SCALE, Math.min(visualScale, 4.0F));
            });
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(FIREBALL_SCALE, 1.0F);
    }

    public float getVisualScale() {
        return this.entityData.get(FIREBALL_SCALE);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            float scale = this.getVisualScale();
            int particleCount = (int) (5 * scale);

            for (int i = 0; i < particleCount; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * scale;
                double offsetY = (this.random.nextDouble() - 0.5) * scale;
                double offsetZ = (this.random.nextDouble() - 0.5) * scale;

                this.level().addParticle(zyo.narutomod.particle.ModParticles.CUSTOM_FLAME.get(), this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            net.minecraft.world.phys.Vec3 hitPos = result.getLocation();
            final float[] finalRadius = {3.0F};

            if (this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                player.getCapability(zyo.narutomod.capability.ShinobiDataProvider.SHINOBI_DATA).ifPresent(stats -> {
                    int ninjutsuLevel = stats.getNinjutsuStat();
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