package com.goo.curiosities.common.item.curio.back.quiver;

import com.goo.curiosities.common.item.QuiverCurioItem;
import com.goo.curiosities.common.registry.CuriositiesEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ElementalQuiver extends QuiverCurioItem {
    public ElementalQuiver(Properties properties) {
        super(properties);
    }

    @Override
    public void onWearerHit(LivingEntity attacker, Entity victim, ItemStack curio, DamageSource source, LivingDamageEvent.Pre event) {
        if (source.is(DamageTypes.ARROW)) {
            int ticks = 60;
            victim.igniteForSeconds(ticks / 20F);
            if (victim instanceof LivingEntity livingVictim) {
                livingVictim.addEffect(new MobEffectInstance(MobEffects.WITHER, ticks, 0), attacker);
                livingVictim.addEffect(new MobEffectInstance(MobEffects.POISON, ticks, 0), attacker);
                livingVictim.addEffect(new MobEffectInstance(CuriositiesEffects.FROZEN, ticks, 0), attacker);
            }
        }
    }
}
