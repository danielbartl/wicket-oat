package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * A label showing numbers and dates as people read them in their locale: numbers with
 * grouping ({@code 48,250}, {@code 48.250}), dates in the medium style
 * ({@code Mar 1, 2019}, {@code 01.03.2019}) - Wicket's own converters leave whole
 * numbers ungrouped and use the short date style ({@code 3/1/19}). Anything else uses
 * Wicket's converters as usual.
 */
class ValueLabel extends Label {

    ValueLabel(String id, IModel<?> model) {
        super(id, model);
    }

    @Override
    protected IConverter<?> createConverter(Class<?> type) {
        if (Number.class.isAssignableFrom(type)) {
            return Converter.NUMBER;
        }
        if (LocalDate.class.isAssignableFrom(type) || LocalDateTime.class.isAssignableFrom(type)
                || ZonedDateTime.class.isAssignableFrom(type) || OffsetDateTime.class.isAssignableFrom(type)
                || LocalTime.class.isAssignableFrom(type)) {
            return Converter.DATE;
        }
        return super.createConverter(type);
    }

    private enum Converter implements IConverter<Object> {
        NUMBER {
            @Override
            public String convertToString(Object value, Locale locale) {
                return value == null ? null : NumberFormat.getNumberInstance(locale).format(value);
            }
        },
        DATE {
            @Override
            public String convertToString(Object value, Locale locale) {
                if (value == null) {
                    return null;
                }
                DateTimeFormatter formatter = value instanceof LocalDate ? DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        : value instanceof LocalTime ? DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                        : DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
                return formatter.withLocale(locale).format((TemporalAccessor) value);
            }
        };

        @Override
        public Object convertToObject(String value, Locale locale) {
            throw new UnsupportedOperationException("A label is never parsed");
        }
    }
}
