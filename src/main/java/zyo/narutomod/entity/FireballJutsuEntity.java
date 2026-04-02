package zyo.narutomod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import zyo.narutomod.particle.ModParticles;

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
                float visualScale = 1.0F + (ninjutsuLevel * 0.025F);
                this.entityData.set(FIREBALL_SCALE, Math.min(visualScale, 30.0F));
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FIREBALL_SCALE.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(Pose pose) {
        // Cap physical collision at 2.5 so it doesn't instantly blow up in your face on cast
        float physicalScale = Math.min(this.getVisualScale(), 2.5F);
        return super.getDimensions(pose).scale(physicalScale);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        // Prevents the massive fireball from turning invisible if the center goes off-screen
        return this.getBoundingBox().inflate(this.getVisualScale());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Visual radius is half of the visual scale. We use 0.45F to represent the "front edge"
            // of the sphere with a tiny bit of leeway so it looks like it truly impacts.
            float visualRadius = this.getVisualScale() * 0.45F;
            net.minecraft.world.phys.Vec3 center = this.position();
            net.minecraft.world.phys.Vec3 movement = this.getDeltaMovement();

            if (movement.lengthSqr() > 0.0001D) {
                net.minecraft.world.phys.Vec3 dir = movement.normalize();

                // 1. Forward Raytrace for Mountain/Block Impacts
                net.minecraft.world.phys.Vec3 frontEdge = center.add(dir.scale(visualRadius));
                net.minecraft.world.phys.BlockHitResult blockHit = this.level().clip(new net.minecraft.world.level.ClipContext(
                        center, frontEdge,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        this));

                if (blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    this.onHit(blockHit);
                    return; // Stop ticking, the fireball just exploded!
                }
            }

            net.minecraft.world.phys.AABB massiveBox = this.getBoundingBox().inflate(visualRadius);
            java.util.List<net.minecraft.world.entity.Entity> entities = this.level().getEntities(this, massiveBox, e ->
                    !e.isSpectator() && e.isPickable() && e != this.getOwner()
            );

            for (net.minecraft.world.entity.Entity e : entities) {
                if (e.distanceToSqr(this) <= (visualRadius * visualRadius)) {
                    this.onHit(new net.minecraft.world.phys.EntityHitResult(e));
                    return;
                }
            }
        }

        if (this.level().isClientSide) {
            float scale = this.getVisualScale();
            int particleCount = Math.min((int) (12 * scale), 150);

            for (int i = 0; i < particleCount; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * (scale * 0.4);
                double offsetY = (this.random.nextDouble() - 0.5) * (scale * 0.4);
                double offsetZ = (this.random.nextDouble() - 0.5) * (scale * 0.4);

                this.level().addParticle(ModParticles.CUSTOM_FLAME.get(),
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        0, 0.05, 0);

                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        0, 0.02, 0);
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
                    float scaledRadius = 3.0F + (ninjutsuLevel * 0.015F);
                    finalRadius[0] = Math.min(scaledRadius, 20.0F);
                });
            }

            this.level().explode(this, hitPos.x, hitPos.y, hitPos.z, finalRadius[0], true, Level.ExplosionInteraction.MOB);
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