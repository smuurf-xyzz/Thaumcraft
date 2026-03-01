package thaumcraft.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class BlockSlabTC extends SlabBlock {
    private final boolean wood;

    public BlockSlabTC(boolean wood) {
        super(BlockBehaviour.Properties
                .of(wood ? Material.WOOD : Material.STONE)
                .sound(wood ? SoundType.WOOD : SoundType.STONE)
                .strength(wood ? 2.0f : 1.5f, wood ? 3.0f : 6.0f)
                .requiresCorrectToolForDrops());
        this.wood = wood;
    }

    /*
    @Override
    public int getFlammability(BlockGetter world, BlockPos pos, Direction face) {
        return wood ? 20 : super.getFlammability(world, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(BlockGetter world, BlockPos pos, Direction face) {
        return wood ? 5 : super.getFireSpreadSpeed(world, pos, face);
    }*/
}