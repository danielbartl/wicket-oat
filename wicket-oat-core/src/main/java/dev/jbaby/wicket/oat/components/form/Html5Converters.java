package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.LocalDateConverter;
import org.apache.wicket.util.convert.converter.LocalDateTimeConverter;
import org.apache.wicket.util.convert.converter.LocalTimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Converters for the {@code java.time} types behind HTML5 date/time inputs. Browsers
 * always render and submit these inputs' values in ISO 8601 form ({@code 2026-10-02},
 * {@code 14:30}, {@code 2026-10-02T14:30}) whatever the user's locale, whereas Wicket's
 * default converters use the session locale's short format.
 */
public final class Html5Converters {

    private Html5Converters() {}

    /** For {@code <input type="date">}: {@code yyyy-MM-dd}. */
    public static IConverter<LocalDate> date() {
        return new IsoLocalDateConverter();
    }

    /** For {@code <input type="time">}: {@code HH:mm[:ss[.SSS]]}. */
    public static IConverter<LocalTime> time() {
        return new IsoLocalTimeConverter();
    }

    /** For {@code <input type="datetime-local">}: {@code yyyy-MM-ddTHH:mm[:ss[.SSS]]}. */
    public static IConverter<LocalDateTime> dateTimeLocal() {
        return new IsoLocalDateTimeConverter();
    }

    private static final class IsoLocalDateConverter extends LocalDateConverter {
        @Override
        protected DateTimeFormatter getDateTimeFormatter() {
            return DateTimeFormatter.ISO_LOCAL_DATE;
        }
    }

    private static final class IsoLocalTimeConverter extends LocalTimeConverter {
        @Override
        protected DateTimeFormatter getDateTimeFormatter() {
            return DateTimeFormatter.ISO_LOCAL_TIME;
        }

        // toString() omits zero seconds like browsers do (ISO_LOCAL_TIME would always print
        // them, making the picker show a seconds field); HTML allows at most milliseconds.
        @Override
        public String convertToString(LocalTime value, Locale locale) {
            return value == null ? null : value.truncatedTo(ChronoUnit.MILLIS).toString();
        }
    }

    private static final class IsoLocalDateTimeConverter extends LocalDateTimeConverter {
        @Override
        protected DateTimeFormatter getDateTimeFormatter() {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }

        // toString() omits zero seconds like browsers do (ISO_LOCAL_DATE_TIME would always print
        // them, making the picker show a seconds field); HTML allows at most milliseconds.
        @Override
        public String convertToString(LocalDateTime value, Locale locale) {
            return value == null ? null : value.truncatedTo(ChronoUnit.MILLIS).toString();
        }
    }
}
