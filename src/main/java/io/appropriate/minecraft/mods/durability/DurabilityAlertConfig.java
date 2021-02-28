package io.appropriate.minecraft.mods.durability;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ToolMaterials;

public class DurabilityAlertConfig {
    public static final List<Integer> DEFAULT_ALERT_CUTOFFS =
        Collections.unmodifiableList(Arrays.<Integer>asList(
            1, 2, 3, 4, 5, 10, 15, 20, 25, 50
        ));

    public static final Set<ToolMaterials> DEFAULT_ALERT_MATERIALS =
        Collections.unmodifiableSet(EnumSet.of(
            ToolMaterials.DIAMOND,
            ToolMaterials.GOLD,
            ToolMaterials.NETHERITE
        ));

    public static final boolean DEFAULT_ALERT_ALL_NAMED = true;

    public static final boolean DEFAULT_ALERT_ALL_ENCHANTED = true;

    private List<Integer> alertCutoffs = Collections.<Integer>emptyList();
    private Set<ToolMaterials> alertMaterials = Collections.<ToolMaterials>emptySet();
    private boolean alertAllNamed = true;
    private boolean alertAllEnchanted = true;
}
