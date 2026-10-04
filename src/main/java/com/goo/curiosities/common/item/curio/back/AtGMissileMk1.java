package com.goo.curiosities.common.item.curio.back;

import com.goo.curiosities.common.entity.MissileEntity;
import com.goo.curiosities.common.item.CuriositiesCurioItem;
import com.goo.curiosities.common.registry.CuriositiesEntities;
import com.goo.goo_lib.util.DelayedTaskScheduler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class AtGMissileMk1 extends CuriositiesCurioItem {
    public AtGMissileMk1(Properties properties) {
        super(properties);
    }

    @Override
    public void onWearerHit(LivingEntity attacker, Entity victim, ItemStack curio, DamageSource source, LivingDamageEvent.Pre event) {
        Level level = attacker.level();
        if (level.isClientSide()) return;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) return;
        for (int i = 0; i < attacker.getRandom().nextIntBetweenInclusive(1, 3); i++) {
            MissileEntity missile = new MissileEntity(CuriositiesEntities.MISSILE.value(), attacker.level());
            missile.setPos(attacker.position().add(0, attacker.getBbHeight() + 0.1, 0));
            missile.setOwner(attacker);
            missile.setTarget(victim);

            float attackerYaw = attacker.getYRot();
            float angle = 180 + attackerYaw + Mth.nextFloat(attacker.getRandom(), -60F, 60F);

            float xComp = Mth.cos(angle) * 0.5F;
            float zComp = Mth.sin(angle) * 0.5F;
            Vec3 initialVelocity = new Vec3(xComp, 1, zComp);
            missile.setDeltaMovement(initialVelocity);

            double horizontalDist = initialVelocity.horizontalDistance();
            float yaw = (float) (Mth.atan2(initialVelocity.x, initialVelocity.z) * (180.0D / Math.PI));
            float pitch = (float) (Mth.atan2(initialVelocity.y, horizontalDist) * (180.0D / Math.PI));

            missile.setYRot(yaw);
            missile.setXRot(pitch);
            missile.yRotO = yaw;
            missile.xRotO = pitch;

            if (i > 0) {
                DelayedTaskScheduler.queueServerWork(level, i, () -> {
                    level.addFreshEntity(missile);
                    level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                            SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS,
                            1.0F, Mth.nextFloat(attacker.getRandom(), 0.8F, 1.2F));
                });
            } else {
                level.addFreshEntity(missile);
                level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS,
                        1.0F, Mth.nextFloat(attacker.getRandom(), 0.8F, 1.2F));
            }
        }
    }
}
