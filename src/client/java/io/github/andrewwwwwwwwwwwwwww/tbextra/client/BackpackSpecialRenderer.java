package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Draws a backpack wherever the item is shown - inventory, hand, dropped, and on the
 * player's back, since Traveler's Backpack renders the worn pack from the item model.
 *
 * The geometry is emitted in block space, the same layout a block model uses, because that
 * is what both the item display transforms and TB's own back-positioning code expect.
 */
public record BackpackSpecialRenderer(BackpackVariant variant) implements SpecialModelRenderer<Void> {
    /** Centres the pack on the block footprint; the generator lays it out around the origin. */
    private static final Vector3f BLOCK_SPACE = new Vector3f(0.5F, 0.0F, 0.5F);

    @Override
    public void submit(Void argument, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(BLOCK_SPACE.x, BLOCK_SPACE.y, BLOCK_SPACE.z);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(variant.texture()),
                (pose, consumer) -> variant.geometry().emit(pose, consumer, light, overlay));
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        variant.geometry().extents(corner -> consumer.accept(new Vector3f(corner).add(BLOCK_SPACE)));
    }

    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }
}
