package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Reskins any Traveler's Backpack, keeping the pack itself intact:
 * <pre>
 *   M M M
 *   M B M     B = any Traveler's Backpack
 *   M U M     U = an empty bundle
 * </pre>
 * The result is the same backpack stack that went in - same item, ability, tier, upgrades
 * and contents - with a skin and a new name applied.
 *
 * Extending {@link ShapedRecipe} rather than writing a recipe from scratch means the recipe
 * book can show it and lay it out, which a fully custom recipe cannot do.
 */
public class SkinRecipe extends ShapedRecipe {
    private static final char MATERIAL = 'M';
    private static final char BACKPACK = 'B';
    private static final char BUNDLE = 'U';

    private static final Identifier DISPLAY_BACKPACK =
            Identifier.fromNamespaceAndPath("travelersbackpack", "standard");

    public static final MapCodec<SkinRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("skin").forGetter(SkinRecipe::skin),
                    Ingredient.CODEC.fieldOf("material").forGetter(SkinRecipe::material),
                    Ingredient.CODEC.fieldOf("backpack").forGetter(SkinRecipe::backpack)
            ).apply(instance, SkinRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkinRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SkinRecipe::skin,
                    Ingredient.CONTENTS_STREAM_CODEC, SkinRecipe::material,
                    Ingredient.CONTENTS_STREAM_CODEC, SkinRecipe::backpack,
                    SkinRecipe::new);

    private final String skin;
    private final Ingredient material;
    private final Ingredient backpack;

    public SkinRecipe(String skin, Ingredient material, Ingredient backpack) {
        super(new Recipe.CommonInfo(true),
                // No group: recipes sharing one are collapsed into a single cycling
                // entry in the recipe book, which reads as a glitch for separate skins.
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.EQUIPMENT, ""),
                pattern(material, backpack),
                displayResult(skin));
        this.skin = skin;
        this.material = material;
        this.backpack = backpack;
    }

    public String skin() {
        return skin;
    }

    public Ingredient material() {
        return material;
    }

    public Ingredient backpack() {
        return backpack;
    }

    /**
     * The backpack slot is supplied by the recipe file rather than built here. Recipes are
     * decoded before tags are bound, so resolving the tag in this constructor throws.
     */
    private static ShapedRecipePattern pattern(Ingredient material, Ingredient backpack) {
        return ShapedRecipePattern.of(Map.of(
                Character.valueOf(MATERIAL), material,
                Character.valueOf(BACKPACK), backpack,
                Character.valueOf(BUNDLE), Ingredient.of(Items.BUNDLE)
        ), "MMM", "MBM", "MUM");
    }

    /** What the recipe book shows: a plain backpack wearing the skin. */
    private static ItemStackTemplate displayResult(String skin) {
        Item backpack = BuiltInRegistries.ITEM.getValue(DISPLAY_BACKPACK);
        return new ItemStackTemplate(backpack, DataComponentPatch.builder()
                .set(TbExtraComponents.SKIN, skin)
                .set(DataComponents.ITEM_NAME, Component.translatable(BackpackSkins.nameKey(skin)))
                .build());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return super.matches(input, level) && findBundle(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack backpack = findBackpack(input);
        if (backpack == null) {
            return ItemStack.EMPTY;
        }
        // Copying the stack keeps the item and every component, so the pack's ability, tier,
        // upgrades and contents all survive. Only the look and the name change.
        ItemStack result = backpack.copy();
        result.setCount(1);
        result.set(TbExtraComponents.SKIN, skin);
        result.set(DataComponents.ITEM_NAME, Component.translatable(BackpackSkins.nameKey(skin)));
        return result;
    }

    private ItemStack findBackpack(CraftingInput input) {
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty() && backpack.test(stack)) {
                return stack;
            }
        }
        return null;
    }

    /** Only an empty bundle will do - a full one would have its contents destroyed. */
    private static ItemStack findBundle(CraftingInput input) {
        for (ItemStack stack : input.items()) {
            if (stack.is(Items.BUNDLE)) {
                BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
                return contents == null || contents.isEmpty() ? stack : null;
            }
        }
        return null;
    }

    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return TbExtra.SKIN_RECIPE_SERIALIZER;
    }
}
