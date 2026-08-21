package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/** Which skin a placed pack is wearing, if any, and which way it faces. */
public class BackpackBlockRenderState extends BlockEntityRenderState {
    @Nullable
    public String skin;
    public Direction facing = Direction.NORTH;
}
