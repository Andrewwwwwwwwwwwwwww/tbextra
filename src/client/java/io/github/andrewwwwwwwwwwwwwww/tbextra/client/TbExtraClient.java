package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.tiviacz.travelersbackpack.block.TravelersBackpackBlock;
import com.tiviacz.travelersbackpack.init.ModBlockEntityTypes;
import com.tiviacz.travelersbackpack.item.TravelersBackpackItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class TbExtraClient implements ClientModInitializer {
    // BlockEntityRendererRegistry is deprecated in Fabric API, but it is still the only
    // public way to attach a renderer to another mod's block entity type.
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(context -> {
            // Held and carried packs: swap in the skin's model when the stack has one.
            context.modifyItemModelAfterBake().register((model, ctx) -> {
                Identifier itemId = ctx.itemId();
                if (itemId == null) {
                    return model;
                }
                // Only actual backpacks - not hoses, tanks, upgrades or sleeping bags.
                return BuiltInRegistries.ITEM.getValue(itemId) instanceof TravelersBackpackItem
                        ? new SkinnedItemModel(model)
                        : model;
            });

            // Placed packs: hide TB's block model for skinned packs so the block entity
            // renderer can draw the skin in its place.
            context.modifyBlockModelAfterBake().register((model, ctx) -> {
                // Only backpack blocks: the wrapper looks up a block entity per chunk
                // rebuild, which is wasted on sleeping bags and everything else.
                return ctx.state().getBlock() instanceof TravelersBackpackBlock
                        ? new SkinnedBlockStateModel(model)
                        : model;
            });
        });

        BlockEntityRendererRegistry.register(ModBlockEntityTypes.BACKPACK,
                context -> new BackpackBlockRenderer());
    }
}
