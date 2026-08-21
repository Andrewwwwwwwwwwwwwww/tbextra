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
 * Geometry is emitted in block space, the layout a block model uses, because that is what
 * the item display pipeline expects: ItemTransform.apply ends with translate(-0.5) so the
 * block cube is centred before rotation and scale.
 *
 * @param offset where to seat the model within that block. The block renderer stands it on
 *               the floor; item views shift it to sit where TB's own pack sits.
 */
public record BackpackSpecialRenderer(BackpackVariant variant, Vector3f offset)
        implements SpecialModelRenderer<Void> {

    @Override
    public void submit(Void argument, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(variant.texture()),
                (pose, consumer) -> variant.geometry().emit(pose, consumer, light, overlay));
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        variant.geometry().extents(corner -> consumer.accept(new Vector3f(corner).add(offset)));
    }

    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }
}
