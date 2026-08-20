package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tiviacz.travelersbackpack.block.TravelersBackpackBlock;
import com.tiviacz.travelersbackpack.blockentity.BackpackBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Renders our backpacks when placed in the world.
 *
 * This is registered against Traveler's Backpack's own block entity type, so it sees every
 * backpack. Anything that is not one of ours is left alone and keeps rendering through TB's
 * normal baked model.
 */
public class BackpackBlockRenderer implements BlockEntityRenderer<BackpackBlockEntity, BackpackBlockRenderState> {

    @Override
    public BackpackBlockRenderState createRenderState() {
        return new BackpackBlockRenderState();
    }

    @Override
    public void extractRenderState(BackpackBlockEntity blockEntity, BackpackBlockRenderState state,
                                   float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, breakProgress);

        BlockState blockState = blockEntity.getBlockState();
        state.variant = TbExtraClient.variantFor(blockState.getBlock());
        state.facing = blockState.hasProperty(TravelersBackpackBlock.FACING)
                ? blockState.getValue(TravelersBackpackBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public void submit(BackpackBlockRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        BackpackVariant variant = state.variant;
        if (variant == null) {
            return; // one of Traveler's Backpack's own packs - not ours to draw
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-state.facing.toYRot()));

        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(variant.texture()),
                (pose, consumer) -> variant.geometry().emit(pose, consumer, light,
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY));

        poseStack.popPose();
    }
}
