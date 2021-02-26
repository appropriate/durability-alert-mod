package io.appropriate.minecraft.mods.durability;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;

public class DurabilityChecker {
  public Result checkItemStack(ItemStack stack) {
    Result result = new Result();
    result.remainingDamagePercent = calculateRemainingDamagePercent(stack);
    return result;
  }

  private int calculateRemainingDamagePercent(ItemStack stack) {
    return (int)Math.round(
      100 * (stack.getMaxDamage() - stack.getDamage()) / (float)stack.getMaxDamage()
    );
  }

  public static class Result {
    private int remainingDamagePercent;

    public int getRemainingDamagePercent() {
      return remainingDamagePercent;
    }

    public boolean shouldAlert() {
      return true;
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
  }
}
