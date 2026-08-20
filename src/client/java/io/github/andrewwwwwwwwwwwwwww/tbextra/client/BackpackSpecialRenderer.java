package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Draws a backpack wherever the item is shown - inventory, hand, dropped, and on the
 * player's back, since Traveler's Backpack renders the worn pack from the item model.
 */
public record BackpackSpecialRenderer(BackpackVariant variant) implements SpecialModelRenderer<Void> {

    @Override
    public void submit(Void argument, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean hasFoil, int outlineColor) {
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(variant.texture()),
                (pose, consumer) -> variant.geometry().emit(pose, consumer, light, overlay));
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        variant.geometry().extents(consumer);
    }

    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }
}
