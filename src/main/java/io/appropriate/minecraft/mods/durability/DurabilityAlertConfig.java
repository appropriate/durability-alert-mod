package io.appropriate.minecraft.mods.durability;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ToolMaterials;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON;

@Config(name = "durability-alert-mod")
class DurabilityAlertConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    private static final List<Integer> DEFAULT_ALERT_CUTOFFS = Collections.unmodifiableList(Arrays.<Integer>asList(
        1, 2, 3, 4, 5, 10, 15, 20, 25, 50
    ));

    @ConfigEntry.Gui.Excluded
    private static final Material DEFAULT_MINIMUM_ALERT_TIER = Material.Diamond;

    @ConfigEntry.Gui.Tooltip
    List<Integer> alertCutoffs = DEFAULT_ALERT_CUTOFFS;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = BUTTON)
    Material minimumAlertTier = DEFAULT_MINIMUM_ALERT_TIER;

    @ConfigEntry.Gui.Tooltip
    boolean alertAllNamed = true;

    @ConfigEntry.Gui.Tooltip
    boolean alertAllEnchanted = true;

    @ConfigEntry.Gui.Tooltip
    boolean disabled = false;

    @Override
    public void validatePostLoad() throws ValidationException {
        ConfigData.super.validatePostLoad();

        if (alertCutoffs == null) {
            alertCutoffs = DEFAULT_ALERT_CUTOFFS;
        }

        alertCutoffs = alertCutoffs.stream()
                                   .filter(DurabilityAlertConfig::cutoffInBounds)
                                   .sorted()
                                   .distinct()
                                   .collect(Collectors.toList());

        if (minimumAlertTier == null) {
            minimumAlertTier = DEFAULT_MINIMUM_ALERT_TIER;
        }
    }

    private static boolean cutoffInBounds(Integer cutoff) {
        return cutoff != null && cutoff >= 0 && cutoff <= 100;
    }

    static enum Material {
        Wood(ToolMaterials.WOOD),
        Stone(ToolMaterials.STONE),
        Iron(ToolMaterials.IRON),
        Diamond(ToolMaterials.DIAMOND),
        Gold(ToolMaterials.GOLD),
        Netherite(ToolMaterials.NETHERITE);

        private ToolMaterials material;

        Material(ToolMaterials material) {
            this.material = material;
        }

        public ToolMaterials getMaterial() {
            return material;
        }
    }

}