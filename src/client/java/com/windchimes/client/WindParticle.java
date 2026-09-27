package com.windchimes.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** A faint white wisp that drifts along with the wind and fades in and out. */
public class WindParticle extends SingleQuadParticle {
	private static final float MAX_ALPHA = 0.5F;

	private final SpriteSet sprites;
	private final float bobOffset;

	protected WindParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
			SpriteSet sprites, RandomSource random) {
		super(level, x, y, z, xd, yd, zd, sprites.first());
		this.sprites = sprites;
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.lifetime = 30 + random.nextInt(25);
		this.gravity = 0.0F;
		this.friction = 0.99F;
		this.hasPhysics = false;
		this.quadSize = 0.2F + random.nextFloat() * 0.15F;
		this.alpha = 0.0F;
		this.bobOffset = random.nextFloat() * Mth.TWO_PI;
		setSpriteFromAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		if (removed) {
			return;
		}
		setSpriteFromAge(sprites);
		float life = (float) age / lifetime;
		alpha = MAX_ALPHA * Mth.sin(life * Mth.PI);
		yd = Mth.sin(age * 0.25F + bobOffset) * 0.01;
	}

	@Override
	protected Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
				double xd, double yd, double zd, RandomSource random) {
			return new WindParticle(level, x, y, z, xd, yd, zd, sprites, random);
		}
	}
}
