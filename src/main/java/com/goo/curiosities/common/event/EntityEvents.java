package com.goo.curiosities.common.event;

import com.goo.curiosities.client.render.ClientGlitchState;
import com.goo.curiosities.common.Curiosities;
import com.goo.curiosities.common.entity.goals.RegalEmeraldFollowGoal;
import com.goo.curiosities.common.registry.CuriositiesItems;
import com.goo.curiosities.util.CurioUtil;
import com.goo.curiosities.util.FootstepTracker;
import com.goo.goo_lib.common.mob_effect.passive.PassiveMobEffectEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

/**
 * Use for events that are for non-living entities, or more specifically, all entities, living-exclusive events should go in {@link net.minecraft.world.entity.LivingEntity}
 */
@EventBusSubscriber(modid = Curiosities.MOD_ID)
public class EntityEvents {
    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            PassiveMobEffectEngine.clearEntity(livingEntity);
            if (livingEntity.level().isClientSide()) {
                ClientGlitchState.clear(livingEntity);
            }
        }

        if (event.getEntity() instanceof Player player) {
            if (player.level().isClientSide()) {
                FootstepTracker.remove(player);
            }
        }
    }
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            villager.goalSelector.addGoal(0, new RegalEmeraldFollowGoal(villager));
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.getOwner() instanceof LivingEntity livingEntity) {
            if (CurioUtil.isWearingCurio(livingEntity, CuriositiesItems.EXPLOSIVE_QUIVER.value())) {
                if (projectile.level() instanceof ServerLevel serverLevel) {
                    Vec3 pos = event.getRayTraceResult().getLocation();
                    serverLevel.explode(projectile, pos.x(), pos.y(), pos.z(), 0.5F, Level.ExplosionInteraction.NONE);
                }
            }
        }
    }

}
