package zyo.narutomod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class CrowCloneEntity extends PathfinderMob {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(CrowCloneEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public CrowCloneEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public java.util.Optional<java.util.UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MAX_HEALTH, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public void setOwner(Player player) {
        this.entityData.set(OWNER_UUID, Optional.of(player.getUUID()));

        double playerMaxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(playerMaxHealth);
        this.setHealth((float) playerMaxHealth);

        this.setCustomName(player.getName());
        this.setCustomNameVisible(true);
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {

            serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    50, 0.5D, 1.0D, 0.5D, 0.1D);

            this.level().playSound(null, this.blockPosition(), SoundEvents.BAT_TAKEOFF, SoundSource.HOSTILE, 1.0F, 0.8F);

            for (int i = 0; i < 4; i++) {
                net.minecraft.world.entity.ambient.Bat bat = net.minecraft.world.entity.EntityType.BAT.create(serverLevel);
                if (bat != null) {
                    bat.moveTo(this.getX(), this.getY() + 1.0, this.getZ(), this.random.nextFloat() * 360F, 0);
                    serverLevel.addFreshEntity(bat);
                }
            }
        }
        super.die(cause);
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }
}