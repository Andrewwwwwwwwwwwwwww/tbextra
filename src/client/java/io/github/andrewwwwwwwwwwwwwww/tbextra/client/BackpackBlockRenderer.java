package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tiviacz.travelersbackpack.block.TravelersBackpackBlock;
import com.tiviacz.travelersbackpack.blockentity.BackpackBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Draws placed backpacks that are wearing one of our skins. Registered against Traveler's
 * Backpack's own block entity type, so it sees every pack and ignores the ones without a
 * skin - those keep rendering through TB's normal block model.
 */
public class BackpackBlockRenderer
        implements BlockEntityRenderer<BackpackBlockEntity, BackpackBlockRenderState> {

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
        state.skin = PlacedSkin.of(blockEntity);
        state.facing = blockState.hasProperty(TravelersBackpackBlock.FACING)
                ? blockState.getValue(TravelersBackpackBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public void submit(BackpackBlockRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        String skin = state.skin;
        if (skin == null) {
            return; // an ordinary Traveler's Backpack - not ours to draw
        }

        BackpackSpecialRenderer renderer = SkinPresentation.of(skin).blockRenderer();
        int light = state.lightCoords;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        collector.submitCustomGeometry(poseStack,
                RenderTypes.entityCutout(renderer.variant().texture()),
                (pose, consumer) -> renderer.variant().geometry()
                        .emit(pose, consumer, light, OverlayTexture.NO_OVERLAY));
        poseStack.popPose();
    }
}
