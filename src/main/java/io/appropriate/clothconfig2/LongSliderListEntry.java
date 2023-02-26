package io.appropriate.minecraft.clothconfig2;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class LongSliderListEntry extends AbstractSliderListEntry<Long, LongSliderListEntry.LongSliderListCell, LongSliderListEntry> {
    private static final Function<Long, Text> DEFAULT_TEXT_GETTER = value -> Text.of(String.format("Value: %d", value));

    public LongSliderListEntry(Text fieldName, long minimum, long maximum, List<Long> value, boolean defaultExpanded, Supplier<Optional<Text[]>> tooltipSupplier, Consumer<List<Long>> saveConsumer, Supplier<List<Long>> defaultValue, long cellDefaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
        super(fieldName, minimum, maximum, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, cellDefaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, LongSliderListCell::new);
        setTextGetter(DEFAULT_TEXT_GETTER);
        cells.forEach(LongSliderListCell::syncValueToSlider);
    }

    @Override
    public LongSliderListEntry self() {
        return this;
    }

    public static class LongSliderListCell extends AbstractSliderListEntry.AbstractSliderListCell<Long, LongSliderListCell, LongSliderListEntry> {
        private final AtomicLong value;

        public LongSliderListCell(Long value, LongSliderListEntry listListEntry) {
            super(value, listListEntry);

            this.value = new AtomicLong(value == null ? listListEntry.cellDefaultValue : value);
        }

        @Override
        public Long getValue() {
            return value.get();
        }

        @Override
        protected double getValueForSlider() {
            return ((double) this.value.get() - listListEntry.minimum) / Math.abs(listListEntry.maximum - listListEntry.minimum);
        }

        @Override
        protected void setValueFromSlider(double value) {
            this.value.set((long) (listListEntry.minimum + Math.abs(listListEntry.maximum - listListEntry.minimum) * value));
        }
    }
}
