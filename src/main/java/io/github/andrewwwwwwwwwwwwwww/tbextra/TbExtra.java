package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.tiviacz.travelersbackpack.init.ModBlockEntityTypes;
import com.tiviacz.travelersbackpack.init.ModItemGroups;
import com.tiviacz.travelersbackpack.item.TravelersBackpackItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers extra backpacks that reuse Traveler's Backpack wholesale - the same block,
 * item, block entity, inventory, upgrades and screens. Only the model is ours.
 */
public class TbExtra implements ModInitializer {
    public static final String MODID = "tbextra";

    /** Every backpack this mod adds, in creative-tab order. */
    public static final List<Block> BACKPACKS = new ArrayList<>();

    private static boolean linked = false;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    @Override
    public void onInitialize() {
        register("firewatch", MapColor.COLOR_BROWN, SoundType.WOOL);
        register("trapper", MapColor.COLOR_BROWN, SoundType.WOOL);

        // Fabric invokes entrypoints alphabetically, so "tbextra" runs before
        // "travelersbackpack" and its block entity type is still null at this point.
        // "depends" guarantees the mod is present, not that it initialised first, so link
        // up at a point where it definitely has rather than relying on load order.
        // Dedicated servers get it here; clients get it from TbExtraClient, since Fabric
        // runs every "main" entrypoint before any "client" one.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> linkBlockEntityType());
        linkBlockEntityType();

        CreativeModeTabEvents.modifyOutputEvent(ModItemGroups.TRAVELERS_BACKPACK).register(output -> {
            for (Block backpack : BACKPACKS) {
                output.accept(ModItemGroups.withTanks(backpack));
            }
        });
    }

    /**
     * Adds our blocks to Traveler's Backpack's block entity type.
     *
     * TB builds that type from a fixed block list, and LevelChunk discards any block entity
     * whose type does not accept its block - without this a placed backpack would lose its
     * contents on reload. Safe to call repeatedly; a no-op until TB has initialised.
     */
    public static synchronized void linkBlockEntityType() {
        if (linked || ModBlockEntityTypes.BACKPACK == null) {
            return;
        }
        for (Block backpack : BACKPACKS) {
            ((FabricBlockEntityType) ModBlockEntityTypes.BACKPACK).addValidBlock(backpack);
        }
        linked = true;
    }

    private static void register(String name, MapColor color, SoundType sound) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(name));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(name));

        Block block = Registry.register(BuiltInRegistries.BLOCK, id(name),
                new ExtraBackpackBlock(BlockBehaviour.Properties.of()
                        .mapColor(color)
                        .sound(sound)
                        .setId(blockKey), name));

        Registry.register(BuiltInRegistries.ITEM, id(name),
                new TravelersBackpackItem(new Item.Properties().setId(itemKey), block));

        BACKPACKS.add(block);
    }
}
