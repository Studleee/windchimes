package com.windchimes.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.windchimes.Wind;
import com.windchimes.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Plays the chime like a real one: each of the four rods has its own note, and the wind knocks the striker
 * into the rods at uneven moments, often clattering into two or three in a row so the notes ring over each other.
 * Gusts make strikes come faster. All of this runs on the client; nothing is saved to disk.
 */
public class WindChimeBlockEntity extends BlockEntity {
	public static final int STRIKE_EVENT = 0;
	public static final int RODS = 4;
	/** Index used for the center striker in {@link #lastStrikeTick(int)}. */
	public static final int STRIKER = RODS;

	/** Rod indexes from longest to shortest; longer rods get lower notes. Matches WindChimeRenderer. */
	private static final int[] RODS_BY_LENGTH = {2, 0, 3, 1};
	/**
	 * Chance per tick (20 per second) of a strike. Below CALM_WIND only the rare stray note sounds, so the
	 * lulls between gusts are quiet; above it, strikes pick up with the wind.
	 */
	private static final float BASE_STRIKE_CHANCE = 0.003F;
	private static final float CALM_WIND = 0.15F;
	private static final float STRIKE_CHANCE_PER_WIND = 0.12F;
	private static final long NEVER = Long.MIN_VALUE / 2;

	private final long[] lastStrikeTick = new long[RODS + 1];
	private final List<Strike> pending = new ArrayList<>();
	private int[] rodNotes;

	private record Strike(long tick, int rod, float volume) {
	}

	public WindChimeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WIND_CHIME, pos, state);
		Arrays.fill(lastStrikeTick, NEVER);
	}

	public long lastStrikeTick(int rod) {
		return lastStrikeTick[rod];
	}

	/** Right-clicking sends this event to every nearby player, who each hear a quick tumble of notes. */
	@Override
	public boolean triggerEvent(int id, int param) {
		if (id != STRIKE_EVENT) {
			return super.triggerEvent(id, param);
		}
		if (level != null && level.isClientSide()) {
			RandomSource random = level.getRandom();
			long tick = level.getGameTime();
			int count = 3 + random.nextInt(3);
			for (int i = 0; i < count; i++) {
				pending.add(new Strike(tick, random.nextInt(RODS), 1.0F - i * 0.12F));
				tick += 1 + random.nextInt(4);
			}
		}
		return true;
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, WindChimeBlockEntity chime) {
		if (state.getValue(WindChimeBlock.MUFFLED)) {
			chime.pending.clear();
			return;
		}
		RandomSource random = level.getRandom();
		long now = level.getGameTime();
		float wind = Wind.strength(level, pos, 0.0F);

		if (random.nextFloat() < BASE_STRIKE_CHANCE + Math.max(0.0F, wind - CALM_WIND) * STRIKE_CHANCE_PER_WIND) {
			int rod = random.nextInt(RODS);
			float volume = Mth.clamp(0.45F + wind * 0.35F, 0.45F, 1.0F) * (0.65F + random.nextFloat() * 0.35F);
			chime.pending.add(new Strike(now, rod, volume));

			// The striker swings on and clatters into more rods while the first is still ringing.
			long tick = now;
			float clatterChance = 0.35F + wind * 0.3F;
			while (random.nextFloat() < clatterChance) {
				tick += random.nextInt(5);
				rod = (rod + 1 + random.nextInt(RODS - 1)) % RODS;
				volume *= 0.7F + random.nextFloat() * 0.25F;
				chime.pending.add(new Strike(tick, rod, volume));
				clatterChance *= 0.6F;
			}
		}

		Iterator<Strike> it = chime.pending.iterator();
		while (it.hasNext()) {
			Strike strike = it.next();
			if (strike.tick() <= now) {
				chime.play(level, pos, strike, random);
				it.remove();
			}
		}
	}

	private void play(Level level, BlockPos pos, Strike strike, RandomSource random) {
		if (!(getBlockState().getBlock() instanceof WindChimeBlock block)) {
			return;
		}
		ChimeVoice voice = block.voice();
		float pitch = ChimeVoice.pitch(rodNotes(voice)[strike.rod()]) * (1.0F + (random.nextFloat() - 0.5F) * 0.012F);
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 1.0 - 0.6 * block.length();
		double z = pos.getZ() + 0.5;
		level.playLocalSound(x, y, z, voice.sound(), SoundSource.BLOCKS, voice.volume() * strike.volume(), pitch, false);
		if (voice.undertone() != null) {
			level.playLocalSound(x, y, z, voice.undertone(), SoundSource.BLOCKS,
				voice.volume() * voice.undertoneVolume() * strike.volume(), pitch, false);
		}
		long now = level.getGameTime();
		lastStrikeTick[strike.rod()] = now;
		lastStrikeTick[STRIKER] = now;
	}

	/** Four different notes from the voice's scale, picked by position so every chime has its own tuning. */
	private int[] rodNotes(ChimeVoice voice) {
		if (rodNotes == null) {
			int[] scale = voice.notes().clone();
			RandomSource random = RandomSource.create(Mth.getSeed(worldPosition));
			for (int i = scale.length - 1; i > 0; i--) {
				int j = random.nextInt(i + 1);
				int swap = scale[i];
				scale[i] = scale[j];
				scale[j] = swap;
			}
			int[] chosen = Arrays.copyOf(scale, RODS);
			Arrays.sort(chosen);
			rodNotes = new int[RODS];
			for (int i = 0; i < RODS; i++) {
				rodNotes[RODS_BY_LENGTH[i]] = chosen[i];
			}
		}
		return rodNotes;
	}
}
