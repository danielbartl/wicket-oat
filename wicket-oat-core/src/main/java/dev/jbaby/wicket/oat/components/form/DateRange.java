package dev.jbaby.wicket.oat.components.form;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A range of days for {@link OatDateRangeField}, both ends included. Either end may be
 * {@code null} for a range open on that side, e.g. "everything since March".
 *
 * @param from the first day, or {@code null}
 * @param to the last day, or {@code null}
 */
public record DateRange(LocalDate from, LocalDate to) implements Serializable {

    /** Whether the day is in the range, counting a missing end as unbounded. */
    public boolean contains(LocalDate day) {
        return (from == null || !day.isBefore(from)) && (to == null || !day.isAfter(to));
    }
}
