package thaumcraft.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blockentities.misc.BlockEntityBarrierStone;

public class BlockBarrier extends Block {
    public BlockBarrier() {
        super(BlockBehaviour.Properties.of(Material.BARRIER)
                .noOcclusion()
                .lightLevel(s -> 0)
                .sound(SoundType.STONE)
                .strength(-1.0f, 3600000.0f)
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (!(ctx instanceof EntityCollisionContext entityCtx)) {
            return Shapes.empty();
        }

        Entity entity = entityCtx.getEntity();
        if (!shouldBlock(entity)) {
            return Shapes.empty();
        }

        Level entityLevel = entity.level;
        if (entityLevel == null || entityLevel.isClientSide) {
            return Shapes.empty();
        }

        if (isBarrierActive(entityLevel, pos)) {
            return Shapes.block();
        }

        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;
        if (!shouldBlock(entity)) return;
        if (!isBarrierActive(level, pos)) return;

        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * -0.3, Math.max(motion.y, 0.1), motion.z * -0.3);
        entity.resetFallDistance();
    }

    private boolean shouldBlock(Entity entity) {
        if (entity == null) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (entity instanceof Player) return false;
        if (entity.getPassengers().stream().anyMatch(e -> e instanceof Player)) return false;
        return true;
    }

    private boolean isBarrierActive(Level level, BlockPos pos) {
        for (int a = 1; a <= 2; a++) {
            BlockPos below = pos.below(a);
            if (level.getBlockState(below).getBlock() == BlocksTC.pavingStoneBarrier) {
                BlockEntity te = level.getBlockEntity(below);
                if (te instanceof BlockEntityBarrierStone bs) {
                    return !bs.gettingPower();
                }

                return level.getBestNeighborSignal(below) == 0;
            }
        }
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext ctx) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        BlockState below1 = level.getBlockState(pos.below(1));
        if (below1.getBlock() != BlocksTC.pavingStoneBarrier && below1.getBlock() != this) {
            level.removeBlock(pos, false);
        }
    }

    public static boolean isPassable(Level level, BlockPos pos) {
        for (int a = 1; a < 3; a++) {
            BlockEntity te = level.getBlockEntity(pos.below(a));
            if (te instanceof BlockEntityBarrierStone bs) {
                return bs.gettingPower();
            }
        }
        return true;
    }
}