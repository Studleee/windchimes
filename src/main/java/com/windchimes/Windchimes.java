package com.windchimes;

import com.windchimes.registry.ModBlockEntities;
import com.windchimes.registry.ModBlocks;
import com.windchimes.registry.ModCreativeTab;
import com.windchimes.registry.ModItems;
import com.windchimes.registry.ModParticles;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Windchimes implements ModInitializer {
	public static final String MOD_ID = "windchimes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModParticles.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();
		ModCreativeTab.initialize();
		LOGGER.info("{} loaded", MOD_ID);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
