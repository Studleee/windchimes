package com.windchimes.client;

import com.windchimes.block.WindChimeBlockEntity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class WindChimeRenderState extends BlockEntityRenderState {
	public float seconds;
	public float windDirection;
	/** Wind strength already scaled by how easily this chime swings; 0 when muffled. */
	public float sway;
	/** Per rod, then the center striker last. */
	public final float[] secondsSinceStrike = new float[WindChimeBlockEntity.RODS + 1];
	public float phase;
	/** Rod length in blocks: 1 for normal chimes, 2 for long ones. */
	public float length = 1.0F;
	public TextureAtlasSprite rod;
	public TextureAtlasSprite cap;
	public TextureAtlasSprite string;
}
