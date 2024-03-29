package io.appropriate.minecraft.mods.durability;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static net.minecraft.enchantment.Enchantments.MENDING;
import static net.minecraft.item.Items.BEEF;
import static net.minecraft.item.Items.DIAMOND_AXE;
import static net.minecraft.item.Items.DIAMOND_PICKAXE;
import static net.minecraft.item.Items.SHEARS;
import static net.minecraft.item.Items.WOODEN_SHOVEL;
import static net.minecraft.item.ToolMaterials.DIAMOND;
import static net.minecraft.item.ToolMaterials.GOLD;
import static net.minecraft.item.ToolMaterials.IRON;
import static net.minecraft.item.ToolMaterials.NETHERITE;
import static net.minecraft.item.ToolMaterials.STONE;
import static net.minecraft.item.ToolMaterials.WOOD;
import static net.minecraft.text.Text.literal;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DurabilityCheckerTests {
  @BeforeAll
  static void initMinecraft() {
    SharedConstants.createGameVersion();
    Bootstrap.initialize();
  }

  @DisplayName("Returns null when config is disabled")
  @Test
  void returnsNullWhenDisabled() {
    var config = new DurabilityAlertConfig();
    config.disabled = true;
    var checker = new DurabilityChecker(config);

    var stack = new ItemStack(DIAMOND_PICKAXE);
    stack.setDamage(stack.getMaxDamage() - 1);
    assertThat(checker.checkItemStack(stack)).isEmpty();
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
    var checker = new DurabilityChecker();
    var stack = new ItemStack(item);
    var result = checker.checkItemStack(stack);
    assertThat(result).isEmpty();
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
    var checker = new DurabilityChecker();
    var stack = new ItemStack(item);
    stack.setDamage(stack.getMaxDamage() - 1);
    var result = checker.checkItemStack(stack);
    assertThat(result).isEmpty();
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
    var checker = new DurabilityChecker();
    var stack = new ItemStack(item);
    stack.setDamage(stack.getMaxDamage() - 1);
    var result = checker.checkItemStack(stack);
    assertThat(result).isPresent();
  }

  private static Stream<Arguments> returnsResultForImportantMaterials() {
    return toolsByMaterials(Set.of(NETHERITE, DIAMOND, GOLD));
  }

  @DisplayName("Checking the same item twice doesn't alert unless new cutoff is reached")
  @Test
  void returnsNullForSameItemWithLittleAdditionalDamage() {
    var checker = new DurabilityChecker();

    // First check should be non-null
    var stack = new ItemStack(DIAMOND_PICKAXE);
    stack.setDamage(stack.getMaxDamage() / 2 - 1);
    assertThat(checker.checkItemStack(stack)).isPresent();

    // Second check should be null; perform on a copy since we expect a
    // distinct ItemStack each time we query the main hand inventory
    var copy = stack.copy();
    copy.setDamage(copy.getMaxDamage() / 2 - 2);
    assertThat(checker.checkItemStack(copy)).isEmpty();

    // Third check should be non-null
    var copy2 = stack.copy();
    copy2.setDamage(copy2.getMaxDamage() - 1);
    assertThat(checker.checkItemStack(copy2)).isPresent();
  }

  @DisplayName("Checking a different item with the same damage alerts")
  @Test
  void returnsResultForDifferentItemWithSameDamage() {
    var checker = new DurabilityChecker();

    // First check should be non-null
    var stack = new ItemStack(DIAMOND_PICKAXE);
    stack.setDamage(stack.getMaxDamage() / 2 - 1);
    assertThat(checker.checkItemStack(stack)).isPresent();

    // Second check should also be non-null
    var other = new ItemStack(DIAMOND_AXE);
    other.setDamage(other.getMaxDamage() / 2 - 1);
    assertThat(checker.checkItemStack(other)).isPresent();
  }

  @DisplayName("Checking a named low-tier item alerts")
  @Test
  void returnsResultForNamedLowTierItem() {
    var checker = new DurabilityChecker();

    var stack = new ItemStack(WOODEN_SHOVEL);
    stack.setDamage(stack.getMaxDamage() - 1);
    stack.setCustomName(literal("Me dear old spade"));
    assertThat(checker.checkItemStack(stack)).isPresent();
  }

  @DisplayName("Checking a named low-tier item does not alert if alertAllNamed is false")
  @Test
  void returnsNullForNamedLowTierItemWithoutAlertAllNamed() {
    var config = new DurabilityAlertConfig();
    config.alertAllNamed = false;
    var checker = new DurabilityChecker(config);

    var stack = new ItemStack(WOODEN_SHOVEL);
    stack.setDamage(stack.getMaxDamage() - 1);
    stack.setCustomName(literal("Me dear old spade"));
    assertThat(checker.checkItemStack(stack)).isEmpty();
  }

  @DisplayName("Checking an enchanted low-tier item alerts")
  @Test
  void returnsResultForEnchantedLowTierItem() {
    var checker = new DurabilityChecker();

    var stack = new ItemStack(WOODEN_SHOVEL);
    stack.setDamage(stack.getMaxDamage() - 1);
    stack.addEnchantment(MENDING, 1);
    assertThat(checker.checkItemStack(stack)).isPresent();
  }

  @DisplayName("Checking an enchanted low-tier item does not alert if alertAllEnchanted is false")
  @Test
  void returnsNullForEnchantedLowTierItemWithoutAlertAllEnchanted() {
    var config = new DurabilityAlertConfig();
    config.alertAllEnchanted = false;
    var checker = new DurabilityChecker(config);

    var stack = new ItemStack(WOODEN_SHOVEL);
    stack.setDamage(stack.getMaxDamage() - 1);
    stack.addEnchantment(MENDING, 1);
    assertThat(checker.checkItemStack(stack)).isEmpty();
  }

  @DisplayName("Checking an item with an exotic material does not alert")
  @Test
  void returnsNullForExoticMaterial() {
    var checker = new DurabilityChecker();

    var beefShovel = new ShovelItem(new Beef(), 0.0f, 0.0f, new Item.Settings());
    var stack = new ItemStack(beefShovel);
    assertThat(checker.checkItemStack(stack)).isEmpty();
  }

  static class Beef implements ToolMaterial {
    public float getAttackDamage() {
      return 0.0f;
    }

    public int getDurability() {
      return 0;
    }

    public int getEnchantability() {
      return 0;
    }

    public int getMiningLevel() {
      return 0;
    }

    public float getMiningSpeedMultiplier() {
      return 0.0f;
    }

    public Ingredient getRepairIngredient() {
      return Ingredient.ofItems(BEEF);
    }
  }

  @DisplayName("Correctly calculates remaining damage")
  @ParameterizedTest
  @MethodSource
  void correctlyCalculatesRemainingDamage(int percent) {
    var config = new DurabilityAlertConfig();
    config.alertCutoffs = List.of(100);
    var checker = new DurabilityChecker(config);

    var stack = new ItemStack(DIAMOND_PICKAXE);
    stack.setDamage((100 - percent) * stack.getMaxDamage() / 100);

    var result = checker.checkItemStack(stack);
    assertThat(result).isPresent();
    assertThat(result.get().getRemainingDamagePercent()).isEqualTo(percent);
  }

  static IntFunction<Arguments> PERCENT_ARGUMENTS =
      n -> arguments(named(String.format("%d%%", n), n));

  static Stream<Arguments> correctlyCalculatesRemainingDamage() {
    // Return 0-100 by 10s
    return IntStream.rangeClosed(0, 10).map(n -> n * 10).mapToObj(PERCENT_ARGUMENTS);
  }

  @DisplayName("Correctly calculates damage color")
  @ParameterizedTest
  @MethodSource
  void correctlyCalculatesDamageColor(int percent) {
    var config = new DurabilityAlertConfig();
    config.alertCutoffs = List.of(100);
    var checker = new DurabilityChecker(config);

    var stack = new ItemStack(DIAMOND_PICKAXE);
    stack.setDamage((100 - percent) * stack.getMaxDamage() / 100);

    var result = checker.checkItemStack(stack);
    assertThat(result).isPresent();
    assertThat(result.get().getDamageMessageColor().getRgb()).isEqualTo(stack.getItemBarColor());
  }

  @SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
  static Stream<Arguments> correctlyCalculatesDamageColor() {
    return new Random().ints(20, 0, 100).mapToObj(PERCENT_ARGUMENTS);
  }
}
