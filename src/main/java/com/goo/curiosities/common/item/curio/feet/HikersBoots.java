package com.goo.curiosities.common.item.curio.feet;

import com.goo.curiosities.common.item.CuriositiesCurioItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class HikersBoots extends CuriositiesCurioItem {
    public HikersBoots(Properties properties) {
        super(properties);
    }

    @Override
    public void onWearerMeleeHit(LivingEntity attacker, Entity victim, ItemStack curio, DamageSource damageSource, LivingDamageEvent.Pre event) {
        float difference = (float) (attacker.getY() - victim.getY());
        float bonus = 1 + (difference * 0.2F);
        event.setNewDamage(event.getNewDamage() * bonus);
    }
}
