package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * A data table column showing a number formatted in the user's locale and aligned
 * right, so digits line up: {@code 1,234.50} in English, {@code 1.234,50} in German.
 * With a {@link #setCurrency currency} it shows an amount with the currency's symbol
 * and decimals ({@code €1,234.50}, {@code 1.234,50 €}):
 * <pre>{@code
 * new OatNumberColumn<Invoice, String>(Model.of("Total"), "total", Invoice::total)
 *         .setCurrency(Invoice::currency)
 * }</pre>
 * Empty cells stay empty for a {@code null} value.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatNumberColumn<T, S> extends LambdaColumn<T, S> {

    private int minFractionDigits = -1;
    private int maxFractionDigits = -1;
    private SerializableFunction<T, Currency> currency;

    /** An unsortable column. */
    public OatNumberColumn(IModel<String> displayModel, SerializableFunction<T, ? extends Number> value) {
        this(displayModel, null, value);
    }

    /** @param sortProperty the property to sort by, or {@code null} if unsortable */
    public OatNumberColumn(IModel<String> displayModel, S sortProperty, SerializableFunction<T, ? extends Number> value) {
        super(displayModel, sortProperty, value::apply);
    }

    /**
     * The number of decimals shown, e.g. {@code (2, 2)} for {@code 3.10}. By default a
     * plain number shows up to 3 and an amount its currency's.
     */
    public OatNumberColumn<T, S> setFractionDigits(int min, int max) {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Need 0 <= min <= max, but got " + min + " and " + max);
        }
        this.minFractionDigits = min;
        this.maxFractionDigits = max;
        return this;
    }

    /** Shows every value as an amount in this currency. */
    public OatNumberColumn<T, S> setCurrency(Currency currency) {
        return setCurrency(row -> currency);
    }

    /** Shows each value as an amount in the row's currency, e.g. {@code Invoice::currency}. */
    public OatNumberColumn<T, S> setCurrency(SerializableFunction<T, Currency> currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public String getCssClass() {
        return "align-right";
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new Label(componentId, getDataModel(rowModel)) {
            @Override
            protected IConverter<?> createConverter(Class<?> type) {
                Currency rowCurrency = currency != null ? currency.apply(rowModel.getObject()) : null;
                return new NumberConverter(rowCurrency, minFractionDigits, maxFractionDigits);
            }
        });
    }

    private record NumberConverter(Currency currency, int minFractionDigits, int maxFractionDigits)
            implements IConverter<Object> {

        @Override
        public String convertToString(Object value, Locale locale) {
            if (value == null) {
                return null;
            }
            NumberFormat format;
            if (currency != null) {
                format = NumberFormat.getCurrencyInstance(locale);
                format.setCurrency(currency);
                int digits = Math.max(currency.getDefaultFractionDigits(), 0);
                format.setMinimumFractionDigits(digits);
                format.setMaximumFractionDigits(digits);
            } else {
                format = NumberFormat.getNumberInstance(locale);
            }
            if (minFractionDigits >= 0) {
                format.setMinimumFractionDigits(minFractionDigits);
                format.setMaximumFractionDigits(maxFractionDigits);
            }
            return format.format(value);
        }

        @Override
        public Object convertToObject(String value, Locale locale) {
            throw new UnsupportedOperationException("A table cell is never parsed");
        }
    }
}
