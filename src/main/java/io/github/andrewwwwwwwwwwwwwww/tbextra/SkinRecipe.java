package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Reskins any Traveler's Backpack, keeping the pack itself intact.
 *
 * The grid is fixed:
 * <pre>
 *   M M M
 *   M B M     B = any Traveler's Backpack
 *   M U M     U = an empty bundle
 * </pre>
 * where M is the skin's material. The result is the very same backpack stack - same item,
 * same ability, same tier, same contents - with a skin and a new name applied.
 */
public record SkinRecipe(String skin, Ingredient material) implements CraftingRecipe {
    /** Traveler's Backpack tags all of its own packs here, so "any backpack" comes free. */
    public static final TagKey<Item> ANY_BACKPACK = TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("travelersbackpack", "custom_travelers_backpack"));

    public static final MapCodec<SkinRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("skin").forGetter(SkinRecipe::skin),
                    Ingredient.CODEC.fieldOf("material").forGetter(SkinRecipe::material)
            ).apply(instance, SkinRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkinRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SkinRecipe::skin,
                    Ingredient.CONTENTS_STREAM_CODEC, SkinRecipe::material,
                    SkinRecipe::new);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                ItemStack stack = input.getItem(column, row);
                boolean ok = switch (column * 10 + row) {
                    case 11 -> isBackpack(stack);
                    case 12 -> isEmptyBundle(stack);
                    default -> material.test(stack);
                };
                if (!ok) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isBackpack(ItemStack stack) {
        return stack.is(ANY_BACKPACK);
    }

    /** An empty bundle only - a full one would have its contents silently destroyed. */
    private static boolean isEmptyBundle(ItemStack stack) {
        if (!stack.is(Items.BUNDLE)) {
            return false;
        }
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        return contents == null || contents.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack backpack = input.getItem(1, 1);
        // Copying the stack keeps the item and every component, so the pack's ability, tier,
        // upgrades and contents all survive. Only the look and the name change.
        ItemStack result = backpack.copy();
        result.setCount(1);
        result.set(TbExtraComponents.SKIN, skin);
        result.set(DataComponents.ITEM_NAME, Component.translatable(BackpackSkins.nameKey(skin)));
        return result;
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
        return TbExtra.SKIN_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_EQUIPMENT;
    }

    @Override
    public PlacementInfo placementInfo() {
        // "Any backpack" cannot be expressed as a fixed ingredient list, so the recipe book
        // cannot auto-fill this one.
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return TbExtra.MODID + ":reskin";
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
