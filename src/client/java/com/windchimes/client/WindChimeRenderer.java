package com.windchimes.client;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.windchimes.Wind;
import com.windchimes.block.WindChimeBlock;
import com.windchimes.block.WindChimeBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;

/**
 * Draws the moving parts of a wind chime: four rods and the center striker, each hanging from its string
 * and swinging like a pendulum. Coordinates are in pixels (0-16 across the block).
 */
public class WindChimeRenderer implements BlockEntityRenderer<WindChimeBlockEntity, WindChimeRenderState> {
	private static final Identifier STRING_TEXTURE = Identifier.withDefaultNamespace("block/white_wool");
	private static final RenderType RENDER_TYPE = RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS);

	private static final float PIVOT_Y = 14.5F;
	private static final float ROD_TOP = 13.0F;
	/** Each rod: x, z, bottom y. Different lengths swing at different speeds. */
	private static final float[][] RODS = {
		{4.5F, 8.0F, 5.0F},
		{11.5F, 8.0F, 7.0F},
		{8.0F, 4.5F, 4.0F},
		{8.0F, 11.5F, 6.0F}
	};

	private final SpriteGetter sprites;
	private final Map<Identifier, TextureAtlasSprite> spriteCache = new HashMap<>();

	public WindChimeRenderer(BlockEntityRendererProvider.Context context) {
		this.sprites = context.sprites();
	}

	@Override
	public WindChimeRenderState createRenderState() {
		return new WindChimeRenderState();
	}

	@Override
	public void extractRenderState(WindChimeBlockEntity chime, WindChimeRenderState state, float partialTick, Vec3 cameraPos,
			ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderState.extractBase(chime, state, breakProgress);

		Level level = chime.getLevel();
		BlockState blockState = chime.getBlockState();
		if (level == null || !(blockState.getBlock() instanceof WindChimeBlock block)) {
			return;
		}

		BlockPos pos = chime.getBlockPos();
		boolean muffled = blockState.getValue(WindChimeBlock.MUFFLED);

		state.seconds = Wind.seconds(level, partialTick);
		state.windDirection = Wind.direction(level, partialTick);
		state.sway = muffled ? 0.0F : Wind.strength(level, pos, partialTick) * block.voice().swing();
		for (int i = 0; i < state.secondsSinceStrike.length; i++) {
			state.secondsSinceStrike[i] = muffled ? Float.MAX_VALUE : (level.getGameTime() - chime.lastStrikeTick(i) + partialTick) / 20.0F;
		}
		state.phase = (Mth.getSeed(pos) & 1023) / 1023.0F * Mth.TWO_PI;
		state.length = block.length();
		state.rod = sprite(block.rodTexture());
		state.cap = sprite(block.capTexture());
		state.string = sprite(STRING_TEXTURE);
	}

	@Override
	public void submit(WindChimeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.rod == null) {
			return;
		}
		int light = state.lightCoords;
		float length = state.length;

		for (int i = 0; i < RODS.length; i++) {
			float x = RODS[i][0];
			float z = RODS[i][1];
			float bottom = ROD_TOP - (ROD_TOP - RODS[i][2]) * length;

			poseStack.pushPose();
			swing(poseStack, state, x, z, PIVOT_Y - bottom, i * 1.7F, state.secondsSinceStrike[i]);
			collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
				column(pose, buffer, state.string, x - 0.2F, ROD_TOP, z - 0.2F, x + 0.2F, PIVOT_Y, z + 0.2F, light);
				column(pose, buffer, state.rod, x - 0.5F, bottom, z - 0.5F, x + 0.5F, ROD_TOP, z + 0.5F, light);
			});
			poseStack.popPose();
		}

		float stringBottom = PIVOT_Y - (PIVOT_Y - 6.5F) * length;
		float clapper = PIVOT_Y - (PIVOT_Y - 8.0F) * length;
		float sailBottom = stringBottom - 4.5F;

		poseStack.pushPose();
		swing(poseStack, state, 8.0F, 8.0F, PIVOT_Y - sailBottom, 5.3F, state.secondsSinceStrike[WindChimeBlockEntity.STRIKER]);
		collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
			column(pose, buffer, state.string, 7.8F, stringBottom, 7.8F, 8.2F, PIVOT_Y, 8.2F, light);
			column(pose, buffer, state.cap, 6.5F, clapper, 6.5F, 9.5F, clapper + 0.75F, 9.5F, light);
			column(pose, buffer, state.cap, 7.25F, sailBottom, 7.9F, 8.75F, stringBottom, 8.1F, light);
		});
		poseStack.popPose();
	}

	/** Long chimes hang into the block below, which may be on screen when this one isn't. */
	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	/**
	 * Tilts everything drawn after this around the top of a string. Rods lean downwind, rock back and forth
	 * with the gusts (longer rods rock slower), wobble a little sideways, and jump when struck.
	 */
	private static void swing(PoseStack poseStack, WindChimeRenderState state, float pivotX, float pivotZ, float length, float seed,
			float secondsSinceStrike) {
		float sway = state.sway;
		float speed = 2.4F * Mth.sqrt(8.0F / length);
		float phase = state.phase + seed;

		float lean = sway * 0.3F;
		float rock = sway > 0.0F ? 0.04F + sway * 0.2F : 0.0F;
		float strike = 0.3F * (float) Math.exp(-secondsSinceStrike * 1.6F);
		float t = state.seconds;
		float ts = secondsSinceStrike;

		float along = lean + rock * Mth.sin(t * speed + phase) + strike * Mth.sin(ts * speed * 2.2F + seed);
		float across = rock * 0.4F * Mth.sin(t * speed * 1.37F + phase * 2.0F) + strike * 0.6F * Mth.cos(ts * speed * 1.9F + seed * 1.3F);

		float cos = Mth.cos(state.windDirection);
		float sin = Mth.sin(state.windDirection);
		float aroundZ = along * cos - across * sin;
		float aroundX = -(along * sin + across * cos);

		poseStack.rotateAround(new Quaternionf().rotateZ(aroundZ).rotateX(aroundX), pivotX / 16.0F, PIVOT_Y / 16.0F, pivotZ / 16.0F);
	}

	private TextureAtlasSprite sprite(Identifier texture) {
		return spriteCache.computeIfAbsent(texture, id -> sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, id)));
	}

	/** A box that may reach below y 0 into the block underneath; split there so the texture repeats instead of overrunning. */
	private static void column(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
			float x0, float y0, float z0, float x1, float y1, float z1, int light) {
		if (y0 < 0.0F && y1 > 0.0F) {
			box(pose, buffer, sprite, x0, y0, z0, x1, 0.0F, z1, light, 16.0F);
			box(pose, buffer, sprite, x0, 0.0F, z0, x1, y1, z1, light, 0.0F);
		} else {
			box(pose, buffer, sprite, x0, y0, z0, x1, y1, z1, light, y1 <= 0.0F ? 16.0F : 0.0F);
		}
	}

	/** vShift moves side-face texture coordinates up by that many pixels, for parts drawn in the block below. */
	private static void box(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
			float x0, float y0, float z0, float x1, float y1, float z1, int light, float vShift) {
		float vTop = 16 - (y1 + vShift);
		float vBottom = 16 - (y0 + vShift);
		// up
		vertex(pose, buffer, sprite, x0, y1, z0, x0, z0, 0, 1, 0, light);
		vertex(pose, buffer, sprite, x0, y1, z1, x0, z1, 0, 1, 0, light);
		vertex(pose, buffer, sprite, x1, y1, z1, x1, z1, 0, 1, 0, light);
		vertex(pose, buffer, sprite, x1, y1, z0, x1, z0, 0, 1, 0, light);
		// down
		vertex(pose, buffer, sprite, x0, y0, z0, x0, z0, 0, -1, 0, light);
		vertex(pose, buffer, sprite, x1, y0, z0, x1, z0, 0, -1, 0, light);
		vertex(pose, buffer, sprite, x1, y0, z1, x1, z1, 0, -1, 0, light);
		vertex(pose, buffer, sprite, x0, y0, z1, x0, z1, 0, -1, 0, light);
		// north
		vertex(pose, buffer, sprite, x0, y0, z0, x0, vBottom, 0, 0, -1, light);
		vertex(pose, buffer, sprite, x0, y1, z0, x0, vTop, 0, 0, -1, light);
		vertex(pose, buffer, sprite, x1, y1, z0, x1, vTop, 0, 0, -1, light);
		vertex(pose, buffer, sprite, x1, y0, z0, x1, vBottom, 0, 0, -1, light);
		// south
		vertex(pose, buffer, sprite, x0, y0, z1, x0, vBottom, 0, 0, 1, light);
		vertex(pose, buffer, sprite, x1, y0, z1, x1, vBottom, 0, 0, 1, light);
		vertex(pose, buffer, sprite, x1, y1, z1, x1, vTop, 0, 0, 1, light);
		vertex(pose, buffer, sprite, x0, y1, z1, x0, vTop, 0, 0, 1, light);
		// west
		vertex(pose, buffer, sprite, x0, y0, z0, z0, vBottom, -1, 0, 0, light);
		vertex(pose, buffer, sprite, x0, y0, z1, z1, vBottom, -1, 0, 0, light);
		vertex(pose, buffer, sprite, x0, y1, z1, z1, vTop, -1, 0, 0, light);
		vertex(pose, buffer, sprite, x0, y1, z0, z0, vTop, -1, 0, 0, light);
		// east
		vertex(pose, buffer, sprite, x1, y0, z0, z0, vBottom, 1, 0, 0, light);
		vertex(pose, buffer, sprite, x1, y1, z0, z0, vTop, 1, 0, 0, light);
		vertex(pose, buffer, sprite, x1, y1, z1, z1, vTop, 1, 0, 0, light);
		vertex(pose, buffer, sprite, x1, y0, z1, z1, vBottom, 1, 0, 0, light);
	}

	private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
			float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
		buffer.addVertex(pose, x / 16.0F, y / 16.0F, z / 16.0F)
			.setColor(-1)
			.setUv(sprite.getU(u / 16.0F), sprite.getV(v / 16.0F))
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(light)
			.setNormal(pose, nx, ny, nz);
	}
}
