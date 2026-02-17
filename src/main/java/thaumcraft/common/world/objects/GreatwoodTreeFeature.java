package thaumcraft.common.world.objects;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blocks.world.plants.BlockLogsTC;

public class GreatwoodTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final byte[] OTHER_COORD_PAIRS = new byte[]{2, 0, 0, 1, 2, 1};

    private RandomSource rand;
    private LevelAccessor level;
    private int[] basePos = new int[3];
    private int heightLimit = 0;
    private int height;
    private final double heightAttenuation = 0.618;
    private final double branchSlope = 0.38;
    private double scaleWidth = 1.2;
    private final double leafDensity = 0.9;
    private final int trunkSize = 2;
    private final int heightLimitLimit = 11;
    private final int leafDistanceLimit = 4;
    private int[][] leafNodes;
    private final boolean spiders;

    public GreatwoodTreeFeature(Codec<NoneFeatureConfiguration> codec, boolean spiders) {
        super(codec);
        this.spiders = spiders;
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        this.level = context.level();
        this.rand = context.random();
        BlockPos pos = context.origin();

        this.basePos[0] = pos.getX();
        this.basePos[1] = pos.getY();
        this.basePos[2] = pos.getZ();

        if (this.heightLimit == 0) {
            this.heightLimit = this.heightLimitLimit + this.rand.nextInt(this.heightLimitLimit);
        }

        boolean valid = false;
        int offsetX = 0;
        int offsetZ = 0;

        outerLoop:
        for (int a = -1; a < 2; a++) {
            for (int b = -1; b < 2; b++) {
                boolean allValid = true;
                for (int x = 0; x < this.trunkSize; x++) {
                    for (int z = 0; z < this.trunkSize; z++) {
                        if (!this.validTreeLocation(x + a, z + b)) {
                            allValid = false;
                            break;
                        }
                    }
                    if (!allValid) break;
                }
                if (allValid) {
                    valid = true;
                    offsetX = a;
                    offsetZ = b;
                    this.basePos[0] += a;
                    this.basePos[2] += b;
                    break outerLoop;
                }
            }
        }

        if (!valid) {
            return false;
        }

        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.generateTrunk();

        this.scaleWidth = 1.66;
        this.basePos[0] = pos.getX() + offsetX;
        this.basePos[1] = pos.getY() + this.height;
        this.basePos[2] = pos.getZ() + offsetZ;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.generateTrunk();

        if (this.spiders) {
            generateSpiderNest(pos);
        }

        return true;
    }

    private void generateLeafNodeList() {
        this.height = (int)((double)this.heightLimit * this.heightAttenuation);
        if (this.height >= this.heightLimit) {
            this.height = this.heightLimit - 1;
        }

        int nodeCount = (int)(1.382 + Math.pow(this.leafDensity * (double)this.heightLimit / 13.0, 2.0));
        if (nodeCount < 1) {
            nodeCount = 1;
        }

        int[][] tempNodes = new int[nodeCount * this.heightLimit][4];
        int currentY = this.basePos[1] + this.heightLimit - this.leafDistanceLimit;
        int nodeIndex = 1;
        int topY = this.basePos[1] + this.height;
        int layerHeight = currentY - this.basePos[1];

        tempNodes[0][0] = this.basePos[0];
        tempNodes[0][1] = currentY--;
        tempNodes[0][2] = this.basePos[2];
        tempNodes[0][3] = topY;

        while (layerHeight >= 0) {
            float layerRadius = this.layerSize(layerHeight);
            if (layerRadius < 0.0f) {
                currentY--;
                layerHeight--;
                continue;
            }

            for (int i = 0; i < nodeCount; i++) {
                double angle = this.rand.nextDouble() * 2.0 * Math.PI;
                double radius = this.scaleWidth * layerRadius * (this.rand.nextDouble() + 0.328);

                int nodeX = Mth.floor(radius * Math.sin(angle) + this.basePos[0] + 0.5);
                int nodeZ = Mth.floor(radius * Math.cos(angle) + this.basePos[2] + 0.5);
                int[] nodePos = new int[]{nodeX, currentY, nodeZ};
                int[] nodeTop = new int[]{nodeX, currentY + this.leafDistanceLimit, nodeZ};

                if (this.checkBlockLine(nodePos, nodeTop) != -1) {
                    continue;
                }

                int[] center = new int[]{this.basePos[0], this.basePos[1], this.basePos[2]};
                double distance = Math.sqrt(
                        Math.pow(Math.abs(this.basePos[0] - nodePos[0]), 2.0) +
                                Math.pow(Math.abs(this.basePos[2] - nodePos[2]), 2.0)
                );
                double branchHeight = distance * this.branchSlope;
                center[1] = (double)nodePos[1] - branchHeight > topY
                        ? topY
                        : (int)((double)nodePos[1] - branchHeight);

                if (this.checkBlockLine(center, nodePos) == -1) {
                    tempNodes[nodeIndex][0] = nodeX;
                    tempNodes[nodeIndex][1] = currentY;
                    tempNodes[nodeIndex][2] = nodeZ;
                    tempNodes[nodeIndex][3] = center[1];
                    nodeIndex++;
                }
            }

            currentY--;
            layerHeight--;
        }

        this.leafNodes = new int[nodeIndex][4];
        System.arraycopy(tempNodes, 0, this.leafNodes, 0, nodeIndex);
    }

    private void generateLeaves() {
        for (int[] node : this.leafNodes) {
            this.generateLeafNode(node[0], node[1], node[2]);
        }
    }

    private void generateLeafNode(int x, int y, int z) {
        for (int dy = 0; dy < this.leafDistanceLimit; dy++) {
            float radius = this.leafSize(dy);
            this.genTreeLayer(x, y + dy, z, radius, (byte)1, BlocksTC.leafGreatwood.defaultBlockState());
        }
    }

    private void genTreeLayer(int centerX, int centerY, int centerZ, float radius, byte axis, BlockState state) {
        int intRadius = (int)(radius + 0.618);
        byte axis1 = OTHER_COORD_PAIRS[axis];
        byte axis2 = OTHER_COORD_PAIRS[axis + 3];
        int[] center = new int[]{centerX, centerY, centerZ};
        int[] current = new int[3];

        current[axis] = center[axis];

        for (int offset1 = -intRadius; offset1 <= intRadius; offset1++) {
            current[axis1] = center[axis1] + offset1;

            for (int offset2 = -intRadius; offset2 <= intRadius; offset2++) {
                current[axis2] = center[axis2] + offset2;

                double distSq = Math.pow(Math.abs(offset1) + 0.5, 2.0) +
                        Math.pow(Math.abs(offset2) + 0.5, 2.0);

                if (distSq > radius * radius) {
                    continue;
                }

                BlockPos pos = new BlockPos(current[0], current[1], current[2]);
                BlockState existingState = this.level.getBlockState(pos);

                if (existingState.isAir() || existingState.is(BlockTags.LEAVES)) {
                    setBlock(this.level, pos, state);
                }
            }
        }
    }

    private void generateTrunk() {
        int x = this.basePos[0];
        int y = this.basePos[1];
        int topY = this.basePos[1] + this.height;
        int z = this.basePos[2];

        int[] start = new int[]{x, y, z};
        int[] end = new int[]{x, topY, z};

        this.placeBlockLine(start, end, BlocksTC.logGreatwood.defaultBlockState());

        if (this.trunkSize == 2) {
            start[0]++;
            end[0]++;
            this.placeBlockLine(start, end, BlocksTC.logGreatwood.defaultBlockState());

            start[2]++;
            end[2]++;
            this.placeBlockLine(start, end, BlocksTC.logGreatwood.defaultBlockState());

            start[0]--;
            end[0]--;
            this.placeBlockLine(start, end, BlocksTC.logGreatwood.defaultBlockState());
        }
    }

    private void generateLeafNodeBases() {
        int[] trunkPos = new int[]{this.basePos[0], this.basePos[1], this.basePos[2]};

        for (int[] node : this.leafNodes) {
            int[] nodePos = new int[]{node[0], node[1], node[2]};
            trunkPos[1] = node[3];

            int heightDiff = trunkPos[1] - this.basePos[1];
            if (this.leafNodeNeedsBase(heightDiff)) {
                this.placeBlockLine(trunkPos, nodePos, BlocksTC.logGreatwood.defaultBlockState());
            }
        }
    }

    private void placeBlockLine(int[] start, int[] end, BlockState state) {
        int[] delta = new int[3];
        int longestAxis = 0;

        for (int i = 0; i < 3; i++) {
            delta[i] = end[i] - start[i];
            if (Math.abs(delta[i]) > Math.abs(delta[longestAxis])) {
                longestAxis = i;
            }
        }

        if (delta[longestAxis] == 0) {
            return;
        }

        byte otherAxis1 = OTHER_COORD_PAIRS[longestAxis];
        byte otherAxis2 = OTHER_COORD_PAIRS[longestAxis + 3];
        int direction = delta[longestAxis] > 0 ? 1 : -1;
        double slope1 = (double)delta[otherAxis1] / (double)delta[longestAxis];
        double slope2 = (double)delta[otherAxis2] / (double)delta[longestAxis];
        int[] current = new int[3];

        for (int step = 0; step != delta[longestAxis] + direction; step += direction) {
            current[longestAxis] = Mth.floor(start[longestAxis] + step + 0.5);
            current[otherAxis1] = Mth.floor(start[otherAxis1] + step * slope1 + 0.5);
            current[otherAxis2] = Mth.floor(start[otherAxis2] + step * slope2 + 0.5);

            int logAxis = 1;
            int dx = Math.abs(current[0] - start[0]);
            int dz = Math.abs(current[2] - start[2]);
            int maxDist = Math.max(dx, dz);

            if (maxDist > 0) {
                if (dx == maxDist) {
                    logAxis = 0; // X
                } else if (dz == maxDist) {
                    logAxis = 2; // Z
                }
            }

            BlockPos pos = new BlockPos(current[0], current[1], current[2]);
            if (this.isReplaceable(pos)) {
                BlockState logState = state;
                if (logAxis == 0) {
                    logState = state.setValue(BlockLogsTC.AXIS, Direction.Axis.X);
                } else if (logAxis == 2) {
                    logState = state.setValue(BlockLogsTC.AXIS, Direction.Axis.Z);
                }
                setBlock(this.level, pos, logState);
            }
        }
    }

    private int checkBlockLine(int[] start, int[] end) {
        int[] delta = new int[3];
        int longestAxis = 0;

        for (int i = 0; i < 3; i++) {
            delta[i] = end[i] - start[i];
            if (Math.abs(delta[i]) > Math.abs(delta[longestAxis])) {
                longestAxis = i;
            }
        }

        if (delta[longestAxis] == 0) {
            return -1;
        }

        byte otherAxis1 = OTHER_COORD_PAIRS[longestAxis];
        byte otherAxis2 = OTHER_COORD_PAIRS[longestAxis + 3];
        int direction = delta[longestAxis] > 0 ? 1 : -1;
        double slope1 = (double)delta[otherAxis1] / (double)delta[longestAxis];
        double slope2 = (double)delta[otherAxis2] / (double)delta[longestAxis];
        int[] current = new int[3];
        int step = 0;

        for (int limit = delta[longestAxis] + direction; step != limit; step += direction) {
            current[longestAxis] = start[longestAxis] + step;
            current[otherAxis1] = Mth.floor(start[otherAxis1] + step * slope1);
            current[otherAxis2] = Mth.floor(start[otherAxis2] + step * slope2);

            BlockState state = this.level.getBlockState(new BlockPos(current[0], current[1], current[2]));
            if (!state.isAir() && !state.is(BlockTags.LEAVES)) {
                break;
            }
        }

        return step == delta[longestAxis] + direction ? -1 : Math.abs(step);
    }

    private boolean validTreeLocation(int offsetX, int offsetZ) {
        int x = this.basePos[0] + offsetX;
        int z = this.basePos[2] + offsetZ;

        BlockPos groundPos = new BlockPos(x, this.basePos[1] - 1, z);
        BlockState groundState = this.level.getBlockState(groundPos);

        boolean isSoil = groundState.canSustainPlant(
                this.level,
                groundPos,
                Direction.UP,
                (BushBlock) BlocksTC.saplingGreatwood
        );

        if (!isSoil) {
            return false;
        }

        int[] start = new int[]{x, this.basePos[1], z};
        int[] end = new int[]{x, this.basePos[1] + this.heightLimit - 1, z};

        int blockage = this.checkBlockLine(start, end);
        if (blockage == -1) {
            return true;
        }

        if (blockage < 6) {
            return false;
        }

        this.heightLimit = blockage;
        return true;
    }

    private float layerSize(int layer) {
        if ((double)layer < (double)this.heightLimit * 0.3) {
            return -1.618f;
        }

        float halfHeight = (float)this.heightLimit / 2.0f;
        float distFromMiddle = halfHeight - (float)layer;

        if (distFromMiddle == 0.0f) {
            return halfHeight * 0.5f;
        }

        if (Math.abs(distFromMiddle) >= halfHeight) {
            return 0.0f;
        }

        float radius = (float)Math.sqrt(
                Math.pow(Math.abs(halfHeight), 2.0) -
                        Math.pow(Math.abs(distFromMiddle), 2.0)
        );

        return radius * 0.5f;
    }

    private float leafSize(int distance) {
        if (distance >= 0 && distance < this.leafDistanceLimit) {
            return (distance != 0 && distance != this.leafDistanceLimit - 1) ? 3.0f : 2.0f;
        }
        return -1.0f;
    }

    private boolean leafNodeNeedsBase(int height) {
        return (double)height >= (double)this.heightLimit * 0.2;
    }

    private boolean isReplaceable(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES) || state.getMaterial().isReplaceable();
    }

    private void generateSpiderNest(BlockPos treeBase) {
        //@TODO
    }

    private boolean isNearTreeBlocks(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState state = this.level.getBlockState(pos.relative(dir));
            if (state.is(BlocksTC.leafGreatwood) || state.is(BlocksTC.logGreatwood)) {
                return true;
            }
        }
        return false;
    }
}