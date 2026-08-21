package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.tiviacz.travelersbackpack.blockentity.BackpackBlockEntity;
import io.github.andrewwwwwwwwwwwwwww.tbextra.BackpackSkins;
import io.github.andrewwwwwwwwwwwwwww.tbextra.TbExtraComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/** Reads the skin off a placed backpack, if it has one. */
public final class PlacedSkin {
    private PlacedSkin() {
    }

    @Nullable
    public static String at(@Nullable BlockGetter level, BlockPos pos) {
        if (level == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return of(blockEntity);
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
