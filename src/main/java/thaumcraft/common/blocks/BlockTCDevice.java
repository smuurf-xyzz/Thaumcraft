package thaumcraft.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BlockTCDevice extends BlockTCBlockEntity {
    public static final DirectionProperty FACING_HORIZONTAL = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty FACING_ALL = BlockStateProperties.FACING;

    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

    protected BlockTCDevice(Class<? extends BlockEntity> beClass, Properties properties) {
        super(beClass, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(getFacingProperty(), getDefaultFacing()).setValue(ENABLED, true));
    }

    protected boolean useFullFacing() {
        return false;
    }

    public DirectionProperty getFacingProperty() {
        return useFullFacing() ? FACING_ALL : FACING_HORIZONTAL;
    }

    protected Direction getDefaultFacing() {
        return Direction.NORTH;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(getFacingProperty(), ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing;
        if (useFullFacing()) {
            Direction clicked = ctx.getClickedFace();
            facing = clicked.getAxis() == Direction.Axis.Y ? clicked : ctx.getHorizontalDirection().getOpposite();
        } else {
            facing = ctx.getHorizontalDirection().getOpposite();
        }
        return this.defaultBlockState().setValue(getFacingProperty(), facing).setValue(ENABLED, true);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            boolean shouldBeEnabled = !powered;
            if (state.getValue(ENABLED) != shouldBeEnabled) {
                level.setBlock(pos, state.setValue(ENABLED, shouldBeEnabled), 3);
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, moving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            DirectionProperty prop = getFacingProperty();
            Direction current = state.getValue(prop);
            Direction next;
            if (useFullFacing()) {
                Direction[] dirs = Direction.values();
                next = dirs[(current.ordinal() + 1) % dirs.length];
            } else {
                next = current.getClockWise();
            }
            level.setBlock(pos, state.setValue(prop, next), 3);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}