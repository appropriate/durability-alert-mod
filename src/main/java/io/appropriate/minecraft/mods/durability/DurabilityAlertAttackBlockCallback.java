package io.appropriate.minecraft.mods.durability;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DurabilityAlertAttackBlockCallback implements AttackBlockCallback {
  private DurabilityChecker checker;

  public static DurabilityAlertAttackBlockCallback register(DurabilityAlertConfig config) {
    DurabilityAlertAttackBlockCallback callback = new DurabilityAlertAttackBlockCallback(config);
    AttackBlockCallback.EVENT.register(callback);
    return callback;
  }

  public DurabilityAlertAttackBlockCallback(DurabilityAlertConfig config) {
    this.checker = new DurabilityChecker(config);
  }

  public void updateConfig(DurabilityAlertConfig config) {
    this.checker = new DurabilityChecker(config);
  }

  public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos,
      Direction direction) {
    ItemStack stack = player.getMainHandStack();
    BlockState block = world.getBlockState(pos);

    if (player.canHarvest(block)) {
      DurabilityChecker.Result result = checker.checkItemStack(stack);

      if (result != null) {
        String translationKey = "messages.durability-alert-mod.alert";
        Text stackName = stack.getName();

        if (stack.hasCustomName()) {
          translationKey = "messages.durability-alert-mod.alert-named";
          if (stackName instanceof MutableText) {
            stackName = ((MutableText) stackName).formatted(Formatting.ITALIC);
          }
        }

        MutableText message = Text.translatable(
            translationKey, stackName, result.getRemainingDamagePercent());

        player.sendMessage(message.formatted(result.getDamageColor()), true);
      }
    }

    return ActionResult.PASS;
  }
}
