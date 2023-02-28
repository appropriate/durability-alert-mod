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

/**
 * A callback object to receive messages from the game when a block is attacked by a player.
 */
public class DurabilityAlertAttackBlockCallback implements AttackBlockCallback {
  private DurabilityChecker checker;

  /**
   * Registers a new callback with the given configuration.
   *
   * @param config the configuration to use for the new callback
   * @return a new callback
   */
  public static DurabilityAlertAttackBlockCallback register(DurabilityAlertConfig config) {
    DurabilityAlertAttackBlockCallback callback = new DurabilityAlertAttackBlockCallback(config);
    AttackBlockCallback.EVENT.register(callback);
    return callback;
  }

  /**
   * Builds a new callback instance with the given configuration.
   *
   * @param config the configuration to use for this callback
   */
  public DurabilityAlertAttackBlockCallback(DurabilityAlertConfig config) {
    this.checker = new DurabilityChecker(config);
  }

  /**
   * Updates the callback with a new configuration.
   *
   * <p>Whenever the configuration is updated, a new {@link DurabilityChecker} is constructed, which
   * will reset any tracking of previous alerts.
   *
   * @param config the updated configuration to use for this callback
   */
  public void updateConfig(DurabilityAlertConfig config) {
    this.checker = new DurabilityChecker(config);
  }

  /**
   * Called when a user attacks a block.
   *
   * @param player the player
   * @param world the world
   * @param hand which hand the player used to attack the block
   * @param pos the position of the block being attacked
   * @param direction the direction the player was facing while attacking the block
   * @return always returns {@code ActionResult.PASS} to indicate that the attack should be allowed
   */
  public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos,
      Direction direction) {
    ItemStack stack = player.getStackInHand(hand);
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
