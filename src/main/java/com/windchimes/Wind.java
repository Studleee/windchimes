package com.windchimes;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * One shared breeze for the whole world. Every chime reads the same wind, so they all lean the same way,
 * and gusts roll across the landscape in the wind's direction instead of hitting everything at once.
 * Pure math on game time and weather: it gives the same answer on the server and every client.
 */
public final class Wind {
	private static final float TICKS_PER_SECOND = 20.0F;
	private static final int ALTITUDE_START = 80;

	private Wind() {
	}

	/** Direction the wind blows toward, in radians (0 = east, PI/2 = south). Drifts slowly over minutes. */
	public static float direction(Level level, float partialTick) {
		float t = seconds(level, partialTick);
		return 1.3F * Mth.sin(t * 0.011F) + 0.9F * Mth.sin(t * 0.0037F + 2.0F);
	}

	/**
	 * How hard the wind is blowing at a position, roughly 0.1 (still air) to 1.5 (storm gust on a mountain).
	 * Rain and thunder fade in smoothly because they use the weather's transition levels.
	 */
	public static float strength(Level level, BlockPos pos, float partialTick) {
		float t = seconds(level, partialTick);
		float dir = direction(level, partialTick);

		float weather = 0.3F + 0.3F * level.getRainLevel(partialTick) + 0.4F * level.getThunderLevel(partialTick);
		float altitude = Mth.clamp((pos.getY() - ALTITUDE_START) / 100.0F, 0.0F, 0.35F);

		// Gust fronts travel downwind: chimes further along the wind direction get each gust a moment later.
		float travel = (pos.getX() * Mth.cos(dir) + pos.getZ() * Mth.sin(dir)) * 0.12F;
		float wave = 0.5F * Mth.sin(t * 0.9F - travel)
			+ 0.35F * Mth.sin(t * 0.37F - travel * 0.7F + 1.7F)
			+ 0.15F * Mth.sin(t * 0.13F + 0.5F);
		float gust = Mth.clamp(0.5F + 0.5F * wave, 0.0F, 1.0F);
		gust *= gust;

		return weather * (0.35F + 0.9F * gust) + altitude * gust;
	}

	public static float seconds(Level level, float partialTick) {
		return (level.getGameTime() + partialTick) / TICKS_PER_SECOND;
	}
}
