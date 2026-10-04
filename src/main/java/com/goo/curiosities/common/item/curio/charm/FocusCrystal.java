package com.goo.curiosities.common.item.curio.charm;

import com.goo.curiosities.common.item.CuriositiesCurioItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class FocusCrystal extends CuriositiesCurioItem {
    public FocusCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public void onWearerHit(LivingEntity attacker, Entity victim, ItemStack curio, DamageSource source, LivingDamageEvent.Pre event) {
        if (attacker.distanceToSqr(victim) <= 25) {
            event.setNewDamage(event.getNewDamage() * 1.2F);
        }
    }
}
