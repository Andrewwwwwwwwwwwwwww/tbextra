package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
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
     * Worn on the back. Traveler's Backpack renders that through the item model with
     * ItemDisplayContext.NONE and sets no item transform - its own BackpackModel positions
     * the pack itself, assuming the geometry sits in block space like a block model. So we
     * place ours the same way the block renderer does, and let TB do the positioning.
     */
    private final BackpackSpecialRenderer worn;

    /** Every other view: item display expects the model centred on the origin. */
    private final BackpackSpecialRenderer held;

    public BackpackItemModel(BackpackVariant variant) {
        this.worn = new BackpackSpecialRenderer(variant, new Vector3f(0.5F, 0.0F, 0.5F));
        Vector3f centre = variant.geometry().centre();
        this.held = new BackpackSpecialRenderer(variant, centre.negate());
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        if (context == ItemDisplayContext.NONE) {
            layer.setupSpecialModel(worn, null);
        } else {
            layer.setItemTransform(transformFor(context));
            layer.setupSpecialModel(held, null);
        }
    }

    /**
     * Modelled on vanilla block item display defaults, which suit a chunky upright object.
     * Translations are in blocks (vanilla model JSON states them in sixteenths).
     * The renderer centres the geometry first, so these are pure presentation.
     */
    private static ItemTransform transformFor(ItemDisplayContext context) {
        return switch (context) {
            case GUI -> transform(30.0F, 225.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.625F);
            case GROUND -> transform(0.0F, 0.0F, 0.0F, 0.0F, 0.1875F, 0.0F, 0.25F);
            case FIXED -> transform(0.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F);
            case HEAD -> transform(0.0F, 180.0F, 0.0F, 0.0F, 0.25F, 0.0F, 1.0F);
            case THIRD_PERSON_RIGHT_HAND -> transform(0.0F, 45.0F, 0.0F, 0.0F, 0.15625F, 0.0F, 0.4F);
            case THIRD_PERSON_LEFT_HAND -> transform(0.0F, 225.0F, 0.0F, 0.0F, 0.15625F, 0.0F, 0.4F);
            case FIRST_PERSON_RIGHT_HAND -> transform(0.0F, 45.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4F);
            case FIRST_PERSON_LEFT_HAND -> transform(0.0F, 225.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4F);
            default -> ItemTransform.NO_TRANSFORM;
        };
    }

    private static ItemTransform transform(float rotX, float rotY, float rotZ,
                                           float transX, float transY, float transZ, float scale) {
        return new ItemTransform(
                new Vector3f(rotX, rotY, rotZ),
                new Vector3f(transX, transY, transZ),
                new Vector3f(scale, scale, scale));
    }

    /** The unbaked form parsed from assets/tbextra/items/<name>.json. */
    public record Unbaked(String backpack) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        com.mojang.serialization.Codec.STRING.fieldOf("backpack").forGetter(Unbaked::backpack)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            return new BackpackItemModel(BackpackVariant.of(backpack));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            // No parent models to resolve - the geometry is baked into the mod.
        }
    }
}
