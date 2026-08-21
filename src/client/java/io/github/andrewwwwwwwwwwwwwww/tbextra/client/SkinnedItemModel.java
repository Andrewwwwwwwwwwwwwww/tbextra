package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import io.github.andrewwwwwwwwwwwwwww.tbextra.BackpackSkins;
import io.github.andrewwwwwwwwwwwwwww.tbextra.TbExtraComponents;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Wraps Traveler's Backpack's item model. A pack carrying one of our skins is drawn with
 * that skin's model; anything else falls straight through to TB's own rendering.
 */
public class SkinnedItemModel extends WrapperBakedItemModel {
    public SkinnedItemModel(ItemModel wrapped) {
        super(wrapped);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        String skin = stack.get(TbExtraComponents.SKIN);
        if (skin == null || !BackpackSkins.isKnown(skin)) {
            super.update(state, stack, resolver, context, level, owner, seed);
            return;
        }

        // The render state is cached by model identity; without this every skin would share
        // one entry and the first one drawn would stand in for the rest.
        state.appendModelIdentityElement(skin);

        SkinPresentation presentation = SkinPresentation.of(skin);
        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        layer.setUsesBlockLight(true);
        // NONE maps to NO_TRANSFORM, which is what TB uses for the worn pack - it does the
        // positioning on the player's back itself.
        layer.setItemTransform(presentation.transforms().getTransform(context));
        layer.setupSpecialModel(presentation.itemRenderer(), null);
    }
}
