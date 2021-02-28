package io.appropriate.minecraft.mods.durability;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class DurabilityAlertMod implements ClientModInitializer {
    public void onInitializeClient() {
        final DurabilityChecker checker = new DurabilityChecker();

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
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
        });
    }
}
