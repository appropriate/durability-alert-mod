package io.appropriate.minecraft.mods.durability;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Formatting;

public class DurabilityChecker {
    private DurabilityAlertConfig config;
    private Result previous = null;

    public DurabilityChecker() {
        this(new DurabilityAlertConfig());
    }

    public DurabilityChecker(DurabilityAlertConfig config) {
        this.config = config;
    }

    public Result checkItemStack(ItemStack stack) {
        if (!isAlertable(stack)) {
            return null;
        }

        Result result = new Result(stack);

        if (result.alertCutoff == null || result.repeats(previous)) {
            return null;
        }

        previous = result;

        return result;
    }

    private Stream<Integer> alertCutoffs() {
        return config.alertCutoffs.stream();
    }

    private boolean isAlertable(ItemStack stack) {
        if (config.disabled) {
            return false;
        }

        if (!ToolItem.class.isInstance(stack.getItem())) {
            return false;
        }

        if (config.alertAllNamed && stack.hasCustomName()) {
            return true;
        }

        if (config.alertAllEnchanted && stack.hasEnchantments()) {
            return true;
        }

        ToolMaterial material = ToolItem.class.cast(stack.getItem()).getMaterial();
        if (!ToolMaterials.class.isInstance(material)) {
            return false;
        }

        return ((ToolMaterials)material).ordinal() >= config.minimumAlertTier.getMaterial().ordinal();
    }

    public class Result {
        private ItemStack stack;
        private int remainingDamagePercent;
        private Integer alertCutoff;

        private Result(ItemStack stack) {
            this.stack = stack;
            this.remainingDamagePercent = calculateRemainingDamagePercent();
            this.alertCutoff = findAlertCutoff();
        }

        private int calculateRemainingDamagePercent() {
            return (int)Math.round(
                100 * (stack.getMaxDamage() - stack.getDamage()) / (float)stack.getMaxDamage()
            );
        }

        private Integer findAlertCutoff() {
            return DurabilityChecker.this.alertCutoffs()
                .filter(cutoff -> cutoff >= remainingDamagePercent)
                .findFirst()
                .orElse(null);
        }

        public int getRemainingDamagePercent() {
            return remainingDamagePercent;
        }

        public Formatting getDamageColor() {
            if (remainingDamagePercent > 75) {
                return Formatting.GREEN;
            } else if (remainingDamagePercent > 40) {
                return Formatting.YELLOW;
            } else if (remainingDamagePercent > 15) {
                return Formatting.GOLD;
            } else {
                return Formatting.RED;
            }
        }

        public boolean repeats(Result otherResult) {
            return otherResult != null &&
                ItemStack.areItemsEqual(stack, otherResult.stack) &&
                Objects.equals(alertCutoff, otherResult.alertCutoff);
        }
    }
}
