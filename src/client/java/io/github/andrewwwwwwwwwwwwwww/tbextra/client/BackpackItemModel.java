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
    private final BackpackSpecialRenderer renderer;

    public BackpackItemModel(BackpackVariant variant) {
        this.renderer = new BackpackSpecialRenderer(variant);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        layer.setItemTransform(transformFor(context));
        layer.setupSpecialModel(renderer, null);
    }

    /**
     * The geometry sits on y=0 centred on X/Z, so each context lifts and scales it into place.
     * Nudge these if a pack sits oddly in a particular view.
     */
    private static ItemTransform transformFor(ItemDisplayContext context) {
        return switch (context) {
            case GUI -> transform(30.0F, 225.0F, 0.0F, 0.0F, -0.15F, 0.0F, 1.0F);
            case GROUND -> transform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F);
            case FIXED -> transform(0.0F, 180.0F, 0.0F, 0.0F, -0.15F, 0.0F, 1.0F);
            case HEAD -> transform(0.0F, 180.0F, 0.0F, 0.0F, 0.3F, 0.0F, 1.2F);
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND ->
                    transform(0.0F, 0.0F, 0.0F, 0.0F, 0.1F, 0.0F, 0.7F);
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND ->
                    transform(0.0F, 135.0F, 0.0F, 0.0F, 0.1F, 0.0F, 0.7F);
            default -> transform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
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
