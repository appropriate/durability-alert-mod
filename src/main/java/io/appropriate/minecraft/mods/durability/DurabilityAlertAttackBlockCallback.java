package io.appropriate.minecraft.mods.durability;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

public class DurabilityAlertAttackBlockCallback implements AttackBlockCallback {
    private DurabilityChecker checker = new DurabilityChecker();

    public static void register() {
        AttackBlockCallback.EVENT.register(new DurabilityAlertAttackBlockCallback());
    }

    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        ItemStack stack = player.getMainHandStack();
        BlockState block = world.getBlockState(pos);

        if (player.isUsingEffectiveTool(block)) {
            DurabilityChecker.Result result = checker.checkItemStack(stack);

            if (result != null) {
                String translationKey = "messages.durability-alert-mod.alert";
                Text stackName = stack.getName();

                if (stack.hasCustomName()) {
                    translationKey = "messages.durability-alert-mod.alert-named";
                    if (stackName instanceof MutableText) {
                        stackName = ((MutableText)stackName).formatted(Formatting.ITALIC);
                    }
                }

                TranslatableText message = new TranslatableText(
                    translationKey, stackName, result.getRemainingDamagePercent());

                player.sendMessage(message.formatted(result.getDamageColor()), true);
            }
        }

        return ActionResult.PASS;
    }
}
