package thaumcraft.common.blocks.basic;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockCandle extends Block {
    protected static final VoxelShape SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 8.0D, 10.0D);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public DyeColor dye;

    public BlockCandle(Properties pProperties, DyeColor dye) {
        super(pProperties);
        this.dye = dye;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MaterialColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MaterialColor defaultColor) {
        return dye.getMaterialColor();
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public @NotNull BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getClockWise());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public void animateTick(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull RandomSource pRandom) {
        double x = (double)pPos.getX() + 0.5D;
        double y = (double)pPos.getY() + 0.7D;
        double z = (double)pPos.getZ() + 0.5D;
        pLevel.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        pLevel.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
    }
}
