package thaumcraft.common.blocks.misc;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import thaumcraft.common.lib.SoundsTC;

public class BlockFlesh extends Block {
    public BlockFlesh() {
        super(Properties.of(Material.SPONGE)
                .strength(0.25f, 2.0f)
                .sound(SoundsTC.GORE));
    }
}
