package thaumcraft.common.blockentities.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blockentities.Tickable;

import java.util.List;

public class BlockEntityBarrierStone extends BlockEntity implements Tickable {
    int count;

    public BlockEntityBarrierStone(BlockPos pos, BlockState state) {
        super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(new ResourceLocation("thaumcraft:barrier_stone")), pos, state);
        this.count = 0;
    }

    public boolean gettingPower() {
        return this.level.getBestNeighborSignal(this.worldPosition) > 0;
    }

    @Override
    public void tick() {
        if (this.level.isClientSide) return;

        if (this.count == 0) {
            this.count = this.level.random.nextInt(100);
        }

        if (this.count % 5 == 0 && !this.gettingPower()) {
            final List<LivingEntity> targets = this.level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(
                            this.worldPosition.getX(),
                            this.worldPosition.getY(),
                            this.worldPosition.getZ(),
                            this.worldPosition.getX() + 1,
                            this.worldPosition.getY() + 3,
                            this.worldPosition.getZ() + 1
                    ).inflate(0.1, 0.1, 0.1)
            );

            for (final LivingEntity e : targets) {
                if (!e.isOnGround() && !(e instanceof Player)) {
                    float yaw = (e.getYRot() + 180.0f) * Mth.PI / 180.0f;
                    e.push(-Mth.sin(yaw) * 0.2f, -0.1, Mth.cos(yaw) * 0.2f);
                }
            }
        }

        if (++this.count % 100 == 0) {
            BlockPos up1 = this.worldPosition.above(1);
            BlockPos up2 = this.worldPosition.above(2);

            if (this.level.getBlockState(up1) != BlocksTC.barrier.defaultBlockState()
                    && this.level.isEmptyBlock(up1)) {
                this.level.setBlock(up1, BlocksTC.barrier.defaultBlockState(), 3);
            }
            if (this.level.getBlockState(up2) != BlocksTC.barrier.defaultBlockState()
                    && this.level.isEmptyBlock(up2)) {
                this.level.setBlock(up2, BlocksTC.barrier.defaultBlockState(), 3);
            }
        }
    }
}