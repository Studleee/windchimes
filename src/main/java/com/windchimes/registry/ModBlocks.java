package com.windchimes.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.windchimes.block.ChimeVoice;
import com.windchimes.block.LongWindChimeBlock;
import com.windchimes.block.WindChimeBlock;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

/**
 * All of your blocks. Strength is how long it takes to mine (dirt is 0.5, stone is 1.5, obsidian is 50).
 * The new-block script adds a line here and creates the texture, model, drop, and name for you.
 *
 * Wind chimes take a rod texture and a cap texture, both vanilla block textures (e.g. "iron_block", "spruce_planks").
 */
public final class ModBlocks {
	private static final List<Block> WIND_CHIMES = new ArrayList<>();

	public static final Block BAMBOO_WIND_CHIME = windChime("bamboo", ChimeVoice.BAMBOO, SoundType.BAMBOO, "bamboo_block", "bamboo_planks");
	public static final Block IRON_WIND_CHIME = windChime("iron", ChimeVoice.IRON, SoundType.CHAIN, "iron_block", "spruce_planks");
	public static final Block COPPER_WIND_CHIME = windChime("copper", ChimeVoice.COPPER, SoundType.COPPER, "copper_block", "dark_oak_planks");
	public static final Block GOLD_WIND_CHIME = windChime("gold", ChimeVoice.GOLD, SoundType.CHAIN, "gold_block", "dark_oak_planks");
	public static final Block AMETHYST_WIND_CHIME = windChime("amethyst", ChimeVoice.AMETHYST, SoundType.AMETHYST, "amethyst_block", "cherry_planks");

	// Two blocks tall, with longer rods and deeper notes.
	public static final Block LONG_BAMBOO_WIND_CHIME = longWindChime("bamboo", ChimeVoice.LONG_BAMBOO, SoundType.BAMBOO, "bamboo_block", "bamboo_planks");
	public static final Block LONG_IRON_WIND_CHIME = longWindChime("iron", ChimeVoice.LONG_IRON, SoundType.CHAIN, "iron_block", "spruce_planks");
	public static final Block LONG_COPPER_WIND_CHIME = longWindChime("copper", ChimeVoice.LONG_COPPER, SoundType.COPPER, "copper_block", "dark_oak_planks");
	public static final Block LONG_GOLD_WIND_CHIME = longWindChime("gold", ChimeVoice.LONG_GOLD, SoundType.CHAIN, "gold_block", "dark_oak_planks");
	public static final Block LONG_AMETHYST_WIND_CHIME = longWindChime("amethyst", ChimeVoice.LONG_AMETHYST, SoundType.AMETHYST, "amethyst_block", "cherry_planks");

	// new-block-marker (the new-block script adds blocks above this line)

	private ModBlocks() {
	}

	public static void initialize() {
	}

	public static List<Block> windChimes() {
		return Collections.unmodifiableList(WIND_CHIMES);
	}

	private interface ChimeFactory {
		WindChimeBlock create(ChimeVoice voice, Identifier rodTexture, Identifier capTexture, BlockBehaviour.Properties properties);
	}

	private static Block windChime(String material, ChimeVoice voice, SoundType sound, String rodTexture, String capTexture) {
		return register(material + "_wind_chime", WindChimeBlock::new, voice, sound, rodTexture, capTexture);
	}

	private static Block longWindChime(String material, ChimeVoice voice, SoundType sound, String rodTexture, String capTexture) {
		return register("long_" + material + "_wind_chime", LongWindChimeBlock::new, voice, sound, rodTexture, capTexture);
	}

	private static Block register(String name, ChimeFactory factory, ChimeVoice voice, SoundType sound, String rodTexture, String capTexture) {
		Block block = Register.block(
			name,
			properties -> factory.create(
				voice,
				Identifier.withDefaultNamespace("block/" + rodTexture),
				Identifier.withDefaultNamespace("block/" + capTexture),
				properties
			),
			BlockBehaviour.Properties.of()
				.strength(0.5F)
				.sound(sound)
				.noCollision()
				.noOcclusion()
				.pushReaction(PushReaction.POPPED)
		);
		WIND_CHIMES.add(block);
		return block;
	}
}
