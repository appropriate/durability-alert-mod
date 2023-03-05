package io.appropriate.minecraft.clothconfig2;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * A config entry list consisting of bounded {@link Long} values that use one
 * {@link LongSliderListCell} per entry.
 */
@ClientOnly
public class LongSliderListEntry extends AbstractSliderListEntry<
    Long, LongSliderListEntry.LongSliderListCell, LongSliderListEntry> {
  private static final Function<Long, Text> DEFAULT_TEXT_GETTER =
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
  public LongSliderListEntry(Text fieldName, long minimum, long maximum, List<Long> value,
      boolean defaultExpanded, Supplier<Optional<Text[]>> tooltipSupplier,
      Consumer<List<Long>> saveConsumer, Supplier<List<Long>> defaultValue, long cellDefaultValue,
      Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled,
      boolean insertInFront) {
    super(fieldName, minimum, maximum, value, defaultExpanded, tooltipSupplier, saveConsumer,
        defaultValue, cellDefaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled,
        insertInFront, LongSliderListCell::new);
    setTextGetter(DEFAULT_TEXT_GETTER);
    cells.forEach(LongSliderListCell::syncValueToSlider);
  }

  @Override
  public LongSliderListEntry self() {
    return this;
  }

  /**
   * A config entry within a parent {@link LongSliderListEntry} containing a single bounded
   * {@link Long} with an {@link net.minecraft.client.gui.widget.SliderWidget} for user display and
   * input.
   */
  public static class LongSliderListCell extends AbstractSliderListEntry.AbstractSliderListCell<
      Long, LongSliderListCell, LongSliderListEntry> {
    private final AtomicLong value;

    /**
     * Creates a new slider cell with the given initial value and parent list entry.
     *
     * <p>If the initial value is {@code null}, the cell uses the default cell value from the list
     * entry
     *
     * @param value the initial value
     * @param listListEntry the parent list entry
     */
    public LongSliderListCell(Long value, LongSliderListEntry listListEntry) {
      super(value, listListEntry);

      this.value = new AtomicLong(value == null ? listListEntry.cellDefaultValue : value);
    }

    /**
     * The current value of the slider.
     *
     * @return the current value
     */
    @Override
    public Long getValue() {
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
          (long) (listListEntry.minimum
              + Math.abs(listListEntry.maximum - listListEntry.minimum) * value)
      );
    }
  }
}
