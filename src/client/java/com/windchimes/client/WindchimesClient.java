package com.windchimes.client;

import com.windchimes.registry.ModBlockEntities;
import com.windchimes.registry.ModParticles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class WindchimesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(ModBlockEntities.WIND_CHIME, WindChimeRenderer::new);
		ParticleProviderRegistry.getInstance().register(ModParticles.WIND, WindParticle.Provider::new);
	}
}
