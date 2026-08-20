package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.tiviacz.travelersbackpack.block.TravelersBackpackBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * A Traveler's Backpack block whose collision shape follows our own model rather than
 * inheriting the shape of the vanilla pack, which is a different size.
 */
public class ExtraBackpackBlock extends TravelersBackpackBlock {
    private final Map<Direction, VoxelShape> shapes;

    public ExtraBackpackBlock(BlockBehaviour.Properties properties, String backpack) {
        super(properties);
        this.shapes = buildShapes(BackpackBounds.of(backpack));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return shapes.getOrDefault(facing, Shapes.block());
    }

    /**
     * The model is authored facing south, and the renderer spins it by -facing.toYRot().
     * Rotate the box the same way so the hitbox always tracks what is drawn.
     */
    private static Map<Direction, VoxelShape> buildShapes(BackpackBounds.Bounds bounds) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            double angle = Math.toRadians(-facing.toYRot());
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
            double[][] corners = {
                    {bounds.x0(), bounds.z0()}, {bounds.x0(), bounds.z1()},
                    {bounds.x1(), bounds.z0()}, {bounds.x1(), bounds.z1()}
            };
            for (double[] corner : corners) {
                double x = corner[0] - 0.5;
                double z = corner[1] - 0.5;
                double rx = x * cos + z * sin + 0.5;
                double rz = -x * sin + z * cos + 0.5;
                minX = Math.min(minX, rx);
                maxX = Math.max(maxX, rx);
                minZ = Math.min(minZ, rz);
                maxZ = Math.max(maxZ, rz);
            }

            // A pack may overhang the block it sits on; the shape cannot, so clamp it.
            shapes.put(facing, Shapes.box(
                    clamp(minX), clamp(bounds.y0()), clamp(minZ),
                    clamp(maxX), clamp(bounds.y1()), clamp(maxZ)));
        }
        return shapes;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
