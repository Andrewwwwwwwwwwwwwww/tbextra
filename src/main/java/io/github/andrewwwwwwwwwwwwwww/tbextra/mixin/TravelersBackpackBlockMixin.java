package io.github.andrewwwwwwwwwwwwwww.tbextra.mixin;

import com.tiviacz.travelersbackpack.block.TravelersBackpackBlock;
import io.github.andrewwwwwwwwwwwwwww.tbextra.PlacedSkin;
import io.github.andrewwwwwwwwwwwwwww.tbextra.SkinShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives a reskinned backpack a collision shape that matches the skin. Traveler's Backpack
 * picks from four fixed shapes sized for its own pack, and cannot know about ours.
 */
@Mixin(TravelersBackpackBlock.class)
public class TravelersBackpackBlockMixin {
    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void tbextra$skinnedShape(BlockState state, BlockGetter level, BlockPos pos,
                                      CollisionContext context,
                                      CallbackInfoReturnable<VoxelShape> cir) {
        String skin = PlacedSkin.at(level, pos);
        if (skin == null) {
            return; // an ordinary pack - leave Traveler's Backpack's shape alone
        }
        Direction facing = state.hasProperty(TravelersBackpackBlock.FACING)
                ? state.getValue(TravelersBackpackBlock.FACING)
                : Direction.NORTH;
        cir.setReturnValue(SkinShapes.get(skin, facing));
    }
}
