package zyo.narutomod.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SusanooEntity extends Mob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LivingEntity owner;

    private static final EntityDataAccessor<Integer> TIER = SynchedEntityData.defineId(SusanooEntity.class, EntityDataSerializers.INT);
    public SusanooEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TIER, 1); // Defaults to Tier 1 (Ribcage)
    }

    public int getTier() {
        return this.entityData.get(TIER);
    }

    public void setTier(int tier) {
        this.entityData.set(TIER, tier);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean canBeCollidedWith() { return false; }

    @Override
    public boolean shouldRiderSit() { return false; }

    @Override
    public double getMyRidingOffset() { return -1.5D; }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Discard instantly if the owner is gone, dead, or the Susanoo gets dismounted
            if (this.owner == null || !this.owner.isAlive() || this.getVehicle() != this.owner) {
                this.discard();
                return;
            }

            this.setPos(this.owner.getX(), this.owner.getY(), this.owner.getZ());
            this.setYRot(this.owner.getYRot());
            this.setXRot(this.owner.getXRot());
            this.yBodyRot = this.owner.yBodyRot;
            this.yHeadRot = this.owner.yHeadRot;

            double radius = 3.0D;
            AABB detectionBox = this.getBoundingBox().inflate(radius);

            for (Projectile projectile : this.level().getEntitiesOfClass(Projectile.class, detectionBox)) {
                if (projectile.isAlive() && !projectile.isRemoved()) {
                    Vec3 projPos = projectile.position();
                    Vec3 susanoPos = this.position();
                    Vec3 toProj = projPos.subtract(susanoPos);

                    Vec3 velocity = projectile.getDeltaMovement();
                    if (velocity.dot(toProj) < 0) {
                        projectile.setDeltaMovement(velocity.multiply(-1, -1, -1));
                    }
                }
            }
        }
    }

    private PlayState predicate(AnimationState<SusanooEntity> event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        event.getController().setAnimationSpeed(0.15f);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}