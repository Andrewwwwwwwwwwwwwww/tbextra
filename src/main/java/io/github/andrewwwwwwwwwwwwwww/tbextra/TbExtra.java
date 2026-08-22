package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.tiviacz.travelersbackpack.init.ModBlocks;
import com.tiviacz.travelersbackpack.init.ModItemGroups;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Reskins for Traveler's Backpack.
 *
 * This mod registers no backpacks of its own. A reskin is a data component on Traveler's
 * Backpack's own item, so a reskinned pack is still the pack it was crafted from - same
 * ability, same tier, same upgrades, same contents. Only its name and model change.
 */
public class TbExtra implements ModInitializer {
    public static final String MODID = "tbextra";

    // Typed to ShapedRecipe because ShapedRecipe.getSerializer() is invariant.
    public static RecipeSerializer<ShapedRecipe> SKIN_RECIPE_SERIALIZER;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    @Override
    public void onInitialize() {
        TbExtraComponents.init();

        SKIN_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("reskin"),
                new RecipeSerializer<>(
                        SkinRecipe.CODEC.xmap(recipe -> (ShapedRecipe) recipe, recipe -> (SkinRecipe) recipe),
                        SkinRecipe.STREAM_CODEC.map(recipe -> (ShapedRecipe) recipe, recipe -> (SkinRecipe) recipe)));

        // The mod registers no items, so without this a skin is only ever obtainable by
        // crafting and never appears in creative or in a recipe viewer's item list. Both
        // JEI and REI build that list from the creative tabs.
        CreativeModeTabEvents.modifyOutputEvent(ModItemGroups.TRAVELERS_BACKPACK).register(output -> {
            for (String skin : BackpackSkins.ALL) {
                ItemStack showcase = showcase(skin);
                if (!showcase.isEmpty()) {
                    output.accept(showcase);
                }
            }
        });
    }

    /**
     * A skin shown on a plain backpack, matching how the recipe book previews it. Built when
     * the tab is filled rather than at init, because Traveler's Backpack registers its blocks
     * in its own entrypoint and ours may run first.
     */
    private static ItemStack showcase(String skin) {
        if (ModBlocks.STANDARD_TRAVELERS_BACKPACK == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = ModItemGroups.withTanks(ModBlocks.STANDARD_TRAVELERS_BACKPACK);
        stack.set(TbExtraComponents.SKIN, skin);
        stack.set(DataComponents.ITEM_NAME, Component.translatable(BackpackSkins.nameKey(skin)));
        return stack;
    }
}
