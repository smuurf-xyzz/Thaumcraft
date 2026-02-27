package thaumcraft.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.PlantType;

public class BlockPlantCinderpearl extends BushBlock {
    public BlockPlantCinderpearl() {
        super(Properties.of(Material.PLANT)
                .sound(SoundType.GRASS)
                .lightLevel(state -> 8)
                .noCollission()
                .instabreak()
                .offsetType(OffsetType.XZ));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SAND) || state.is(Blocks.DIRT) || state.is(BlockTags.TERRACOTTA);
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return PlantType.DESERT;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (rand.nextBoolean()) {
            double xr = pos.getX() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 0.1;
            double yr = pos.getY() + 0.6 + (rand.nextFloat() - rand.nextFloat()) * 0.1;
            double zr = pos.getZ() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 0.1;
            level.addParticle(ParticleTypes.SMOKE,  xr, yr, zr, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, xr, yr, zr, 0.0, 0.0, 0.0);
        }
    }
}