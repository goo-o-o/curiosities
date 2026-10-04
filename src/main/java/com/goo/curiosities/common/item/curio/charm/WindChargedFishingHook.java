package com.goo.curiosities.common.item.curio.charm;

import com.goo.curiosities.common.item.CuriositiesCurioItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

public class WindChargedFishingHook extends CuriositiesCurioItem {
    public WindChargedFishingHook(Properties properties) {
        super(properties);
    }

    public static void explode(LivingEntity source,  Vec3 pos, float strength) {
        source.level()
                .explode(
                        source,
                        null,
                        new SimpleExplosionDamageCalculator(
                                true, false, Optional.of(1.22F * strength), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
                        ),
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        1.2F * strength,
                        false,
                        Level.ExplosionInteraction.TRIGGER,
                        ParticleTypes.GUST_EMITTER_SMALL,
                        ParticleTypes.GUST_EMITTER_LARGE,
                        SoundEvents.WIND_CHARGE_BURST
                );
    }
}
