package com.windchimes.registry;

import java.util.Set;

import com.windchimes.Windchimes;
import com.windchimes.block.WindChimeBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static BlockEntityType<WindChimeBlockEntity> WIND_CHIME;

	private ModBlockEntities() {
	}

	/** Must run after ModBlocks, since the type lists every wind chime block. */
	public static void initialize() {
		WIND_CHIME = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			Windchimes.id("wind_chime"),
			new BlockEntityType<>(WindChimeBlockEntity::new, Set.copyOf(ModBlocks.windChimes()))
		);
	}
}
