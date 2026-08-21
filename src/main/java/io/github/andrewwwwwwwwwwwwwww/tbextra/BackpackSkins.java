package io.github.andrewwwwwwwwwwwwwww.tbextra;

import java.util.List;

/**
 * The reskins this mod adds. A skin only changes a backpack's name and model - the item
 * itself stays whichever Traveler's Backpack it was crafted from, so its ability, tier,
 * upgrades and contents are untouched.
 */
public final class BackpackSkins {
    public static final String FIREWATCH = "firewatch";
    public static final String TRAPPER = "trapper";

    public static final List<String> ALL = List.of(FIREWATCH, TRAPPER);

    private BackpackSkins() {
    }

    public static boolean isKnown(String skin) {
        return ALL.contains(skin);
    }

    /** Translation key used for the renamed backpack, e.g. "item.tbextra.firewatch". */
    public static String nameKey(String skin) {
        return "item." + TbExtra.MODID + "." + skin;
    }
}
