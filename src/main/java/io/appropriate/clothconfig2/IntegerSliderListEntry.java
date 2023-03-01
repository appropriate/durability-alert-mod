package io.appropriate.minecraft.clothconfig2;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

/**
 * A config entry list consisting of bounded {@link Integer} values that use one
 * {@link IntegerSliderListCell} per entry.
 */
@Environment(EnvType.CLIENT)
public class IntegerSliderListEntry extends AbstractSliderListEntry<
    Integer, IntegerSliderListEntry.IntegerSliderListCell, IntegerSliderListEntry> {
  private static final Function<Integer, Text> DEFAULT_TEXT_GETTER =
      value -> Text.literal(String.format("Value: %d", value));

  /**
   * Builds a new list entry for a given field, containing cells for each child entry.
   *
   * @param fieldName the name of the field
   * @param minimum the minimum value that can be selected
   * @param maximum the maximum value that can be selected
   * @param value a list of values for the field
   * @param defaultExpanded whether or not the list should be expanded by default
   * @param tooltipSupplier a {@code Supplier} that returns an array of tooltip text
   * @param saveConsumer a {@code Consumer} to receive the list of values for this field on save
   * @param defaultValue a list of default values of {@code value} is {@code null}
   * @param cellDefaultValue the default value for new cells
   * @param resetButtonKey the i13n key to use for the "Reset" button next to each cell
   * @param requiresRestart whether or not changing the field values requires restarting the game
   * @param deleteButtonEnabled whether a button should be shown to remove cells
   * @param insertInFront whether to insert new cells in front of existing cells ({@code true}) or
   *     after
   */
  public IntegerSliderListEntry(Text fieldName, int minimum, int maximum, List<Integer> value,
      boolean defaultExpanded, Supplier<Optional<Text[]>> tooltipSupplier,
      Consumer<List<Integer>> saveConsumer, Supplier<List<Integer>> defaultValue,
      int cellDefaultValue, Text resetButtonKey, boolean requiresRestart,
      boolean deleteButtonEnabled, boolean insertInFront) {
    super(fieldName, minimum, maximum, value, defaultExpanded, tooltipSupplier, saveConsumer,
        defaultValue, cellDefaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled,
        insertInFront, IntegerSliderListCell::new);
    setTextGetter(DEFAULT_TEXT_GETTER);
    cells.forEach(IntegerSliderListCell::syncValueToSlider);
  }

  @Override
  public IntegerSliderListEntry self() {
    return this;
  }

  /**
   * A config entry within a parent {@link IntegerSliderListEntry} containing a single bounded
   * {@link Integer} with an {@link net.minecraft.client.gui.widget.SliderWidget} for user display
   * and input.
   */
  public static class IntegerSliderListCell extends AbstractSliderListEntry.AbstractSliderListCell<
      Integer, IntegerSliderListCell, IntegerSliderListEntry> {
    private final AtomicInteger value;

    /**
     * Creates a new slider cell with the given initial value and parent list entry.
     *
     * <p>If the initial value is {@code null}, the cell uses the default cell value from the list
     * entry
     *
     * @param value the initial value
     * @param listListEntry the parent list entry
     */
    public IntegerSliderListCell(Integer value, IntegerSliderListEntry listListEntry) {
      super(value, listListEntry);

      this.value = new AtomicInteger(value == null ? listListEntry.cellDefaultValue : value);
    }

    @Override
    public Integer getValue() {
      return value.get();
    }

    @Override
    protected double getValueForSlider() {
      return ((double) this.value.get() - listListEntry.minimum)
          / Math.abs(listListEntry.maximum - listListEntry.minimum);
    }

    @Override
    protected void setValueFromSlider(double value) {
      this.value.set(
          (int) (listListEntry.minimum
              + Math.abs(listListEntry.maximum - listListEntry.minimum) * value)
      );
    }
  }
}
