package io.appropriate.minecraft.mods.durability;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class DurabilityAlertMod implements ClientModInitializer {
  public void onInitializeClient() {
    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
      ItemStack stack = player.getMainHandStack();
      BlockState block = world.getBlockState(pos);

      if (player.isUsingEffectiveTool(block) && stack.isDamaged()) {
        float breakingSpeed = player.getBlockBreakingSpeed(block);
        int remainingDamagePercent = calculateRemainingDamagePercent(stack);

        player.sendMessage(
          new TranslatableText(
            "durability-alert-mod.messages.alert",
            stack.getItem().getName(), remainingDamagePercent, breakingSpeed
          ).formatted(damageColor(remainingDamagePercent)),
          true
        );
      }

      return ActionResult.PASS;
    });
  }

  private int calculateRemainingDamagePercent(ItemStack stack) {
    return (int)Math.round(
      100 * (stack.getMaxDamage() - stack.getDamage()) / (float)stack.getMaxDamage()
    );
  }

  private Formatting damageColor(int remainingDamagePercent) {
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
