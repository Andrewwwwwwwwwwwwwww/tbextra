package io.github.andrewwwwwwwwwwwwwww.tbextra;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Collision shapes matching each skin's model, so a reskinned pack is not left with the
 * shape of the base backpack it was crafted from.
 */
public final class SkinShapes {
    private static final Map<String, Map<Direction, VoxelShape>> CACHE = new HashMap<>();

    private SkinShapes() {
    }

    public static synchronized VoxelShape get(String skin, Direction facing) {
        return CACHE.computeIfAbsent(skin, SkinShapes::build)
                .getOrDefault(facing, Shapes.block());
    }

    /**
     * The model is authored facing south and the renderer turns it by -facing.toYRot(),
     * so the box is turned the same way to stay with what is drawn.
     */
    private static Map<Direction, VoxelShape> build(String skin) {
        BackpackBounds.Bounds bounds = BackpackBounds.of(skin);
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

            // A pack may overhang the block it stands on; a shape cannot, so clamp it.
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
