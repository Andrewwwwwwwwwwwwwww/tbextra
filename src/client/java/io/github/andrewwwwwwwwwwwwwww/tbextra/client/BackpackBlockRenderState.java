package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/** Carries which of our packs is at this position, and which way it faces. */
public class BackpackBlockRenderState extends BlockEntityRenderState {
    @Nullable
    public BackpackVariant variant;
    public Direction facing = Direction.NORTH;
}
