package zyo.narutomod.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class FireballFlameParticle extends TextureSheetParticle {

    protected FireballFlameParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.hasPhysics = false; // Fire shouldn't bounce off walls clunkily
        this.sprites = spriteSet;

        // --- THIS MAKES IT LOOK LIKE THE MOD ---
        this.quadSize *= 2.5F; // Make individual flames larger
        this.lifetime = 10 + this.random.nextInt(12); // Short life so they don't linger too far back
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        // PARTICLE_SHEET_TRANSLUCENT is key for that soft, non-blocky look
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        // Forces the particle to always glow at maximum brightness (Fire logic)
        return 15728880;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        // Fade out at the end of life
        this.alpha = 1.0F - ((float)this.age / (float)this.lifetime);
    }

    private final SpriteSet sprites;

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FireballFlameParticle(level, x, y, z, this.spriteSet, xSpeed, ySpeed, zSpeed);
        }
    }
}