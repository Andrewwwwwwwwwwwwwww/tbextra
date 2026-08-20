package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.tiviacz.travelersbackpack.init.ModBlockEntityTypes;
import io.github.andrewwwwwwwwwwwwwww.tbextra.TbExtra;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class TbExtraClient implements ClientModInitializer {

    // BlockEntityRendererRegistry is deprecated in Fabric API, but it is still the only
    // public way to attach a renderer to another mod's block entity type.
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        // Our own item model type, registered the same way Traveler's Backpack registers its.
        ItemModels.ID_MAPPER.put(TbExtra.id("backpack"), BackpackItemModel.Unbaked.MAP_CODEC);

        // Placed backpacks: TB has no block entity renderer of its own, so registering one
        // against its type is free. We no-op for packs that are not ours.
        BlockEntityRendererRegistry.register(ModBlockEntityTypes.BACKPACK, context -> new BackpackBlockRenderer());
    }

    /** The variant for one of our blocks, or null if this is not our backpack. */
    @Nullable
    public static BackpackVariant variantFor(Block block) {
        if (!TbExtra.BACKPACKS.contains(block)) {
            return null;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return id == null ? null : BackpackVariant.of(id.getPath());
    }
}
