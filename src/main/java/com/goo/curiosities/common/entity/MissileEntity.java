package com.goo.curiosities.common.entity;

import com.goo.curiosities.client.registry.CuriositiesParticles;
import com.goo.goo_lib.client.particle.TrailParticleOption;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class MissileEntity extends AbstractArrow implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final double MAX_SPEED = 0.8D;
    private static final double ACCELERATION = 0.05D;
    private static final double HOMING_RADIUS = 32.0D;
    private static final float MAX_TURN_DEGREES_PER_TICK = 20F;

    @Nullable
    private Entity target;

    public MissileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }

    @Override
    public void tick() {
        if (this.firstTick && this.level().isClientSide()) {
            TrailParticleOption particleOption = new TrailParticleOption(
                    CuriositiesParticles.MISSILE_TRAIL.get(),
                    this.getId(),
                    0.1F,
                    0.15F,
                    true,
                    true
            );

            this.level().addParticle(
                    particleOption, true,
                    this.getX(),
                    this.getY(0.5),
                    this.getZ(),
                    0, 0, 0
            );
        }

        if (this.tickCount > 200) {
            this.discard();
            return;
        }

        if (this.inGround) {
            super.tick();
            return;
        }

        // only execute tracking logic on server side to prevent client/server movement desync
        if (!this.level().isClientSide()) {
            findTargetIfNeeded();

            if (this.target != null && this.target.isAlive()) {
                applySmoothHoming(this.target);
            } else {
                double currentSpeed = Math.min(this.getDeltaMovement().length() + ACCELERATION, MAX_SPEED);
                if (currentSpeed > 0.001D) {
                    this.setDeltaMovement(this.getDeltaMovement().normalize().scale(currentSpeed));
                    this.hasImpulse = true;
                }
            }
        }

        super.tick();

        // sync pitch and yaw to delta movement vector on both sides
        Vec3 vel = this.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-7D) {
            double horizontalDistance = vel.horizontalDistance();

            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();

            float targetYaw = (float) (Mth.atan2(vel.x, vel.z) * (180.0D / Math.PI));
            float targetPitch = (float) (Mth.atan2(vel.y, horizontalDistance) * (180.0D / Math.PI));

            this.setYRot(this.yRotO + Mth.wrapDegrees(targetYaw - this.yRotO));
            this.setXRot(this.xRotO + Mth.wrapDegrees(targetPitch - this.xRotO));

            // align old rotations on first tick to prevent start-frame interpolation jump
            if (this.firstTick) {
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
            }
        }
    }

    private void findTargetIfNeeded() {
        if (this.target == null || !this.target.isAlive()) {
            List<Entity> candidates = this.level().getEntities(
                    this,
                    this.getBoundingBox().inflate(HOMING_RADIUS),
                    e -> e.isAlive() && !e.isSpectator() && e != this.getOwner() && !(e instanceof AbstractArrow)
            );

            double closestDistSqr = -1.0D;
            for (Entity candidate : candidates) {
                double distSqr = this.distanceToSqr(candidate);
                if (closestDistSqr == -1.0D || distSqr < closestDistSqr) {
                    closestDistSqr = distSqr;
                    this.target = candidate;
                }
            }
        }
    }

    private void applySmoothHoming(Entity target) {
        Vec3 currentPos = this.position();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 currentVel = this.getDeltaMovement();

        double currentSpeed = currentVel.length();
        double newSpeed = Math.min(currentSpeed + ACCELERATION, MAX_SPEED);
        if (newSpeed < 0.2D) newSpeed = 0.2D;

        Vec3 idealDir = targetCenter.subtract(currentPos).normalize();
        Vec3 currentDir = currentSpeed > 0.001D ? currentVel.normalize() : idealDir;
        Vec3 steeredDir = rotateTowards(currentDir, idealDir, MAX_TURN_DEGREES_PER_TICK * Math.min(tickCount / 10, 1));

        this.setDeltaMovement(steeredDir.scale(newSpeed));
        this.hasImpulse = true;
    }

    private Vec3 rotateTowards(Vec3 current, Vec3 target, float maxDegrees) {
        double dot = Mth.clamp(current.dot(target), -1.0D, 1.0D);
        double angleBetween = Math.acos(dot);
        double maxRadians = Math.toRadians(maxDegrees);

        if (angleBetween <= maxRadians || angleBetween < 0.0001D) {
            return target;
        }

        double t = maxRadians / angleBetween;
        double sinAngle = Math.sin(angleBetween);

        double weightCurrent = Math.sin((1.0D - t) * angleBetween) / sinAngle;
        double weightTarget = Math.sin(t * angleBetween) / sinAngle;

        return current.scale(weightCurrent).add(target.scale(weightTarget)).normalize();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0F, Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (result.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.invulnerableTime = 0;
        }
        result.getEntity().hurt(result.getEntity().damageSources().explosion(getOwner(), this), 1);
    }

    @Override
    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return Items.ARROW.getDefaultInstance();
    }

    @Override
    protected boolean tryPickup(Player player) {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }
}