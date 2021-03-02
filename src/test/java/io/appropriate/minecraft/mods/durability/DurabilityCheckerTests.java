package io.appropriate.minecraft.mods.durability;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.minecraft.Bootstrap;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;

import io.appropriate.minecraft.mods.durability.DurabilityChecker.Result;

class DurabilityCheckerTests {
    @BeforeAll
    static void initMinecraft() {
        Bootstrap.initialize();
    }

    @DisplayName("Checking a fresh tool returns null")
    @ParameterizedTest
    @MethodSource
    void returnsNullForFreshTools(Item item) {
        DurabilityChecker checker = new DurabilityChecker();
        ItemStack stack = new ItemStack(item);
        Result result = checker.checkItemStack(stack);
        assertThat(result).isNull();
    }

    private static Stream<Item> returnsNullForFreshTools() {
        return Stream.of(
            Items.DIAMOND_AXE,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_SHOVEL,
            Items.DIAMOND_SWORD,
            Items.GOLDEN_AXE,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_SWORD,
            Items.IRON_AXE,
            Items.IRON_PICKAXE,
            Items.IRON_SHOVEL,
            Items.IRON_SWORD,
            Items.NETHERITE_AXE,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_SHOVEL,
            Items.NETHERITE_SWORD,
            Items.SHEARS,
            Items.STONE_AXE,
            Items.STONE_PICKAXE,
            Items.STONE_SHOVEL,
            Items.STONE_SWORD,
            Items.WOODEN_AXE,
            Items.WOODEN_PICKAXE,
            Items.WOODEN_SHOVEL,
            Items.WOODEN_SWORD
        );
    }

    @DisplayName("Checking a damaged wooden or iron tool returns null")
    @ParameterizedTest
    @MethodSource
    void returnsNullForUnimportantMaterials(Item item) {
        DurabilityChecker checker = new DurabilityChecker();
        ItemStack stack = new ItemStack(item);
        stack.setDamage(stack.getMaxDamage() - 1);
        Result result = checker.checkItemStack(stack);
        assertThat(result).isNull();
    }

    private static Stream<Item> returnsNullForUnimportantMaterials() {
        return Stream.of(
            Items.IRON_AXE,
            Items.IRON_PICKAXE,
            Items.IRON_SHOVEL,
            Items.IRON_SWORD,
            Items.SHEARS,
            Items.STONE_AXE,
            Items.STONE_PICKAXE,
            Items.STONE_SHOVEL,
            Items.STONE_SWORD,
            Items.WOODEN_AXE,
            Items.WOODEN_PICKAXE,
            Items.WOODEN_SHOVEL,
            Items.WOODEN_SWORD
        );
    }

    @DisplayName("Checking a damaged gold, diamond, or netherite tool returns a result")
    @ParameterizedTest
    @MethodSource
    void returnsResultForImportantMaterials(Item item) {
        DurabilityChecker checker = new DurabilityChecker();
        ItemStack stack = new ItemStack(item);
        stack.setDamage(stack.getMaxDamage() - 1);
        Result result = checker.checkItemStack(stack);
        assertThat(result).isNotNull();
    }

    private static Stream<Item> returnsResultForImportantMaterials() {
        return Stream.of(
            Items.DIAMOND_AXE,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_SHOVEL,
            Items.DIAMOND_SWORD,
            Items.GOLDEN_AXE,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_SWORD,
            Items.NETHERITE_AXE,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_SHOVEL,
            Items.NETHERITE_SWORD
        );
    }

    @DisplayName("Checking the same item twice doesn't alert unless new cutoff is reached")
    @Test
    void returnsNullCheckingSameItemWithLittleAdditionalDamage() {
        DurabilityChecker checker = new DurabilityChecker();

        // First check should be non-null
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.setDamage(stack.getMaxDamage() / 2 - 1);
        assertThat(checker.checkItemStack(stack)).isNotNull();

        // Second check should be null; perform on a copy since we expect a
        // distinct ItemStack each time we query the main hand inventory
        ItemStack copy = stack.copy();
        copy.setDamage(copy.getMaxDamage() / 2 - 2);
        assertThat(checker.checkItemStack(copy)).isNull();
    }

    @DisplayName("Checking a different item with the same damage alerts")
    @Test
    void returnsResultCheckingDifferentItemWithSameDamage() {
        DurabilityChecker checker = new DurabilityChecker();

        // First check should be non-null
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.setDamage(stack.getMaxDamage() / 2 - 1);
        assertThat(checker.checkItemStack(stack)).isNotNull();

        // Second check should also be non-null
        ItemStack other = new ItemStack(Items.DIAMOND_AXE);
        other.setDamage(other.getMaxDamage() / 2 - 1);
        assertThat(checker.checkItemStack(other)).isNotNull();
    }

    @DisplayName("Checking a named item with an unimportant material alerts")
    @Test
    void returnsResultCheckingNamedItemWithUnimportantMaterial() {
        DurabilityChecker checker = new DurabilityChecker();

        ItemStack stack = new ItemStack(Items.WOODEN_SHOVEL);
        stack.setDamage(stack.getMaxDamage() - 1);
        stack.setCustomName(new LiteralText("Me dear old spade"));
        assertThat(checker.checkItemStack(stack)).isNotNull();
    }

    @DisplayName("Checking an enchanted item with an unimportant material alerts")
    @Test
    void returnsResultCheckingEnchantedItemWithUnimportantMaterial() {
        DurabilityChecker checker = new DurabilityChecker();

        ItemStack stack = new ItemStack(Items.WOODEN_SHOVEL);
        stack.setDamage(stack.getMaxDamage() - 1);
        stack.addEnchantment(Enchantments.MENDING, 1);
        assertThat(checker.checkItemStack(stack)).isNotNull();
    }
}
