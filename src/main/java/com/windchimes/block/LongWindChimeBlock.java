package com.windchimes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A wind chime two blocks tall. The upper half hangs from the ceiling and does everything (sound, animation);
 * the lower half is an invisible spacer that reserves room for the long rods. Breaking either half breaks both.
 */
public class LongWindChimeBlock extends WindChimeBlock {
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	private static final VoxelShape UPPER_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
	private static final VoxelShape LOWER_SHAPE = Block.box(4.0, 2.0, 4.0, 12.0, 16.0, 12.0);

	public LongWindChimeBlock(ChimeVoice voice, Identifier rodTexture, Identifier capTexture, Properties properties) {
		super(voice, rodTexture, capTexture, properties);
		registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER));
	}

	@Override
	public float length() {
		return 2.0F;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HALF);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos below = context.getClickedPos().below();
		Level level = context.getLevel();
		if (level.isOutsideBuildHeight(below) || !level.getBlockState(below).canBeReplaced(context)) {
			return null;
		}
		return defaultBlockState();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.below(), state.setValue(HALF, DoubleBlockHalf.LOWER), Block.UPDATE_ALL);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			return super.canSurvive(state, level, pos);
		}
		BlockState above = level.getBlockState(pos.above());
		return above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		DoubleBlockHalf half = state.getValue(HALF);
		Direction toOtherHalf = half == DoubleBlockHalf.UPPER ? Direction.DOWN : Direction.UP;
		if (direction == toOtherHalf && !(neighborState.is(this) && neighborState.getValue(HALF) != half)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
	}

	/** Breaking the lower half breaks the upper one properly, so it drops the item (the loot table only drops from the upper half). */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos above = pos.above();
			if (level.getBlockState(above).is(this)) {
				level.destroyBlock(above, !player.isCreative(), player);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos above = pos.above();
			BlockState upper = level.getBlockState(above);
			return upper.is(this) ? super.useWithoutItem(upper, level, above, player, hit) : InteractionResult.PASS;
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? super.newBlockEntity(pos, state) : null;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			super.animateTick(state, level, pos, random);
		}
	}
}
