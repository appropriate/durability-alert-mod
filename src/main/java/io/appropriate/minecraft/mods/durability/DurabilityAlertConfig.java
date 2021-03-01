package io.appropriate.minecraft.mods.durability;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ToolMaterials;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "durability-alert-mod")
class DurabilityAlertConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    static List<Integer> alertCutoffs = Arrays.<Integer>asList(
        1, 2, 3, 4, 5, 10, 15, 20, 25, 50
    );

    @ConfigEntry.Gui.Tooltip
    static ToolMaterials minimumAlertTier = ToolMaterials.DIAMOND;

    @ConfigEntry.Gui.Tooltip
    static boolean alertAllNamed = true;

    @ConfigEntry.Gui.Tooltip
    static boolean alertAllEnchanted = true;

    @ConfigEntry.Gui.Tooltip
    static boolean disabled = false;
}
