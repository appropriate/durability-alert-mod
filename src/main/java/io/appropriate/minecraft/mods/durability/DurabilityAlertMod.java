package io.appropriate.minecraft.mods.durability;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;

public class DurabilityAlertMod implements ClientModInitializer {
  public void onInitializeClient() {
    final DurabilityChecker checker = new DurabilityChecker();

    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
      ItemStack stack = player.getMainHandStack();
      BlockState block = world.getBlockState(pos);

      if (player.isUsingEffectiveTool(block) && stack.isDamaged()) {
        DurabilityChecker.Result result = checker.checkItemStack(stack);

        if (result.shouldAlert()) {
          player.sendMessage(
            new TranslatableText(
              "durability-alert-mod.messages.alert",
              stack.getItem().getName(),
              result.getRemainingDamagePercent(),
              player.getBlockBreakingSpeed(block)
            ).formatted(result.getDamageColor()),
            true
          );
        }
      }

      return ActionResult.PASS;
    });
  }
}
