package com.windchimes.block;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * How a wind chime sounds and moves. Notes are note-block semitones (0 = lowest F#, 24 = highest).
 * Every voice uses a major pentatonic scale so any random combination sounds pleasant.
 * Swing is how easily the rods move in the wind.
 *
 * Minecraft can't play a sound below half pitch (note 0), so the long chimes sit at the bottom of their
 * instrument's range and add an undertone: a quieter, lower instrument played on the same note for depth.
 */
public enum ChimeVoice {
	BAMBOO(() -> SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), 0.55F, 1.1F, 0, 2, 4, 7, 9, 12),
	IRON(() -> SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), 0.45F, 0.65F, 7, 9, 12, 14, 16, 19),
	COPPER(() -> SoundEvents.NOTE_BLOCK_BELL.value(), 0.35F, 0.7F, 4, 7, 9, 12, 14, 16),
	GOLD(() -> SoundEvents.NOTE_BLOCK_CHIME.value(), 0.5F, 0.6F, 0, 2, 4, 7, 9, 12, 14),
	AMETHYST(() -> SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 0.8F, 2, 4, 7, 9, 12, 14, 16, 19, 21),

	LONG_BAMBOO(() -> SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), 0.6F, 1.0F,
		() -> SoundEvents.NOTE_BLOCK_HARP.value(), 0.3F, 0, 2, 4, 5, 7),
	LONG_IRON(() -> SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), 0.5F, 0.6F,
		() -> SoundEvents.NOTE_BLOCK_BASS.value(), 0.2F, 0, 2, 4, 7, 9, 12),
	LONG_COPPER(() -> SoundEvents.NOTE_BLOCK_BELL.value(), 0.4F, 0.65F,
		() -> SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), 0.3F, 0, 2, 4, 7, 9),
	LONG_GOLD(() -> SoundEvents.NOTE_BLOCK_CHIME.value(), 0.55F, 0.55F,
		() -> SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), 0.25F, 0, 2, 4, 7, 9),
	LONG_AMETHYST(() -> SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.75F,
		() -> SoundEvents.AMETHYST_BLOCK_RESONATE, 0.35F, 0, 2, 4, 7, 9, 12);

	private final Supplier<SoundEvent> sound;
	private final float volume;
	private final float swing;
	private final Supplier<SoundEvent> undertone;
	private final float undertoneVolume;
	private final int[] notes;

	ChimeVoice(Supplier<SoundEvent> sound, float volume, float swing, int... notes) {
		this(sound, volume, swing, null, 0.0F, notes);
	}

	ChimeVoice(Supplier<SoundEvent> sound, float volume, float swing, Supplier<SoundEvent> undertone, float undertoneVolume, int... notes) {
		this.sound = sound;
		this.volume = volume;
		this.swing = swing;
		this.undertone = undertone;
		this.undertoneVolume = undertoneVolume;
		this.notes = notes;
	}

	public SoundEvent sound() {
		return sound.get();
	}

	public float volume() {
		return volume;
	}

	public float swing() {
		return swing;
	}

	/** A second, lower sound played quietly with every note, or null for none. */
	public SoundEvent undertone() {
		return undertone == null ? null : undertone.get();
	}

	/** Undertone loudness relative to {@link #volume()}. */
	public float undertoneVolume() {
		return undertoneVolume;
	}

	public int[] notes() {
		return notes;
	}

	public static float pitch(int note) {
		return (float) Math.pow(2.0, (note - 12) / 12.0);
	}
}
