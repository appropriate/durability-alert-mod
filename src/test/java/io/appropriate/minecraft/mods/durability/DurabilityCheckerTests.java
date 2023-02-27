package io.appropriate.minecraft.mods.durability;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Named.named;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import static net.minecraft.item.Items.DIAMOND_AXE;
import static net.minecraft.item.Items.DIAMOND_PICKAXE;
import static net.minecraft.item.Items.SHEARS;
import static net.minecraft.item.Items.WOODEN_SHOVEL;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import static net.minecraft.item.ToolMaterials.DIAMOND;
import static net.minecraft.item.ToolMaterials.GOLD;
import static net.minecraft.item.ToolMaterials.IRON;
import static net.minecraft.item.ToolMaterials.NETHERITE;
import static net.minecraft.item.ToolMaterials.STONE;
import static net.minecraft.item.ToolMaterials.WOOD;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import io.appropriate.minecraft.mods.durability.DurabilityChecker.Result;

class DurabilityCheckerTests {
    @BeforeAll
    static void initMinecraft() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    private static Arguments itemArguments(Item item) {
        return arguments(named(item.getName().getString(), item));
    }

    private static Stream<Arguments> toolsByMaterials(Set<ToolMaterial> materials) {
        return Registries.ITEM.stream()
            .filter(ToolItem.class::isInstance)
            .map(ToolItem.class::cast)
            .filter(item -> materials.contains(item.getMaterial()))
            .map(DurabilityCheckerTests::itemArguments);
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

    private static Stream<Arguments> returnsNullForFreshTools() {
        return Stream.concat(
            toolsByMaterials(Set.of(ToolMaterials.values())),
            Stream.of(itemArguments(SHEARS))
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

    private static Stream<Arguments> returnsNullForUnimportantMaterials() {
        return Stream.concat(
            toolsByMaterials(Set.of(IRON, STONE, WOOD)),
            Stream.of(itemArguments(SHEARS))
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

    private static Stream<Arguments> returnsResultForImportantMaterials() {
        return toolsByMaterials(Set.of(NETHERITE, DIAMOND, GOLD));
    }

    @DisplayName("Checking the same item twice doesn't alert unless new cutoff is reached")
    @Test
    void returnsNullCheckingSameItemWithLittleAdditionalDamage() {
        DurabilityChecker checker = new DurabilityChecker();

        // First check should be non-null
        ItemStack stack = new ItemStack(DIAMOND_PICKAXE);
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
        ItemStack stack = new ItemStack(DIAMOND_PICKAXE);
        stack.setDamage(stack.getMaxDamage() / 2 - 1);
        assertThat(checker.checkItemStack(stack)).isNotNull();

        // Second check should also be non-null
        ItemStack other = new ItemStack(DIAMOND_AXE);
        other.setDamage(other.getMaxDamage() / 2 - 1);
        assertThat(checker.checkItemStack(other)).isNotNull();
    }

    @DisplayName("Checking a named item with an unimportant material alerts")
    @Test
    void returnsResultCheckingNamedItemWithUnimportantMaterial() {
        DurabilityChecker checker = new DurabilityChecker();

        ItemStack stack = new ItemStack(WOODEN_SHOVEL);
        stack.setDamage(stack.getMaxDamage() - 1);
        stack.setCustomName(Text.of("Me dear old spade"));
        assertThat(checker.checkItemStack(stack)).isNotNull();
    }

    @DisplayName("Checking an enchanted item with an unimportant material alerts")
    @Test
    void returnsResultCheckingEnchantedItemWithUnimportantMaterial() {
        DurabilityChecker checker = new DurabilityChecker();

        ItemStack stack = new ItemStack(WOODEN_SHOVEL);
        stack.setDamage(stack.getMaxDamage() - 1);
        stack.addEnchantment(Enchantments.MENDING, 1);
        assertThat(checker.checkItemStack(stack)).isNotNull();
    }
}
