package thaumcraft.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.PlantType;
import thaumcraft.client.fx.FXDispatcher;

public class BlockPlantShimmerleaf extends BushBlock {
    public BlockPlantShimmerleaf() {
        super(Properties.of(Material.PLANT)
                .sound(SoundType.GRASS)
                .lightLevel(state -> 6)
                .noCollission()
                .instabreak()
                .offsetType(OffsetType.XZ));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT);
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return PlantType.PLAINS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(3) == 0) {
            float xr = (float) (pos.getX() + 0.5f + rand.nextGaussian() * 0.1);
            float yr = (float) (pos.getY() + 0.4f + rand.nextGaussian() * 0.1);
            float zr = (float) (pos.getZ() + 0.5f + rand.nextGaussian() * 0.1);
            FXDispatcher.INSTANCE.drawWispyMotes(xr, yr, zr,
                    rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01,
                    10,
                    0.3f + level.random.nextFloat() * 0.3f,
                    0.7f + level.random.nextFloat() * 0.3f,
                    0.7f + level.random.nextFloat() * 0.3f,
                    0.0f);
        }
    }
}