package com.goo.curiosities.client.particle.custom;

import com.goo.curiosities.util.Colors;
import com.goo.goo_lib.client.particle.TrailParticle;
import com.goo.goo_lib.client.particle.TrailParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MissileTrailParticle extends TrailParticle {


    public MissileTrailParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Integer entityId, float minVertexDistance, float width, boolean smooth, boolean bloomEnabled, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, entityId, minVertexDistance, width, smooth, bloomEnabled, spriteSet);
        this.nodeMaxLife = 20;
        if (!this.rawTails.isEmpty()) {
            TailNode headNode = this.rawTails.peekFirst();
            if (headNode != null) {
                headNode.life = this.nodeMaxLife;
                headNode.maxLife = this.nodeMaxLife;
            }
        }
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(float partialTicks) {
        return getBoundingBox();
    }



    @Override
    public void tick() {
        super.tick();
        if (!this.rawTails.isEmpty()) {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

            for (TailNode node : this.rawTails) {
                minX = Math.min(minX, node.pos.x);
                minY = Math.min(minY, node.pos.y);
                minZ = Math.min(minZ, node.pos.z);
                maxX = Math.max(maxX, node.pos.x);
                maxY = Math.max(maxY, node.pos.y);
                maxZ = Math.max(maxZ, node.pos.z);
            }

            // small 0.5 padding to avoid precision flickering on edges
            this.setBoundingBox(new AABB(minX - 0.5, minY - 0.5, minZ - 0.5, maxX + 0.5, maxY + 0.5, maxZ + 0.5));
        }
    }

    @Override
    public int getColorAtProgress(float progress, float partialTicks) {
        return FastColor.ARGB32.lerp(progress, Colors.FIRE[1], Colors.FIRE[0]);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<TrailParticleOption> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public @Nullable Particle createParticle(TrailParticleOption options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new MissileTrailParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options.entityId(), options.minVertexDistance(), options.width(), options.smoothInterpolation(), options.bloom(), this.spriteSet);
        }
    }
}
