package io.appropriate.minecraft.clothconfig2;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Environment(EnvType.CLIENT)
public abstract class AbstractSliderListEntry<T, C extends AbstractSliderListEntry.AbstractSliderListCell<T, C, SELF>, SELF extends AbstractSliderListEntry<T, C, SELF>> extends AbstractListListEntry<T, C, SELF> {
    protected final T minimum, maximum, cellDefaultValue;
    protected Function<T, Text> textGetter;

    public AbstractSliderListEntry(Text fieldName, T minimum, T maximum, List<T> value, boolean defaultExpanded, Supplier<Optional<Text[]>> tooltipSupplier, Consumer<List<T>> saveConsumer, Supplier<List<T>> defaultValue, T cellDefaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront, BiFunction<T, SELF, C> createNewCell) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, createNewCell);

        this.minimum = requireNonNull(minimum);
        this.maximum = requireNonNull(maximum);
        this.cellDefaultValue = requireNonNull(cellDefaultValue);
    }

    public AbstractSliderListEntry setTextGetter(Function<T, Text> textGetter) {
        this.textGetter = textGetter;
        this.cells.forEach(c -> c.sliderWidget.updateMessage());
        return this;
    }

    public abstract static class AbstractSliderListCell<T, SELF extends AbstractSliderListEntry.AbstractSliderListCell<T, SELF, OUTER_SELF>, OUTER_SELF extends AbstractSliderListEntry<T, SELF, OUTER_SELF>> extends AbstractListListEntry.AbstractListCell<T, SELF, OUTER_SELF> {
        protected final Slider sliderWidget;
        private boolean isSelected;
        private boolean isHovered;

        public AbstractSliderListCell(T value, OUTER_SELF listListEntry) {
            super(value, listListEntry);
            this.sliderWidget = new Slider(0, 0, 152, 20, 0);
        }

        protected abstract double getValueForSlider();

        protected abstract void setValueFromSlider(double value);

        protected void syncValueToSlider() {
            sliderWidget.syncValueFromCell();
        }

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
            return isSelected ? SelectionType.FOCUSED : isHovered ? SelectionType.HOVERED : SelectionType.NONE;
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
            sliderWidget.appendNarrations(builder);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isFocusedCell, float delta) {
            sliderWidget.x = x;
            sliderWidget.y = y;
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
                super(x, y, width, height, NarratorManager.EMPTY, value);
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
            public boolean keyPressed(int int_1, int int_2, int int_3) {
                if (!listListEntry.isEditable())
                    return false;
                return super.keyPressed(int_1, int_2, int_3);
            }

            @Override
            public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
                if (!listListEntry.isEditable())
                    return false;
                return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
            }

            @Override
            protected void renderBackground(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY) {
                /*
                 * If the width is greater than 200, then fill in the gap in the middle with more button bg
                 */
                int gap = width - 200;
                if (gap > 0) {
                    client.getTextureManager().bindTexture(new Identifier("textures/gui/widgets.png"));

                    int offset = 100;
                    do {
                        drawTexture(matrices, x + offset, y, 1, 46 + 0 * 20, Math.min(gap, 198), height);

                        offset += 198;
                        gap -= 198;
                    } while (gap > 0);
                }

                // Note: the non-error highlight color here is a bit darker
                // than the normal highlight of 0xffe0e0e0 to let the scrubber stand out
                if (isSelected && listListEntry.isEditable())
                    fill(matrices, x, y + 19, x + width, y + 20, getConfigError().isPresent() ? 0xffff5555 : 0xffa0a0a0);

                // Render the scrubber on top of anything we've drawn
                super.renderBackground(matrices, client, mouseX, mouseY);
            }
        }
    }
}
