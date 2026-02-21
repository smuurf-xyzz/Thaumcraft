package thaumcraft.common.blocks.devices;

import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import thaumcraft.common.blockentities.Tickable;
import thaumcraft.common.blockentities.devices.BlockEntityLevitator;
import thaumcraft.common.blocks.BlockTCDevice;
import thaumcraft.common.lib.SoundsTC;

import javax.annotation.Nullable;

public class BlockLevitator extends BlockTCDevice {
    public BlockLevitator() {
        super(BlockEntityLevitator.class, Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(1.5f).noOcclusion());
    }

    @Override
    protected boolean useFullFacing() {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext ctx) {
        return getBaseShape(state);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getIndexedShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getBaseShape(state);
    }

    private VoxelShape getBaseShape(BlockState state) {
        Direction facing = state.getValue(FACING_ALL);
        float f = 0.125f;
        float minx = (facing.getStepX() > 0) ? f : 0.0f;
        float maxx = 1.0f - ((facing.getStepX() < 0) ? f : 0.0f);
        float miny = (facing.getStepY() > 0) ? f : 0.0f;
        float maxy = 1.0f - ((facing.getStepY() < 0) ? f : 0.0f);
        float minz = (facing.getStepZ() > 0) ? f : 0.0f;
        float maxz = 1.0f - ((facing.getStepZ() < 0) ? f : 0.0f);
        return Shapes.box(minx, miny, minz, maxx, maxy, maxz);
    }

    private IndexedVoxelShape getIndexedShape(BlockState state) {
        return new IndexedVoxelShape(
                VoxelShapeCache.getShape(getCuboidByFacing(state.getValue(FACING_ALL))), 0);
    }

    static Cuboid6 getCuboidByFacing(Direction facing) {
        return switch (facing) {
            case DOWN  -> new Cuboid6(0.375, 0.875,  0.375, 0.625, 0.9375, 0.625);
            case EAST  -> new Cuboid6(0.0625, 0.375, 0.375, 0.125, 0.625,  0.625);
            case WEST  -> new Cuboid6(0.875, 0.375,  0.375, 0.9375, 0.625, 0.625);
            case SOUTH -> new Cuboid6(0.375, 0.375,  0.0625, 0.625, 0.625, 0.125);
            case NORTH -> new Cuboid6(0.375, 0.375,  0.875, 0.625, 0.625,  0.9375);
            default    -> new Cuboid6(0.375, 0.0625, 0.375, 0.625, 0.125,  0.625); // UP
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        BlockHitResult traced = RayTracer.retraceBlock((BlockGetter) world, player, pos);
        if (traced instanceof SubHitBlockHitResult subHit && subHit.subHit == 0) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof BlockEntityLevitator lev) {
                lev.increaseRange(player);
                world.playSound(null,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundsTC.key, SoundSource.BLOCKS, 0.5f, 1.0f
                );
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof Tickable tickable) {
                tickable.tick();
            }
        };
    }
}