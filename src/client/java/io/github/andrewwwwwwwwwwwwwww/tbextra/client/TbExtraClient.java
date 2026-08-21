package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.tiviacz.travelersbackpack.init.ModBlockEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class TbExtraClient implements ClientModInitializer {
    private static final String TRAVELERS_BACKPACK = "travelersbackpack";

    // BlockEntityRendererRegistry is deprecated in Fabric API, but it is still the only
    // public way to attach a renderer to another mod's block entity type.
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(context -> {
            // Held and carried packs: swap in the skin's model when the stack has one.
            context.modifyItemModelAfterBake().register((model, ctx) -> {
                Identifier itemId = ctx.itemId();
                return itemId != null && TRAVELERS_BACKPACK.equals(itemId.getNamespace())
                        ? new SkinnedItemModel(model)
                        : model;
            });

            // Placed packs: hide TB's block model for skinned packs so the block entity
            // renderer can draw the skin in its place.
            context.modifyBlockModelAfterBake().register((model, ctx) -> {
                Block block = ctx.state().getBlock();
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
                return blockId != null && TRAVELERS_BACKPACK.equals(blockId.getNamespace())
                        ? new SkinnedBlockStateModel(model)
                        : model;
            });
        });

        BlockEntityRendererRegistry.register(ModBlockEntityTypes.BACKPACK,
                context -> new BackpackBlockRenderer());
    }
}
