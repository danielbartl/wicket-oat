package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * A data table column showing a date, time or date-time in the user's locale:
 * {@code Oct 9, 2026} in English, {@code 09.10.2026} in German. It takes any
 * {@code java.time} value - {@link LocalDate}, {@code LocalDateTime},
 * {@code ZonedDateTime}, {@code OffsetDateTime}, {@link LocalTime} or {@link Instant}
 * (shown in {@link #setZone the zone}, the server's by default):
 * <pre>{@code
 * new OatDateColumn<Invoice, String>(Model.of("Due"), "dueDate", Invoice::dueDate)
 * }</pre>
 * Dates use {@link FormatStyle#MEDIUM} and times {@link FormatStyle#SHORT} by default;
 * {@link #setStyle} or {@link #setPattern} change that. Empty cells stay empty for a
 * {@code null} value.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatDateColumn<T, S> extends LambdaColumn<T, S> {

    private FormatStyle dateStyle = FormatStyle.MEDIUM;
    private FormatStyle timeStyle = FormatStyle.SHORT;
    private String pattern;
    private ZoneId zone;

    /** An unsortable column. */
    public OatDateColumn(IModel<String> displayModel, SerializableFunction<T, ? extends TemporalAccessor> value) {
        this(displayModel, null, value);
    }

    /** @param sortProperty the property to sort by, or {@code null} if unsortable */
    public OatDateColumn(IModel<String> displayModel, S sortProperty, SerializableFunction<T, ? extends TemporalAccessor> value) {
        super(displayModel, sortProperty, value::apply);
    }

    /**
     * The locale's date and time styles, e.g. {@code (SHORT, SHORT)}. Zone-less values
     * ({@code LocalDateTime}, {@code LocalTime}) can't use a {@code LONG} or {@code FULL}
     * time style, which needs a zone.
     */
    public OatDateColumn<T, S> setStyle(FormatStyle dateStyle, FormatStyle timeStyle) {
        this.dateStyle = dateStyle;
        this.timeStyle = timeStyle;
        this.pattern = null;
        return this;
    }

    /** A fixed pattern instead of the locale's style, e.g. {@code "yyyy-MM-dd"}. */
    public OatDateColumn<T, S> setPattern(String pattern) {
        DateTimeFormatter.ofPattern(pattern); // fail fast on a bad pattern
        this.pattern = pattern;
        return this;
    }

    /** The time zone {@link Instant}s are shown in. */
    public OatDateColumn<T, S> setZone(ZoneId zone) {
        this.zone = zone;
        return this;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new Label(componentId, getDataModel(rowModel)) {
            @Override
            protected IConverter<?> createConverter(Class<?> type) {
                return new DateConverter(dateStyle, timeStyle, pattern, zone);
            }
        });
    }

    private record DateConverter(FormatStyle dateStyle, FormatStyle timeStyle, String pattern, ZoneId zone)
            implements IConverter<Object> {

        @Override
        public String convertToString(Object value, Locale locale) {
            if (value == null) {
                return null;
            }
            TemporalAccessor temporal = value instanceof Instant instant
                    ? instant.atZone(zone != null ? zone : ZoneId.systemDefault())
                    : (TemporalAccessor) value;
            DateTimeFormatter formatter;
            if (pattern != null) {
                formatter = DateTimeFormatter.ofPattern(pattern);
            } else if (temporal instanceof LocalDate) {
                formatter = DateTimeFormatter.ofLocalizedDate(dateStyle);
            } else if (temporal instanceof LocalTime) {
                formatter = DateTimeFormatter.ofLocalizedTime(timeStyle);
            } else {
                formatter = DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle);
            }
            return formatter.withLocale(locale).format(temporal);
        }

        @Override
        public Object convertToObject(String value, Locale locale) {
            throw new UnsupportedOperationException("A table cell is never parsed");
        }
    }
}
