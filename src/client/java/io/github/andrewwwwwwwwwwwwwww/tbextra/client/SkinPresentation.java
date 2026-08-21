package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import io.github.andrewwwwwwwwwwwwwww.tbextra.BackpackBounds;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Everything needed to draw one skin: its geometry, and where to seat it.
 *
 * Traveler's Backpack's display transforms are tuned for the centre of its own pack, so a
 * taller skin is seated lower by half the difference and scaled to match TB's footprint.
 * Both numbers come from the generated model bounds rather than being hand-tuned.
 */
public final class SkinPresentation {
    /** Height of Traveler's Backpack's own pack, in blocks (10.1px). */
    private static final float REFERENCE_HEIGHT = 10.1F / 16.0F;

    /** Longest edge of TB's pack, so skins sit at a consistent size next to each other. */
    private static final float REFERENCE_EDGE = 0.875F;

    private static final Map<String, SkinPresentation> CACHE = new ConcurrentHashMap<>();

    private final BackpackVariant variant;
    private final BackpackSpecialRenderer itemRenderer;
    private final ItemTransforms transforms;

    private SkinPresentation(String skin) {
        BackpackBounds.Bounds bounds = BackpackBounds.of(skin);
        BackpackVariant variant = BackpackVariant.of(skin);
        float drop = (bounds.height() - REFERENCE_HEIGHT) / 2.0F;

        this.variant = variant;
        this.itemRenderer = new BackpackSpecialRenderer(variant, new Vector3f(0.5F, -drop, 0.5F));
        this.transforms = createTransforms(REFERENCE_EDGE / bounds.longestEdge());
    }

    public static SkinPresentation of(String skin) {
        return CACHE.computeIfAbsent(skin, SkinPresentation::new);
    }

    public BackpackSpecialRenderer itemRenderer() {
        return itemRenderer;
    }

    public BackpackVariant variant() {
        return variant;
    }

    public ItemTransforms transforms() {
        return transforms;
    }

    /** Traveler's Backpack's own display transforms, so skins present like the base packs. */
    private static ItemTransforms createTransforms(float scale) {
        ItemTransform thirdPerson = transform(60.0F, -180.0F, 0.0F, 0.0F, 0.09375F, 0.03125F, 0.7F * scale);
        ItemTransform firstPerson = transform(0.0F, -90.0F, 12.5F, 0.070625F, 0.375F, 0.125F, 0.68F * scale);
        ItemTransform head = transform(0.0F, 180.0F, 0.0F, 0.0F, 0.90625F, 0.0F, scale);
        ItemTransform gui = transform(30.0F, -38.0F, 0.0F, -0.015625F, 0.140625F, 0.0F, scale);
        ItemTransform ground = transform(0.0F, 0.0F, 0.0F, 0.0F, 0.125F, 0.0F, 0.5F * scale);
        ItemTransform fixed = transform(0.0F, 180.0F, 0.0F, 0.0F, 0.140625F, 0.0F, scale);
        return new ItemTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson,
                head, gui, ground, fixed, fixed);
    }

    private static ItemTransform transform(float rotX, float rotY, float rotZ,
                                           float transX, float transY, float transZ, float scale) {
        return new ItemTransform(
                new Vector3f(rotX, rotY, rotZ),
                new Vector3f(transX, transY, transZ),
                new Vector3f(scale, scale, scale));
    }
}
