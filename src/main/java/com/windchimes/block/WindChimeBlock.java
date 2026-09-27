package com.windchimes.block;

import com.windchimes.Wind;
import com.windchimes.registry.ModBlockEntities;
import com.windchimes.registry.ModParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Hangs from the underside of a block and rings in the wind (the notes are played by WindChimeBlockEntity).
 * Stronger wind (gusts, rain, thunder, altitude) makes it ring more often and swing harder.
 * Right-click to ring it yourself; sneak + right-click to muffle it.
 * The wooden cap is a normal block model; the swaying rods are drawn by WindChimeRenderer.
 */
public class WindChimeBlock extends Block implements EntityBlock {
	public static final BooleanProperty MUFFLED = BooleanProperty.create("muffled");

	private static final VoxelShape SHAPE = Block.box(3.0, 1.0, 3.0, 13.0, 16.0, 13.0);
	private static final float WIND_PARTICLE_THRESHOLD = 0.55F;

	private final ChimeVoice voice;
	private final Identifier rodTexture;
	private final Identifier capTexture;

	public WindChimeBlock(ChimeVoice voice, Identifier rodTexture, Identifier capTexture, Properties properties) {
		super(properties);
		this.voice = voice;
		this.rodTexture = rodTexture;
		this.capTexture = capTexture;
		registerDefaultState(stateDefinition.any().setValue(MUFFLED, false));
	}

	public ChimeVoice voice() {
		return voice;
	}

	public Identifier rodTexture() {
		return rodTexture;
	}

	public Identifier capTexture() {
		return capTexture;
	}

	/** How long the rods hang, in blocks. */
	public float length() {
		return 1.0F;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WindChimeBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MUFFLED);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.UP && !canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		return blockEntity != null && blockEntity.triggerEvent(id, param);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (player.isSecondaryUseActive()) {
			boolean muffled = !state.getValue(MUFFLED);
			level.setBlock(pos, state.setValue(MUFFLED, muffled), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, muffled ? 0.8F : 1.2F);
			player.sendOverlayMessage(Component.translatable(muffled ? "block.windchimes.muffled" : "block.windchimes.unmuffled"));
		} else if (state.getValue(MUFFLED)) {
			level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.6F, 1.0F);
		} else {
			level.blockEvent(pos, this, WindChimeBlockEntity.STRIKE_EVENT, 0);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (!level.isClientSide() || type != ModBlockEntities.WIND_CHIME) {
			return null;
		}
		return (BlockEntityTicker<T>) (BlockEntityTicker<WindChimeBlockEntity>) WindChimeBlockEntity::clientTick;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		float wind = Wind.strength(level, pos, 0.0F);
		if (wind > WIND_PARTICLE_THRESHOLD && random.nextFloat() < (wind - WIND_PARTICLE_THRESHOLD) * 0.6F) {
			spawnWindWisp(level, pos, wind, random);
		}
	}

	private static void spawnWindWisp(Level level, BlockPos pos, float wind, RandomSource random) {
		float dir = Wind.direction(level, 0.0F);
		double speed = 0.06 + wind * 0.08;
		double upwind = 1.5 + random.nextDouble();
		double x = pos.getX() + 0.5 - Mth.cos(dir) * upwind + (random.nextDouble() - 0.5) * 2.0;
		double y = pos.getY() + 0.2 + random.nextDouble() * 0.8;
		double z = pos.getZ() + 0.5 - Mth.sin(dir) * upwind + (random.nextDouble() - 0.5) * 2.0;
		level.addParticle(ModParticles.WIND, x, y, z, Mth.cos(dir) * speed, 0.0, Mth.sin(dir) * speed);
	}
}
