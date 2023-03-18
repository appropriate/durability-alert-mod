package io.appropriate.minecraft.clothconfig2;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.ChatNarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * A config entry list consisting of bounded values that use one {@link AbstractSliderListCell} per
 * entry.
 *
 * <p>Any bounded values that can be respresented as a {@code double} can be listed using this entry
 * list by implementing a specialized subclass of {@link AbstractSliderListCell}.
 *
 * @param <T>     the configuration object type
 * @param <C>     the cell type
 * @param <SelfT> the "curiously recurring template pattern" type parameter
 * @see AbstractListListEntry
 */
@ClientOnly
public abstract class AbstractSliderListEntry<
    T, C extends AbstractSliderListEntry.AbstractSliderListCell<T, C, SelfT>,
    SelfT extends AbstractSliderListEntry<T, C, SelfT>> extends AbstractListListEntry<T, C, SelfT> {
  /**
   * The minimum value allowed by this list entry.
   */
  protected final T minimum;

  /**
   * The maximum value allowed by this list entry.
   */
  protected final T maximum;

  /**
   * The default value for new cells.
   */
  protected final T cellDefaultValue;

  /**
   * A {@code Function} that returns a textual representation of a value.
   */
  protected Function<T, Text> textGetter;

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
   * @param createNewCell a {@code BiFunction} that create a new cell, given a value and the list
   *     entry
   */
  public AbstractSliderListEntry(Text fieldName, T minimum, T maximum, List<T> value,
      boolean defaultExpanded, Supplier<Optional<Text[]>> tooltipSupplier,
      Consumer<List<T>> saveConsumer, Supplier<List<T>> defaultValue, T cellDefaultValue,
      Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled,
      boolean insertInFront, BiFunction<T, SelfT, C> createNewCell) {
    super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue,
        resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, createNewCell);

    this.minimum = requireNonNull(minimum);
    this.maximum = requireNonNull(maximum);
    this.cellDefaultValue = requireNonNull(cellDefaultValue);
  }

  /**
   * Sets the function that returns a textual representation of a value.
   *
   * <p>This function is expected to be called only once at initialization. When called, it updates
   * the displayed message for all child cells using this function.
   *
   * @param textGetter the {@code Function} that returns a textual representation of a value; if
   *     {@code null}, values will have a blank textual representation
   * @return this list entry to allow method chaining
   */
  public AbstractSliderListEntry setTextGetter(Function<T, Text> textGetter) {
    this.textGetter = textGetter;
    this.cells.forEach(c -> c.sliderWidget.updateMessage());
    return this;
  }

  /**
   * A config entry within a parent {@link AbstractSliderListEntry} containing a single bounded
   * value with a {@link Slider} widget for user display and input.
   *
   * <p>Any bounded value that can be respresented as a {@code double} can be listed by subclassing
   * this class and its parent {@link AbstractSliderListEntry}, implementing the
   * {@link #getValueForSlider()} and {@link #setValueFromSlider(double)} methods.
   *
   * @param <T>          the configuration object type
   * @param <SelfT>      the "curiously recurring template pattern" type parameter for this class
   * @param <OuterSelfT> the "curiously recurring template pattern" type parameter for the outer
   *     class
   * @see AbstractSliderListEntry
   */
  public abstract static class AbstractSliderListCell<
      T, SelfT extends AbstractSliderListEntry.AbstractSliderListCell<T, SelfT, OuterSelfT>,
      OuterSelfT extends AbstractSliderListEntry<T, SelfT, OuterSelfT>>
      extends AbstractListListEntry.AbstractListCell<T, SelfT, OuterSelfT> {
    /**
     * The {@code Slider} widget that display the value of this cell and allows it to be updated.
     */
    protected final Slider sliderWidget;

    private boolean isSelected;
    private boolean isHovered;

    /**
     * Creates a new slider cell with the given initial value and parent list entry.
     *
     * @param value the initial value
     * @param listListEntry the parent list entry
     */
    public AbstractSliderListCell(T value, OuterSelfT listListEntry) {
      super(value, listListEntry);
      this.sliderWidget = new Slider(0, 0, 152, 20, 0);
    }

    /**
     * Returns the value of the cell as it should be used to position the slider.
     *
     * @return a value between 0.0 and 1.0
     */
    protected abstract double getValueForSlider();

    /**
     * Sets the value of the cell based on the position of the slider.
     *
     * @param value a value between 0.0 and 1.0
     */
    protected abstract void setValueFromSlider(double value);

    /**
     * Updates the slider's value to match the value of the cell.
     */
    protected void syncValueToSlider() {
      sliderWidget.syncValueFromCell();
    }

    /**
     * Uses the parent list entry's {@code textGetter} to produce a textual representation of the
     * cell's value.
     *
     * @return the textual representation of the cell's value
     */
    protected Text getValueForMessage() {
      if (listListEntry.textGetter == null) {
        return null;
      } else {
        return listListEntry.textGetter.apply(getValue());
      }
    }

    @Override
    public void onAdd() {
      syncValueToSlider();
      sliderWidget.updateMessage();
    }

    @Override
    public void updateSelected(boolean isSelected) {
      this.isSelected = isSelected;
    }

    @Override
    public Optional<Text> getError() {
      return Optional.empty();
    }

    @Override
    public int getCellHeight() {
      return 22;
    }

    @Override
    public Selectable.SelectionType getType() {
      return isSelected ? SelectionType.FOCUSED
          : isHovered ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
      sliderWidget.appendNarrations(builder);
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x,
        int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isFocusedCell,
        float delta) {
      sliderWidget.setX(x);
      sliderWidget.setY(y);
      sliderWidget.setWidth(entryWidth - 12);
      sliderWidget.active = listListEntry.isEditable();
      isHovered = sliderWidget.isMouseOver(mouseX, mouseY);
      sliderWidget.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends Element> children() {
      return Collections.singletonList(sliderWidget);
    }

    private class Slider extends SliderWidget {
      protected Slider(int x, int y, int width, int height, double value) {
        super(x, y, width, height, ChatNarratorManager.NO_TITLE, value);
      }

      protected void syncValueFromCell() {
        this.value = AbstractSliderListCell.this.getValueForSlider();
        updateMessage();
      }

      @Override
      public void updateMessage() {
        if (AbstractSliderListCell.this.listListEntry.textGetter != null) {
          setMessage(AbstractSliderListCell.this.getValueForMessage());
        }
      }

      @Override
      protected void applyValue() {
        AbstractSliderListCell.this.setValueFromSlider(value);
      }

      @Override
      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!listListEntry.isEditable()) {
          return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
      }

      @Override
      public boolean mouseDragged(double mouseX, double mouseY, int button,
          double deltaX, double deltaY) {
        if (!listListEntry.isEditable()) {
          return false;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
    }
  }
}
