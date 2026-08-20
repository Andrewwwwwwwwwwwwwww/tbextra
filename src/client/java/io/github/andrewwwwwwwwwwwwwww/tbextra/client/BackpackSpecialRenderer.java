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
 */
public record BackpackSpecialRenderer(BackpackVariant variant) implements SpecialModelRenderer<Void> {

    @Override
    public void submit(Void argument, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean hasFoil, int outlineColor) {
        // The geometry sits on y=0 so it can be placed as a block. Item display expects the
        // model centred on the origin, otherwise it hangs above wherever it is drawn.
        Vector3f centre = variant.geometry().centre();

        poseStack.pushPose();
        poseStack.translate(-centre.x, -centre.y, -centre.z);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(variant.texture()),
                (pose, consumer) -> variant.geometry().emit(pose, consumer, light, overlay));
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        variant.geometry().centredExtents(consumer);
    }

    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }
}
