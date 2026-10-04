package com.goo.curiosities.common.item.curio.charm;

import com.goo.curiosities.common.item.CuriositiesCurioItem;
import com.goo.curiosities.common.registry.CuriositiesItems;
import com.goo.curiosities.util.CurioUtil;
import com.goo.curiosities.util.EntityUtil;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;

public class Gasoline extends CuriositiesCurioItem {
    public Gasoline(Properties properties) {
        super(properties);
    }

    @Override
    public void onWearerKill(LivingEntity attacker, Entity victim, ItemStack curio, DamageSource source, LivingDeathEvent event) {
        burnNearby(attacker, victim.position());
    }

    protected static void burnNearby(LivingEntity attacker, Vec3 position) {
        List<LivingEntity> nearby = EntityUtil.getNearbyEnemies(attacker, position, 5);
        burnNearby(nearby);
    }

    protected static void burnNearby(List<LivingEntity> toBurn) {
        toBurn.forEach(e -> {
            int currentTicks = e.getRemainingFireTicks();
            e.igniteForTicks(currentTicks + 200);
        });
    }

    public static void proc(DamageSource source, LivingEntity victim) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            List<LivingEntity> nearbyMobsWithGasoline = victim.level().getNearbyEntities(
                    LivingEntity.class,
                    TargetingConditions.forNonCombat()
                            .ignoreInvisibilityTesting()
                            .ignoreLineOfSight()
                            .selector(e -> CurioUtil.isWearingCurio(e, CuriositiesItems.GASOLINE.value())),
                    victim,
                    victim.getBoundingBox().inflate(15)
            );

            if (!nearbyMobsWithGasoline.isEmpty()) {
                Entity attacker = source.getEntity();
                LivingEntity centerOwner = attacker instanceof LivingEntity living ? living : victim;

                // find enemies surrounding the burning victim relative to the attacker
                List<LivingEntity> nearby = EntityUtil.getNearbyEnemies(centerOwner, victim.position(), 5);

                // ensure victim and nearby entities burn
                if (!nearby.contains(victim)) {
                    nearby.add(victim);
                }

                burnNearby(nearby);
            }
        }
    }
}
