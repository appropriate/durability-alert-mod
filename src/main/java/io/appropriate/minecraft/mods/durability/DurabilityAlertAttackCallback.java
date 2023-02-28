package io.appropriate.minecraft.mods.durability;

import static net.minecraft.util.hit.HitResult.Type.MISS;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * A callback object to receive messages from the game when a block is attacked by a player.
 */
public class DurabilityAlertAttackCallback implements AttackBlockCallback, AttackEntityCallback {
  private DurabilityChecker checker;

  /**
   * Registers a new callback with the given configuration.
   *
   * @param config the configuration to use for the new callback
   * @return a new callback
   */
  public static DurabilityAlertAttackCallback register(DurabilityAlertConfig config) {
    var callback = new DurabilityAlertAttackCallback(config);
    AttackBlockCallback.EVENT.register(callback);
    AttackEntityCallback.EVENT.register(callback);
    return callback;
  }

  /**
   * Builds a new callback instance with the given configuration.
   *
   * @param config the configuration to use for this callback
   */
  public DurabilityAlertAttackCallback(DurabilityAlertConfig config) {
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
    if (player.canHarvest(world.getBlockState(pos))) {
      checkStackInHand(player, hand);
    }

    return ActionResult.PASS;
  }

  /**
   * Called when a user attacks an entity.
   *
   * @param player the player
   * @param world the world
   * @param hand which hand the player used to attack the entity
   * @param entity the entity being attacked
   * @param hitResult the result of the hit
   * @return always returns {@code ActionResult.PASS} to indicate that the attack should be allowed
   */
  public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity,
      EntityHitResult hitResult) {
    if (hitResult == null || hitResult.getType() != MISS) {
      checkStackInHand(player, hand);
    }

    return ActionResult.PASS;
  }

  private void checkStackInHand(PlayerEntity player, Hand hand) {
    if (player.isSpectator()) {
      return;
    }

    var stack = player.getStackInHand(hand);
    if (stack == null) {
      return;
    }

    var result = checker.checkItemStack(stack);
    if (result == null) {
      return;
    }

    var translationKey = "messages.durability-alert-mod.alert";
    var stackName = stack.getName();

    if (stack.hasCustomName()) {
      translationKey = "messages.durability-alert-mod.alert-named";
      if (stackName instanceof MutableText) {
        stackName = ((MutableText) stackName).formatted(Formatting.ITALIC);
      }
    }

    var message = Text.translatable(
        translationKey, stackName, result.getRemainingDamagePercent());

    player.sendMessage(message.formatted(result.getDamageColor()), true);
  }
}
