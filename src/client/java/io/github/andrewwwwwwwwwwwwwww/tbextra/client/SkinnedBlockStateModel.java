package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import io.github.andrewwwwwwwwwwwwwww.tbextra.PlacedSkin;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Wraps Traveler's Backpack's block model. A placed pack wearing one of our skins draws
 * nothing here - {@link BackpackBlockRenderer} draws the skin instead, because our textures
 * live outside the block atlas. Unskinned packs render exactly as TB intends.
 */
public class SkinnedBlockStateModel extends WrapperBlockStateModel {
    public SkinnedBlockStateModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos,
                          BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        if (PlacedSkin.at(level, pos) != null) {
            return;
        }
        super.emitQuads(emitter, level, pos, state, random, cullTest);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                    RandomSource random) {
        String skin = PlacedSkin.at(level, pos);
        Object wrappedKey = super.createGeometryKey(level, pos, state, random);
        if (skin == null) {
            return wrappedKey;
        }
        // Chunk meshes are cached by this key, so a skinned pack has to key differently from
        // an unskinned one or the wrong geometry gets reused.
        return java.util.List.of(skin, String.valueOf(wrappedKey));
    }
}
