package com.windchimes.registry;

import com.windchimes.Windchimes;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public final class ModParticles {
	public static final SimpleParticleType WIND = Registry.register(
		BuiltInRegistries.PARTICLE_TYPE,
		Windchimes.id("wind"),
		FabricParticleTypes.simple()
	);

	private ModParticles() {
	}

	public static void initialize() {
	}
}
