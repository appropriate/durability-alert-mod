package io.appropriate.minecraft.mods.durability;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Formatting;

/**
 * A {@code DurabilityChecker} checks an {@link ItemStack} for durability changes according to a
 * {@link DurabilityAlertConfig}.
 *
 * <p>The {@code DurabilityChecker} stores the result of its most recent check to allow it to avoid
 * returning repeated results.
 */
public class DurabilityChecker {
  private DurabilityAlertConfig config;
  private Result previous = null;

  /**
   * Construct a {@code DurabilityChecker} with the default {@code DurabilityConfig}.
   */
  public DurabilityChecker() {
    this(new DurabilityAlertConfig());
  }

  /**
   * Construct a {@code DurabilityChecker} with the given {@code DurabilityConfig}.
   *
   * @param config the configuration to use for determining whether to return a {@code Result}
   */
  public DurabilityChecker(DurabilityAlertConfig config) {
    this.config = requireNonNull(config);
  }

  /**
   * Checks if the given {@code ItemStack} should trigger an alert based on the {@code
   * DurabilityAlertConfig}.
   *
   * @param stack the item stack to check
   * @return a {@code Result} if an alert should be shown or {@code null} otherwise
   */
  public Optional<Result> checkItemStack(ItemStack stack) {
    if (!isAlertable(stack)) {
      return Optional.empty();
    }

    var result = new Result(stack);

    if (result.alertCutoff == null || result.repeats(previous)) {
      return Optional.empty();
    }

    previous = result;

    return Optional.of(result);
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

    var material = ToolItem.class.cast(stack.getItem()).getMaterial();
    if (!ToolMaterials.class.isInstance(material)) {
      return false;
    }

    return ((ToolMaterials) material).ordinal() >= config.minimumAlertTier.getMaterial().ordinal();
  }

  /**
   * A {@code Result} represents the damage percentage for an {@code ItemStack}, the formatting
   * color to be used for that damage level, and alert cutoff that was determined based on the
   * damage level.
   */
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
      return (int) Math.round(
        100 * (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage()
      );
    }

    private Integer findAlertCutoff() {
      return DurabilityChecker.this.alertCutoffs()
        .filter(cutoff -> cutoff >= remainingDamagePercent)
        .findFirst()
        .orElse(null);
    }

    /**
     * The remaining damage percentage for the {@code ItemStack} being checked.
     *
     * @return the damage percentage as an integer from 0 to 100
     */
    public int getRemainingDamagePercent() {
      return remainingDamagePercent;
    }

    /**
     * The formatting color to be used if a message is shown to the user based on this
     * {@code Result}.
     *
     * @return the color to be used for any displayed message
     */
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

    /**
     * Compares another {@code Result} to this one and checks if it is for the same
     * {@code ItemStack} and alert cutoff.
     *
     * @param otherResult the {@code Result} to compare against
     * @return {@code true} if the other {@code Result} has the same {@code ItemStack} and alert
     *     cutoff
     */
    public boolean repeats(Result otherResult) {
      return otherResult != null
        && ItemStack.areItemsEqual(stack, otherResult.stack)
        && Objects.equals(alertCutoff, otherResult.alertCutoff);
    }
  }
}
