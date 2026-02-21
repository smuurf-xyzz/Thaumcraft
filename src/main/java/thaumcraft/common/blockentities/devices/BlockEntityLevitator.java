package thaumcraft.common.blockentities.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.blockentities.BlockEntityThaumcraft;
import thaumcraft.common.blockentities.Tickable;
import thaumcraft.common.blocks.BlockTCDevice;

import java.util.List;

public class BlockEntityLevitator extends BlockEntityThaumcraft implements Tickable {
    private int[] ranges;
    private int range;
    private int rangeActual;
    private int counter;
    private int vis;

    public BlockEntityLevitator(BlockPos pos, BlockState state) {
        super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(new ResourceLocation("thaumcraft:levitator")), pos, state);
        this.ranges = new int[]{ 4, 8, 16, 32 };
        this.range = 1;
        this.rangeActual = 0;
        this.counter = 0;
        this.vis = 0;
    }

    @Override
    public void tick() {
        final Direction facing = this.getBlockState().getValue(BlockTCDevice.FACING_ALL);

        if (this.rangeActual > this.ranges[this.range]) {
            this.rangeActual = 0;
        }

        final int p = this.counter % this.ranges[this.range];
        final BlockPos offsetPos = this.worldPosition.relative(facing, 1 + p);

        if (this.level.getBlockState(offsetPos).isRedstoneConductor(this.level, offsetPos)) {
            if (1 + p < this.rangeActual) {
                this.rangeActual = 1 + p;
            }
            this.counter = -1;
        } else if (1 + p > this.rangeActual) {
            this.rangeActual = 1 + p;
        }

        ++this.counter;

        if (!this.level.isClientSide && this.vis < 10) {
            this.vis += (int)(AuraHandler.drainVis(this.level, this.worldPosition, 1.0f, false) * 1200.0f);
            this.setChanged();
            this.syncTile(false);
        }

        final boolean enabled = this.getBlockState().getValue(BlockTCDevice.ENABLED);

        if (this.rangeActual > 0 && this.vis > 0 && enabled) {
            final int fx = facing.getStepX();
            final int fy = facing.getStepY();
            final int fz = facing.getStepZ();

            final List<Entity> targets = this.level.getEntitiesOfClass(
                    Entity.class,
                    new AABB(
                            this.worldPosition.getX() - (fx < 0 ? this.rangeActual : 0),
                            this.worldPosition.getY() - (fy < 0 ? this.rangeActual : 0),
                            this.worldPosition.getZ() - (fz < 0 ? this.rangeActual : 0),
                            this.worldPosition.getX() + 1 + (fx > 0 ? this.rangeActual : 0),
                            this.worldPosition.getY() + 1 + (fy > 0 ? this.rangeActual : 0),
                            this.worldPosition.getZ() + 1 + (fz > 0 ? this.rangeActual : 0)
                    )
            );

            boolean lifted = false;
            for (final Entity e : targets) {
                if (!(e instanceof ItemEntity) && !e.isPushable() && !(e instanceof AbstractHorse)) {
                    continue;
                }

                lifted = true;
                this.drawFXAt(e);
                this.drawFX(facing, 0.6);

                if (e.isCrouching() && facing == Direction.UP) {
                    Vec3 motion = e.getDeltaMovement();
                    if (motion.y < 0.0) {
                        e.setDeltaMovement(motion.x, motion.y * 0.9, motion.z);
                    }
                } else {
                    Vec3 motion = e.getDeltaMovement();
                    double mx = motion.x + 0.1 * fx;
                    double my = motion.y + 0.1 * fy;
                    double mz = motion.z + 0.1 * fz;

                    if (facing.getAxis() != Direction.Axis.Y && !e.isOnGround()) {
                        if (my < 0.0) my *= 0.9;
                        my += 0.08;
                    }

                    final double cap = 0.35;
                    mx = Math.max(-cap, Math.min(cap, mx));
                    my = Math.max(-cap, Math.min(cap, my));
                    mz = Math.max(-cap, Math.min(cap, mz));

                    e.setDeltaMovement(mx, my, mz);
                }

                e.resetFallDistance();
                this.vis -= this.getCost();
                if (this.vis <= 0) break;
            }

            this.drawFX(facing, 0.1);
            if (lifted && !this.level.isClientSide && this.counter % 20 == 0) {
                this.setChanged();
            }
        }
    }

    private void drawFX(final Direction facing, final double chance) {
        if (this.level.isClientSide && this.level.random.nextFloat() < chance) {
            final float x = this.worldPosition.getX() + 0.25f + this.level.random.nextFloat() * 0.5f;
            final float y = this.worldPosition.getY() + 0.25f + this.level.random.nextFloat() * 0.5f;
            final float z = this.worldPosition.getZ() + 0.25f + this.level.random.nextFloat() * 0.5f;
            FXDispatcher.INSTANCE.drawLevitatorParticles(
                    x, y, z,
                    facing.getStepX() / 50.0,
                    facing.getStepY() / 50.0,
                    facing.getStepZ() / 50.0
            );
        }
    }

    private void drawFXAt(final Entity e) {
        if (this.level.isClientSide && this.level.random.nextFloat() < 0.1f) {
            final float x = (float)(e.getX() + (this.level.random.nextFloat() - this.level.random.nextFloat()) * e.getBbWidth());
            final float y = (float)(e.getY() + this.level.random.nextFloat() * e.getBbHeight());
            final float z = (float)(e.getZ() + (this.level.random.nextFloat() - this.level.random.nextFloat()) * e.getBbWidth());
            FXDispatcher.INSTANCE.drawLevitatorParticles(
                    x, y, z,
                    (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01,
                    (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01,
                    (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01
            );
        }
    }

    @Override
    public void readSyncNBT(final CompoundTag nbt) {
        this.range = nbt.getByte("range");
        this.vis = nbt.getInt("vis");
    }

    @Override
    public CompoundTag writeSyncNBT(final CompoundTag nbt) {
        nbt.putByte("range", (byte) this.range);
        nbt.putInt("vis", this.vis);
        return nbt;
    }

    public int getCost() {
        return this.ranges[this.range] * 2;
    }

    public void increaseRange(final Player player) {
        this.rangeActual = 0;
        if (!this.level.isClientSide) {
            ++this.range;
            if (this.range >= this.ranges.length) {
                this.range = 0;
            }
            this.setChanged();
            this.syncTile(false);
            player.sendSystemMessage(
                    Component.literal(
                            String.format(
                                    Component.translatable("tc.levitator").getString(),
                                    this.ranges[this.range],
                                    this.getCost()
                            )
                    )
            );
        }
    }
}