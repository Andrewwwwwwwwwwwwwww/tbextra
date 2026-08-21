package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.tiviacz.travelersbackpack.blockentity.BackpackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Reads the skin off a placed backpack. Common code: the client needs it to draw the pack,
 * and the server needs it to give the pack the right collision shape.
 */
public final class PlacedSkin {
    private PlacedSkin() {
    }

    @Nullable
    public static String at(@Nullable BlockGetter level, BlockPos pos) {
        return level == null ? null : of(level.getBlockEntity(pos));
    }

    @Nullable
    public static String of(@Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof BackpackBlockEntity backpack)) {
            return null;
        }
        ItemStack stack = backpack.getWrapper().getBackpackStack();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        String skin = stack.get(TbExtraComponents.SKIN);
        return skin != null && BackpackSkins.isKnown(skin) ? skin : null;
    }
}
