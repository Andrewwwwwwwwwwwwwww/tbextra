package io.github.andrewwwwwwwwwwwwwww.tbextra;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class TbExtraComponents {
    /**
     * Which skin a backpack is wearing. Present only on reskinned packs; everything else
     * about the stack is left exactly as Traveler's Backpack made it.
     */
    public static DataComponentType<String> SKIN;

    private TbExtraComponents() {
    }

    public static void init() {
        SKIN = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, TbExtra.id("skin"),
                DataComponentType.<String>builder()
                        .persistent(Codec.STRING)
                        .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                        .build());
    }
}
