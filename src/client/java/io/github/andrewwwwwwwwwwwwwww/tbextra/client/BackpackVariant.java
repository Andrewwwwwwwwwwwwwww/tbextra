package io.github.andrewwwwwwwwwwwwwww.tbextra.client;

import io.github.andrewwwwwwwwwwwwwww.tbextra.TbExtra;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/** Pairs each backpack's baked geometry with its texture, loaded once on first use. */
public final class BackpackVariant {
    private static final Map<String, BackpackVariant> CACHE = new HashMap<>();

    private final BackpackGeometry geometry;
    private final Identifier texture;

    private BackpackVariant(String name) {
        this.geometry = BackpackGeometry.load(name);
        this.texture = TbExtra.id("textures/entity/" + name + ".png");
    }

    public static synchronized BackpackVariant of(String name) {
        return CACHE.computeIfAbsent(name, BackpackVariant::new);
    }

    public BackpackGeometry geometry() {
        return geometry;
    }

    public Identifier texture() {
        return texture;
    }
}
