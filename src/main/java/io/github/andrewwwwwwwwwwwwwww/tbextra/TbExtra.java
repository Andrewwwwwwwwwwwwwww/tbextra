package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.tiviacz.travelersbackpack.block.TravelersBackpackBlock;
import com.tiviacz.travelersbackpack.init.ModBlockEntityTypes;
import com.tiviacz.travelersbackpack.init.ModItemGroups;
import com.tiviacz.travelersbackpack.item.TravelersBackpackItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
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

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    @Override
    public void onInitialize() {
        register("firewatch", MapColor.COLOR_BROWN, SoundType.WOOL);
        register("trapper", MapColor.COLOR_BROWN, SoundType.WOOL);

        CreativeModeTabEvents.modifyOutputEvent(ModItemGroups.TRAVELERS_BACKPACK).register(output -> {
            for (Block backpack : BACKPACKS) {
                output.accept(ModItemGroups.withTanks(backpack));
            }
        });
    }

    private static void register(String name, MapColor color, SoundType sound) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(name));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(name));

        Block block = Registry.register(BuiltInRegistries.BLOCK, id(name),
                new TravelersBackpackBlock(BlockBehaviour.Properties.of()
                        .mapColor(color)
                        .sound(sound)
                        .setId(blockKey)));

        Registry.register(BuiltInRegistries.ITEM, id(name),
                new TravelersBackpackItem(new Item.Properties().setId(itemKey), block));

        // Traveler's Backpack builds its block entity type from a fixed block list, and
        // LevelChunk drops any block entity whose type does not accept the block. Without
        // this, a placed backpack would lose its contents on reload.
        ((FabricBlockEntityType) ModBlockEntityTypes.BACKPACK).addValidBlock(block);

        BACKPACKS.add(block);
    }
}
