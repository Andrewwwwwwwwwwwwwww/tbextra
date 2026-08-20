package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.andrewwwwwwwwwwwwwww.tbextra.BackpackBounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * Item model type "tbextra:backpack". The item model JSON only names which pack to draw;
 * the geometry comes from the baked quad list.
 */
public class BackpackItemModel implements ItemModel {
    /**
     * Longest edge of Traveler's Backpack's own pack, in blocks. Each pack is scaled so its
     * longest edge matches, so a wide pack does not tower over a narrow one in the inventory.
     */
    private static final float REFERENCE_EDGE = 0.875F;

    private final BackpackSpecialRenderer renderer;
    private final ItemTransforms transforms;

    public BackpackItemModel(BackpackVariant variant, String backpack) {
        this.renderer = new BackpackSpecialRenderer(variant);
        this.transforms = createTransforms(REFERENCE_EDGE / BackpackBounds.of(backpack).longestEdge());
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        layer.setUsesBlockLight(true);
        // getTransform returns NO_TRANSFORM for ItemDisplayContext.NONE, which is what
        // Traveler's Backpack uses for the worn pack - it positions that itself.
        layer.setItemTransform(transforms.getTransform(context));
        layer.setupSpecialModel(renderer, null);
    }

    /**
     * Traveler's Backpack's own display transforms, so these packs sit the same way in the
     * hand, inventory and item frames as the ones players already know. Scale is adjusted
     * per pack; translations are in blocks.
     */
    private static ItemTransforms createTransforms(float scale) {
        ItemTransform thirdPerson = transform(60.0F, -180.0F, 0.0F, 0.0F, 0.09375F, 0.03125F, 0.7F * scale);
        ItemTransform firstPerson = transform(0.0F, -90.0F, 12.5F, 0.070625F, 0.375F, 0.125F, 0.68F * scale);
        ItemTransform head = transform(0.0F, 180.0F, 0.0F, 0.0F, 0.90625F, 0.0F, scale);
        ItemTransform gui = transform(30.0F, -38.0F, 0.0F, -0.015625F, 0.140625F, 0.0F, scale);
        ItemTransform ground = transform(0.0F, 0.0F, 0.0F, 0.0F, 0.125F, 0.0F, 0.5F * scale);
        ItemTransform fixed = transform(0.0F, 180.0F, 0.0F, 0.0F, 0.140625F, 0.0F, scale);
        return new ItemTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson,
                head, gui, ground, fixed, fixed);
    }

    private static ItemTransform transform(float rotX, float rotY, float rotZ,
                                           float transX, float transY, float transZ, float scale) {
        return new ItemTransform(
                new Vector3f(rotX, rotY, rotZ),
                new Vector3f(transX, transY, transZ),
                new Vector3f(scale, scale, scale));
    }

    /** The unbaked form parsed from assets/tbextra/items/&lt;name&gt;.json. */
    public record Unbaked(String backpack) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.fieldOf("backpack").forGetter(Unbaked::backpack)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            return new BackpackItemModel(BackpackVariant.of(backpack), backpack);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            // No parent models to resolve - the geometry is baked into the mod.
        }
    }
}
