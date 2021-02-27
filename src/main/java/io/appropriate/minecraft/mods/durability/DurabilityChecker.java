package io.appropriate.minecraft.mods.durability;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Formatting;

public class DurabilityChecker {
    private static final int[] ALERT_CUTOFFS = new int[] {
      1, 2, 3, 4, 5, 10, 15, 20, 25, 50
    };

    private Result previous = null;

    public Result checkItemStack(ItemStack stack) {
        Result result = new Result(stack);

        if (result.alertCutoff == null || result.repeats(previous)) {
            return null;
        }

        previous = result;

        return result;
    }

    public static class Result {
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

        private Stream<Integer> alertCutoffs() {
          return Arrays.stream(ALERT_CUTOFFS).boxed();
        }

        private Integer findAlertCutoff() {
            return alertCutoffs()
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
                ItemStack.areItemsEqualIgnoreDamage(stack, otherResult.stack) &&
                Objects.equals(alertCutoff, otherResult.alertCutoff);
        }
    }
}
