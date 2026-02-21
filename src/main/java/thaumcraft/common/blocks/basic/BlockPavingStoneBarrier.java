package thaumcraft.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.blockentities.Tickable;
import thaumcraft.common.blockentities.misc.BlockEntityBarrierStone;
import thaumcraft.common.blocks.misc.BlockBarrier;

import javax.annotation.Nullable;
import java.util.List;

public class BlockPavingStoneBarrier extends BaseEntityBlock {
    public BlockPavingStoneBarrier() {
        super(BlockBehaviour.Properties.of(Material.STONE)
                .strength(2.5f)
                .sound(SoundType.STONE)
                .noOcclusion()
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBarrierStone(pos, state);
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

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (world.getBestNeighborSignal(pos) > 0) {
            for (int a = 0; a < 4; a++) {
                FXDispatcher.INSTANCE.blockRunes(
                        pos.getX(), pos.getY() + 0.7f, pos.getZ(),
                        0.2f + random.nextFloat() * 0.4f,
                        random.nextFloat() * 0.3f,
                        0.8f + random.nextFloat() * 0.2f,
                        20, -0.02f
                );
            }
        } else if (isBarrierPassable(world, pos)) {
            for (int a = 0; a < 6; a++) {
                FXDispatcher.INSTANCE.blockRunes(
                        pos.getX(), pos.getY() + 0.7f, pos.getZ(),
                        0.9f + random.nextFloat() * 0.1f,
                        random.nextFloat() * 0.3f,
                        random.nextFloat() * 0.3f,
                        24, -0.02f
                );
            }
        } else {
            List<Entity> nearby = world.getEntities(
                    null,
                    new AABB(pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
                            .inflate(1.0, 1.0, 1.0)
            );
            for (Entity entity : nearby) {
                if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                    FXDispatcher.INSTANCE.blockRunes(
                            pos.getX(),
                            pos.getY() + 0.6f + random.nextFloat() * Math.max(0.8f, entity.getEyeHeight()),
                            pos.getZ(),
                            0.6f + random.nextFloat() * 0.4f,
                            0.0f,
                            0.3f + random.nextFloat() * 0.7f,
                            20, 0.0f
                    );
                    break;
                }
            }
        }
    }

    private boolean isBarrierPassable(Level world, BlockPos pos) {
        BlockPos up1 = pos.above(1);
        BlockPos up2 = pos.above(2);
        return (world.getBlockState(up1).getBlock() == BlocksTC.barrier
                && BlockBarrier.isPassable(world, up1))
                || (world.getBlockState(up2).getBlock() == BlocksTC.barrier
                && BlockBarrier.isPassable(world, up2));
    }
}