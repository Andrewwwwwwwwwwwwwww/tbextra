package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * A baked quad list produced from the artist's Blockbench glTF export by tools/genquads.js.
 *
 * The packs use sub-assemblies rotated freely on all three axes (bear trap, antlers, straps),
 * which the vanilla block model format cannot express - it allows a single axis at fixed
 * angles. So the geometry is baked to world-space quads and drawn directly instead.
 *
 * Layout per quad: four vertices of (x, y, z, u, v) followed by a face normal (x, y, z).
 */
public final class BackpackGeometry {
    private static final int MAGIC = 0x54424758;
    private static final int FLOATS_PER_QUAD = 23;

    private final float[] data;
    private final int quadCount;
    private final Vector3f min;
    private final Vector3f max;

    private BackpackGeometry(float[] data, int quadCount, Vector3f min, Vector3f max) {
        this.data = data;
        this.quadCount = quadCount;
        this.min = min;
        this.max = max;
    }

    public static BackpackGeometry load(String name) {
        String path = "/assets/tbextra/geometry/" + name + ".bin";
        try (InputStream in = BackpackGeometry.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("missing geometry resource " + path);
            }
            DataInputStream data = new DataInputStream(in);
            if (data.readInt() != MAGIC) {
                throw new IOException("bad magic in " + path);
            }
            int version = data.readInt();
            if (version != 1) {
                throw new IOException("unsupported geometry version " + version + " in " + path);
            }
            int quadCount = data.readInt();
            float[] floats = new float[quadCount * FLOATS_PER_QUAD];
            Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
            for (int i = 0; i < floats.length; i++) {
                floats[i] = data.readFloat();
            }
            for (int q = 0; q < quadCount; q++) {
                int base = q * FLOATS_PER_QUAD;
                for (int v = 0; v < 4; v++) {
                    int o = base + v * 5;
                    min.set(Math.min(min.x, floats[o]), Math.min(min.y, floats[o + 1]), Math.min(min.z, floats[o + 2]));
                    max.set(Math.max(max.x, floats[o]), Math.max(max.y, floats[o + 1]), Math.max(max.z, floats[o + 2]));
                }
            }
            return new BackpackGeometry(floats, quadCount, min, max);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load backpack geometry " + name, e);
        }
    }

    /** Draws every quad. Positions are in block units with the pack centred on X/Z and sitting on y=0. */
    public void emit(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay) {
        for (int q = 0; q < quadCount; q++) {
            int base = q * FLOATS_PER_QUAD;
            float nx = data[base + 20];
            float ny = data[base + 21];
            float nz = data[base + 22];
            for (int v = 0; v < 4; v++) {
                int o = base + v * 5;
                consumer.addVertex(pose, data[o], data[o + 1], data[o + 2])
                        .setColor(-1)
                        .setUv(data[o + 3], data[o + 4])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, nx, ny, nz);
            }
        }
    }

    public void extents(Consumer<Vector3fc> consumer) {
        consumer.accept(min);
        consumer.accept(max);
    }
}
