package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import io.github.andrewwwwwwwwwwwwwww.tbextra.PlacedSkin;
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

        BackpackVariant variant = SkinPresentation.of(skin).variant();
        int light = state.lightCoords;

        poseStack.pushPose();
        // The geometry is centred on X/Z around the origin and stands on y=0, so move to the
        // middle of the block first and turn about that centre - rotating first would swing
        // the pack out to a corner.
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        collector.submitCustomGeometry(poseStack,
                RenderTypes.entityCutout(variant.texture()),
                (pose, consumer) -> variant.geometry()
                        .emit(pose, consumer, light, OverlayTexture.NO_OVERLAY));
        poseStack.popPose();
    }
}
