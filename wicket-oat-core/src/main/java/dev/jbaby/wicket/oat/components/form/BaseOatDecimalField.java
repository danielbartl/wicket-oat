package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.apache.wicket.validation.validator.RangeValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Base class for Oat fields that edit a {@link BigDecimal} in the user's locale, such
 * as {@code 1.234,50} in German and {@code 1,234.50} in English. It renders a text
 * input with {@code inputmode="decimal"} rather than {@code type="number"}, which can't
 * show grouping separators, so mobile browsers still offer a numeric keyboard.
 * <p>
 * Values are shown with {@link #getMinFractionDigits()} to {@link #getMaxFractionDigits()}
 * decimals, and input is rounded half-even to at most {@link #getMaxFractionDigits()}.
 *
 * @param <F> the concrete field type, returned by the fluent setters
 */
public abstract class BaseOatDecimalField<F extends BaseOatDecimalField<F>> extends BaseOatInputField<BigDecimal, TextField<BigDecimal>, F> {

    private BigDecimal min;
    private BigDecimal max;
    private RangeValidator<BigDecimal> range;

    public BaseOatDecimalField(String id, IModel<String> label, IModel<BigDecimal> model, IModel<String> helperText) {
        super(id, label, model, helperText);
    }

    /** The fewest decimals a value is shown with. */
    protected abstract int getMinFractionDigits();

    /** The most decimals a value is shown and stored with. */
    protected abstract int getMaxFractionDigits();

    /**
     * Strips text the user may type along with the number, such as a currency symbol,
     * before parsing. Returns the input unchanged by default.
     */
    protected String stripInput(String input, Locale locale) {
        return input;
    }

    /** The smallest value allowed, or {@code null} for none. */
    public F setMin(BigDecimal min) {
        this.min = min;
        updateRange();
        return self();
    }

    /** The largest value allowed, or {@code null} for none. */
    public F setMax(BigDecimal max) {
        this.max = max;
        updateRange();
        return self();
    }

    private void updateRange() {
        if (range != null) {
            getField().remove(range);
        }
        range = (min == null && max == null) ? null : new RangeValidator<>(min, max);
        if (range != null) {
            getField().add(range);
        }
    }

    @Override
    protected TextField<BigDecimal> createFormComponent(String id, IModel<BigDecimal> model) {
        return new TextField<>(id, model, BigDecimal.class) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "text");
                tag.put("inputmode", "decimal");
                super.onComponentTag(tag);
            }

            @Override
            protected IConverter<?> createConverter(Class<?> type) {
                // A new one each time: the digits may follow a model, e.g. the currency
                return BigDecimal.class.isAssignableFrom(type)
                        ? new DecimalConverter(getMinFractionDigits(), getMaxFractionDigits())
                        : super.createConverter(type);
            }
        };
    }

    private final class DecimalConverter extends BigDecimalConverter {

        private final int minFractionDigits;
        private final int maxFractionDigits;

        DecimalConverter(int minFractionDigits, int maxFractionDigits) {
            this.minFractionDigits = minFractionDigits;
            this.maxFractionDigits = maxFractionDigits;
        }

        @Override
        protected NumberFormat newNumberFormat(Locale locale) {
            NumberFormat format = NumberFormat.getNumberInstance(locale);
            format.setMinimumFractionDigits(minFractionDigits);
            format.setMaximumFractionDigits(maxFractionDigits);
            format.setRoundingMode(RoundingMode.HALF_EVEN);
            return format;
        }

        @Override
        public BigDecimal convertToObject(String value, Locale locale) {
            if (value == null) {
                return null;
            }
            // Also trims no-break spaces, e.g. left over from a pasted "12,50 €"
            String input = stripInput(value, locale).replaceAll("^[\\s\\u00A0\\u202F]+|[\\s\\u00A0\\u202F]+$", "");
            if (input.isEmpty()) {
                return null;
            }
            BigDecimal number = super.convertToObject(input, locale);
            if (number == null) {
                return null;
            }
            if (number.scale() > maxFractionDigits) {
                return number.setScale(maxFractionDigits, RoundingMode.HALF_EVEN);
            }
            return number.scale() < minFractionDigits ? number.setScale(minFractionDigits) : number;
        }
    }
}
