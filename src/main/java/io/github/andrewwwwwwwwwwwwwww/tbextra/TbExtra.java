package io.github.andrewwwwwwwwwwwwwww.tbextra;

import net.fabricmc.api.ModInitializer;
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
    }
}
